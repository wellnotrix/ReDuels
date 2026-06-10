package dev.veltrix.duels.core.player;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Charsets;
import lombok.Getter;
import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.core.arena.ArenaImpl;
import dev.veltrix.duels.core.arena.ArenaManagerImpl;
import dev.veltrix.duels.config.Config;
import dev.veltrix.duels.data.LocationData;
import dev.veltrix.duels.data.PlayerData;
import dev.veltrix.duels.hook.hooks.EssentialsHook;
import dev.veltrix.duels.core.match.team.TeamDuelMatch;
import dev.veltrix.duels.core.teleport.Teleport;
import dev.veltrix.duels.util.Loadable;
import dev.veltrix.duels.util.Log;
import dev.veltrix.duels.util.PlayerUtil;
import dev.veltrix.duels.util.io.FileUtil;
import dev.veltrix.duels.util.json.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Manages:
 * (1) player info cache for restoration after matches.
 * (2) lobby location for teleportation after matches.
 */
public class PlayerInfoManager implements Loadable {

    private static final String CACHE_FILE_NAME = "player-cache.json";
    private static final String LOBBY_FILE_NAME = "lobby.json"; // Assuming you still want to use JSON for lobby
    private static final String KIT_LOBBY_FILE_NAME = "kit_lobby.json";

    private static final String ERROR_LOBBY_LOAD = "Could not load lobby location!";
    private static final String ERROR_LOBBY_SAVE = "Could not save lobby location!";
    private static final String WARNING_LOBBY_NOT_SET = "Lobby locations are not set. Use /duels system setlobby and /duels system setkitlobby to set them.";

    private final DuelsPlugin plugin;
    private final Config config;
    private final File cacheFile;
    private final File lobbyFile;
    private final File kitlobbyFile;

    private final Map<UUID, PlayerInfo> cache = new HashMap<>();

    private Teleport teleport;
    private EssentialsHook essentials;
    private ArenaManagerImpl arenaManager;

    private Location lobby;
    private Location kitLobby;
    private LocationData pendingLobby;
    private LocationData pendingKitLobby;

