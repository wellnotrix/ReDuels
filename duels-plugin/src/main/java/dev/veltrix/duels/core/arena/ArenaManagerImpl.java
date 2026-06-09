package dev.veltrix.duels.core.arena;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Charsets;
import lombok.Getter;
import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.api.arena.Arena;
import dev.veltrix.duels.api.arena.ArenaManager;
import dev.veltrix.duels.api.event.arena.ArenaCreateEvent;
import dev.veltrix.duels.api.event.arena.ArenaRemoveEvent;
import dev.veltrix.duels.config.Config;
import dev.veltrix.duels.config.Lang;
import dev.veltrix.duels.data.ArenaData;
import dev.veltrix.duels.core.kit.KitImpl;
import dev.veltrix.duels.core.queue.Queue;
import dev.veltrix.duels.util.Loadable;
import dev.veltrix.duels.util.Log;
import dev.veltrix.duels.util.StringUtil;
import dev.veltrix.duels.util.compat.Items;
import dev.veltrix.duels.util.gui.MultiPageGui;
import dev.veltrix.duels.util.inventory.ItemBuilder;
import dev.veltrix.duels.util.io.FileUtil;
import dev.veltrix.duels.util.json.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class ArenaManagerImpl implements Loadable, ArenaManager {

    private static final String FILE_NAME = "arenas.json";

    private static final String ERROR_NOT_ALPHANUMERIC = "&c&lCould not load arena %s: Name is not alphanumeric.";
    private static final String ARENAS_LOADED = "&2Loaded %s arena(s).";

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final File file;

    private final List<ArenaImpl> arenas = new ArrayList<>();
    private final Map<String, ArenaImpl> arenasByName = new ConcurrentHashMap<>();
    private final Map<UUID, ArenaImpl> arenasByPlayer = new ConcurrentHashMap<>();

    @Getter
    private MultiPageGui<DuelsPlugin> gui;

    public ArenaManagerImpl(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.file = new File(plugin.getDataFolder(), FILE_NAME);

        Bukkit.getPluginManager().registerEvents(new ArenaListener(), plugin);
    }

    @Override
    public void handleLoad() throws IOException {
        gui = new MultiPageGui<>(plugin, lang.getMessage("GUI.arena-selector.title"), config.getArenaSelectorRows(), arenas);
        gui.setSpaceFiller(Items.from(config.getArenaSelectorFillerType(), config.getArenaSelectorFillerData()));
        gui.setPrevButton(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.kit-selector.buttons.previous-page.name"), lang).build());
        gui.setNextButton(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.kit-selector.buttons.next-page.name"), lang).build());
        gui.setEmptyIndicator(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.kit-selector.buttons.empty.name"), lang).build());
        plugin.getGuiListener().addGui(gui);

        if (FileUtil.checkNonEmpty(file, true)) {
            try (final Reader reader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8)) {
                final List<ArenaData> data = JsonUtil.getObjectMapper().readValue(reader, new TypeReference<>() {
                });
                if (data != null) {
                    for (ArenaData arenaData : data) {
                        if (!StringUtil.isAlphanumeric(arenaData.getName())) {
                            DuelsPlugin.sendMessage(String.format(ERROR_NOT_ALPHANUMERIC, arenaData.getName()));
                            continue;
                        }
                        ArenaImpl arena = arenaData.toArena(plugin);
                        arenas.add(arena);
                        arenasByName.put(arena.getName(), arena);
                    }
                }
            }
        }

        DuelsPlugin.sendMessage(String.format(ARENAS_LOADED, arenas.size()));
        gui.calculatePages();
    }

    @Override
    public void handleUnload() {
        if (gui != null) {
            plugin.getGuiListener().removeGui(gui);
        }
        arenas.clear();
        arenasByName.clear();
        arenasByPlayer.clear();
    }

    void saveArenas() {
        final List<ArenaData> data = arenas.stream().map(ArenaData::new).toList();
        
        plugin.doAsync(() -> {
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
                JsonUtil.getObjectWriter().writeValue(writer, data);
                writer.flush();
            } catch (IOException ex) {
                Log.error(this, "Failed to save arenas asynchronously: " + ex.getMessage(), ex);
            }
        });
    }

    public void registerPlayer(@NotNull final Player player, @NotNull final ArenaImpl arena) {
        arenasByPlayer.put(player.getUniqueId(), arena);
    }

    public void unregisterPlayer(@NotNull final Player player) {
        arenasByPlayer.remove(player.getUniqueId());
    }

    @Nullable
    @Override
    public ArenaImpl get(@NotNull final String name) {
        Objects.requireNonNull(name, "name");
        return arenasByName.get(name);
    }

    @Nullable
    @Override
    public ArenaImpl get(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");
        ArenaImpl arena = arenasByPlayer.get(player.getUniqueId());
        if (arena != null && arena.has(player)) {
            return arena;
        }
        return null;
    }

    @Override
    public boolean isInMatch(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");
        return get(player) != null;
    }

    @NotNull
    @Override
    public List<Arena> getArenas() {
        return Collections.unmodifiableList(arenas);
    }

    public boolean create(final CommandSender source, final String name) {
        if (get(name) != null) return false;

        ArenaImpl arena = new ArenaImpl(plugin, name);
        arenas.add(arena);
        arenasByName.put(name, arena);

        saveArenas();
        Bukkit.getPluginManager().callEvent(new ArenaCreateEvent(source, arena));
        gui.calculatePages();
        return true;
    }

    public boolean remove(final CommandSender source, final ArenaImpl arena) {
        if (arenas.remove(arena)) {
            arenasByName.remove(arena.getName());
            arena.setRemoved(true);
            saveArenas();
            Bukkit.getPluginManager().callEvent(new ArenaRemoveEvent(source, arena));
            gui.calculatePages();
            return true;
        }
        return false;
    }

    public List<ArenaImpl> getArenasImpl() {
        return arenas;
    }

    public Set<Player> getPlayers() {
        Set<Player> players = new HashSet<>();
        for (ArenaImpl arena : arenas) {
            players.addAll(arena.getPlayers());
        }
        return players;
    }

    public long getPlayersInMatch(final Queue queue) {
        int count = 0;
        for (ArenaImpl arena : arenas) {
            if (arena.isUsed() && Objects.requireNonNull(arena.getMatch()).isFromQueue() && arena.getMatch().getSource().equals(queue)) {
                count++;
            }
        }
        return count * 2L;
    }

    public boolean isSelectable(@Nullable final KitImpl kit, @NotNull final ArenaImpl arena) {
        if (!arena.isAvailable()) return false;
        if (arena.isBoundless()) {
            return kit == null || !kit.isArenaSpecific();
        }
        return arena.isBound(kit);
    }

    public ArenaImpl randomArena(final KitImpl kit) {
        List<ArenaImpl> available = new ArrayList<>();
        for (ArenaImpl arena : arenas) {
            if (isSelectable(kit, arena)) {
                available.add(arena);
            }
        }

        if (!available.isEmpty()) {
            return available.get(ThreadLocalRandom.current().nextInt(available.size()));
        }

        return null;
    }

    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        for (ArenaImpl arena : arenas) {
            names.add(arena.getName());
        }
        return names;
    }

    public void clearBinds(final KitImpl kit) {
        for (ArenaImpl arena : arenas) {
            if (arena.isBound(kit)) {
                arena.bind(kit);
            }
        }
    }

    private class ArenaListener implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void on(PlayerInteractEvent event) {
            if (!event.hasBlock() || !config.isPreventInteract()) return;

            ArenaImpl arena = get(event.getPlayer());
            if (arena == null || arena.isCountingComplete()) return;

            event.setCancelled(true);
        }

        @EventHandler(ignoreCancelled = true)
        public void on(EntityDamageEvent event) {
            if (!config.isPreventPvp() || !(event.getEntity() instanceof Player)) return;

            ArenaImpl arena = get((Player) event.getEntity());
            if (arena == null || arena.isCountingComplete()) return;

            event.setCancelled(true);
        }

        @EventHandler(ignoreCancelled = true)
        public void on(ProjectileLaunchEvent event) {
            if (!config.isPreventLaunchProjectile()) return;

            ProjectileSource shooter = event.getEntity().getShooter();
            if (!(shooter instanceof Player)) return;

            ArenaImpl arena = get((Player) shooter);
            if (arena == null || arena.isCountingComplete()) return;

            event.setCancelled(true);
        }

        @EventHandler(ignoreCancelled = true)
        public void on(PlayerMoveEvent event) {
            if (!config.isPreventMovement()) return;

            Location from = event.getFrom();
            Location to = event.getTo();

            if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) return;

            ArenaImpl arena = get(event.getPlayer());
            if (arena == null || arena.isCountingComplete()) return;

            event.setTo(from);
        }
    }
}