    public PlayerInfoManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.cacheFile = new File(plugin.getDataFolder(), CACHE_FILE_NAME);
        this.lobbyFile = new File(plugin.getDataFolder(), LOBBY_FILE_NAME);
        this.kitlobbyFile = new File(plugin.getDataFolder(), KIT_LOBBY_FILE_NAME);
        plugin.doSyncAfter(() -> Bukkit.getPluginManager().registerEvents(new PlayerInfoListener(), plugin), 1L);
    }

    @Override
    public void handleLoad() throws IOException {
        this.teleport = plugin.getTeleport();
        this.essentials = plugin.getHookManager().getHook(EssentialsHook.class);
        this.arenaManager = plugin.getArenaManager();

        if (FileUtil.checkNonEmpty(cacheFile, false)) {
            try (final Reader reader = new InputStreamReader(new FileInputStream(cacheFile), Charsets.UTF_8)) {
                final Map<UUID, PlayerData> data = JsonUtil.getObjectMapper().readValue(reader, new TypeReference<HashMap<UUID, PlayerData>>() {});

                if (data != null) {
                    for (final Map.Entry<UUID, PlayerData> entry : data.entrySet()) {
                        cache.put(entry.getKey(), entry.getValue().toPlayerInfo());
                    }
                }
            }

            cacheFile.delete();
        }

        if (FileUtil.checkNonEmpty(lobbyFile, false)) {
            try (final Reader reader = new InputStreamReader(Files.newInputStream(lobbyFile.toPath()), Charsets.UTF_8)) {
                this.pendingLobby = JsonUtil.getObjectMapper().readValue(reader, LocationData.class);
                this.lobby = pendingLobby.toLocation();
            } catch (IOException ex) {
                Log.error(this, ERROR_LOBBY_LOAD, ex);
            }
        }

        if (FileUtil.checkNonEmpty(kitlobbyFile, false)) {
            try (final Reader reader = new InputStreamReader(Files.newInputStream(kitlobbyFile.toPath()), Charsets.UTF_8)) {
                this.pendingKitLobby = JsonUtil.getObjectMapper().readValue(reader, LocationData.class);
                this.kitLobby = pendingKitLobby.toLocation();
            } catch (IOException ex) {
                Log.error(this, ERROR_LOBBY_LOAD, ex);
            }
        }

        if (lobby == null && kitLobby == null) {
            Log.warn(this, WARNING_LOBBY_NOT_SET);
        }
    }

    public Location getLobby() {
        if (lobby == null && pendingLobby != null) {
            lobby = pendingLobby.toLocation();
        }
        
        if (lobby == null || lobby.getWorld() == null) {
            return Bukkit.getWorlds().getFirst().getSpawnLocation();
        }
        return lobby;
    }

    public Location getKitLobby() {
        if (kitLobby == null && pendingKitLobby != null) {
            kitLobby = pendingKitLobby.toLocation();
        }

        if (kitLobby == null || kitLobby.getWorld() == null) {
            return Bukkit.getWorlds().getFirst().getSpawnLocation();
        }
        return kitLobby;
    }

    @Override
    public void handleUnload() throws IOException {
        Bukkit.getOnlinePlayers().stream().filter(Player::isDead).forEach(player -> {
            final PlayerInfo info = remove(player);

            if (info != null) {
                player.spigot().respawn();
                teleport.tryTeleport(player, info.getLocation());
                PlayerUtil.reset(player);
                info.restore(player);
            }
        });

        if (cache.isEmpty()) {
            return;
        }

        final Map<UUID, PlayerData> data = new HashMap<>();

        for (final Map.Entry<UUID, PlayerInfo> entry : cache.entrySet()) {
            data.put(entry.getKey(), PlayerData.fromPlayerInfo(entry.getValue()));
        }

        try (final Writer writer = new OutputStreamWriter(new FileOutputStream(cacheFile), Charsets.UTF_8)) {
            JsonUtil.getObjectWriter().writeValue(writer, data);
            writer.flush();
        }

        cache.clear();
    }

    /**
     * Sets a lobby location at given player's location.
     *
     * @param player Player to get location for lobby
     * @return true if setting lobby was successful, false otherwise
     */
    public boolean setLobby(final Player player) {
        final Location lobby = player.getLocation().clone();

        try (final Writer writer = new OutputStreamWriter(Files.newOutputStream(lobbyFile.toPath()), Charsets.UTF_8)) {
            JsonUtil.getObjectWriter().writeValue(writer, LocationData.fromLocation(lobby));
            writer.flush();
            this.lobby = lobby;
            this.pendingLobby = LocationData.fromLocation(lobby);
            Log.info(this, "Lobby location set by " + player.getName() + " at " + lobby.getWorld().getName() + ", " + lobby.getBlockX() + ", " + lobby.getBlockY() + ", " + lobby.getBlockZ());
            return true;
        } catch (IOException ex) {
            Log.error(this, ERROR_LOBBY_SAVE, ex);
            return false;
        }
    }

    public boolean setKitLobby(final Player player) {
        final Location lobby = player.getLocation().clone();

        try (final Writer writer = new OutputStreamWriter(Files.newOutputStream(kitlobbyFile.toPath()), Charsets.UTF_8)) {
            JsonUtil.getObjectWriter().writeValue(writer, LocationData.fromLocation(lobby));
            writer.flush();
            this.kitLobby = lobby;
            this.pendingKitLobby = LocationData.fromLocation(lobby);
            Log.info(this, "Kit lobby location set by " + player.getName() + " at " + lobby.getWorld().getName() + ", " + lobby.getBlockX() + ", " + lobby.getBlockY() + ", " + lobby.getBlockZ());
            return true;
        } catch (IOException ex) {
            Log.error(this, ERROR_LOBBY_SAVE, ex);
            return false;
        }
    }

    /**
     * Gets cached PlayerInfo instance for given player.
     *
     * @param player Player to get cached PlayerInfo instance
     * @return cached PlayerInfo instance or null if not found
     */
    public PlayerInfo get(final Player player) {
        return cache.get(player.getUniqueId());
    }

    /**
     * Creates a cached PlayerInfo instance for given player.
     *
     * @param player Player to create a cached PlayerInfo instance
     * @param excludeInventory true to exclude inventory contents from being stored in PlayerInfo, false otherwise
     */
    public void create(final Player player, final boolean excludeInventory) {
        final PlayerInfo info = new PlayerInfo(player, excludeInventory);

        if (!config.isTeleportToLastLocation()) {
            info.setLocation(lobby.clone());
        }

        cache.put(player.getUniqueId(), info);
    }

    /**
     * Creates a cached PlayerInfo instance for given player with explicit experience restore control.
     *
     * @param player Player to create a cached PlayerInfo instance
     * @param excludeInventory true to exclude inventory contents from being stored in PlayerInfo, false otherwise
     * @param restoreExperience true to restore experience/level on return, false to keep any changes made during duel
     */
    public void create(final Player player, final boolean excludeInventory, final boolean restoreExperience) {
        final PlayerInfo info = new PlayerInfo(player, excludeInventory, restoreExperience);

        if (!config.isTeleportToLastLocation()) {
            info.setLocation(lobby.clone());
        }

        cache.put(player.getUniqueId(), info);
    }

    /**
     * Calls {@link #create(Player, boolean)} with excludeInventory defaulting to false.
     *
     * @see {@link #create(Player, boolean)}
     */
    public void create(final Player player) {
        create(player, false);
    }

    /**
     * Removes the given player from cache.
     *
     * @param player Player to remove from cache
     * @return Removed PlayerInfo instance or null if not found
     */
    public PlayerInfo remove(final Player player) {
        return cache.remove(player.getUniqueId());
    }

    private class PlayerInfoListener implements Listener {

        // Handles case of some players causing respawn to skip somehow.
        @EventHandler(priority = EventPriority.HIGHEST)
        public void on(final PlayerJoinEvent event) {
            final Player player = event.getPlayer();

            if (player.isDead()) {
                return;
            }

            final PlayerInfo info = remove(player);

            if (info == null) {
                return;
            }

            teleport.tryTeleport(player, info.getLocation());
            info.restore(player);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void on(final PlayerRespawnEvent event) {
            final Player player = event.getPlayer();
            final PlayerInfo info = get(player);

            if (info == null) {
                return;
            }

            // Check if player is in a team match and is dead (spectator)
            final ArenaImpl arena = arenaManager.get(player);
            if (arena != null && arena.getMatch() instanceof TeamDuelMatch teamMatch) {
                if (teamMatch.isDead(player)) {
                    // Player is dead in team match, keep them in arena as spectator
                    // Use the arena's center position for spectating
                    event.setRespawnLocation(Objects.requireNonNull(arena.getPosition(1)));
                    
                    // Schedule to set spectator mode after respawn
                    plugin.doSyncAfter(() -> {
                        if (player.isOnline() && arenaManager.get(player) == arena) {
                            player.setGameMode(GameMode.SPECTATOR);
                            player.setAllowFlight(true);
                            player.setFlying(true);
                        }
                    }, 1L);
                    return;
                }
            }

            event.setRespawnLocation(info.getLocation());

            if (essentials != null) {
                essentials.setBackLocation(player, event.getRespawnLocation());
            }

            plugin.doSyncAfter(() -> {
                // Do not remove cached data if player left while respawning.
                if (!player.isOnline()) {
                    return;
                }
                
                // Double-check: Don't remove PlayerInfo if player is dead in team match
                final ArenaImpl stillInArena = arenaManager.get(player);
                if (stillInArena != null && stillInArena.getMatch() instanceof TeamDuelMatch stillTeamMatch) {
                    if (stillTeamMatch.isDead(player)) {
                        // Player is still dead in team match, don't restore them
                        return;
                    }
                }

                remove(player);
                DuelsPlugin.getFoliaLib().getScheduler().runAtEntity(player, task -> info.restore(player));
            }, 1L);
        }
    }
}
