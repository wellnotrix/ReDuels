package dev.veltrix.duels.core.kit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import lombok.Getter;
import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.api.event.kit.KitCreateEvent;
import dev.veltrix.duels.api.event.kit.KitRemoveEvent;
import dev.veltrix.duels.api.kit.Kit;
import dev.veltrix.duels.api.kit.KitManager;
import dev.veltrix.duels.config.Config;
import dev.veltrix.duels.config.Lang;
import dev.veltrix.duels.data.KitData;
import dev.veltrix.duels.util.Loadable;
import dev.veltrix.duels.util.Log;
import dev.veltrix.duels.util.StringUtil;
import dev.veltrix.duels.util.compat.Items;
import dev.veltrix.duels.util.gui.MultiPageGui;
import dev.veltrix.duels.util.inventory.ItemBuilder;
import dev.veltrix.duels.util.io.FileUtil;
import dev.veltrix.duels.util.json.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;

public class KitManagerImpl implements Loadable, KitManager {

    private static final String FILE_NAME = "kits.json";

    private static final String ERROR_NOT_ALPHANUMERIC = "&c&lCould not load kit %s: Name is not alphanumeric.";
    private static final String KITS_LOADED = "&2Loaded %s kit(s).";

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final File file;

    private final Map<String, KitImpl> kits = new LinkedHashMap<>();

    @Getter
    private MultiPageGui<DuelsPlugin> gui;

    public KitManagerImpl(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
    }

    @Override
    public void handleLoad() throws IOException {
        gui = new MultiPageGui<>(plugin, lang.getMessage("GUI.kit-selector.title"), config.getKitSelectorRows(), kits.values());
        gui.setSpaceFiller(Items.from(config.getKitSelectorFillerType(), config.getKitSelectorFillerData()));
        gui.setPrevButton(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.kit-selector.buttons.previous-page.name"), lang).build());
        gui.setNextButton(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.kit-selector.buttons.next-page.name"), lang).build());
        gui.setEmptyIndicator(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.kit-selector.buttons.empty.name"), lang).build());
        plugin.getGuiListener().addGui(gui);

        if (FileUtil.checkNonEmpty(file, true)) {
            try (final Reader reader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8)) {
                final Map<String, KitData> data = JsonUtil.getObjectMapper().readValue(reader, new TypeReference<LinkedHashMap<String, KitData>>() {
                });

                if (data != null) {
                    for (final Map.Entry<String, KitData> entry : data.entrySet()) {
                        if (!StringUtil.isAlphanumeric(entry.getKey())) {
                            DuelsPlugin.sendMessage(String.format(ERROR_NOT_ALPHANUMERIC, entry.getKey()));
                            continue;
                        }

                        kits.put(entry.getKey(), entry.getValue().toKit(plugin));
                    }
                }
            }
        }

        DuelsPlugin.sendMessage(String.format(KITS_LOADED, kits.size()));
        gui.calculatePages();
    }

    @Override
    public void handleUnload() {
        if (gui != null) {
            plugin.getGuiListener().removeGui(gui);
        }

        kits.clear();
    }

    void saveKits() {
        final Map<String, KitData> data = new LinkedHashMap<>();

        for (final Map.Entry<String, KitImpl> entry : kits.entrySet()) {
            data.put(entry.getKey(), KitData.fromKit(entry.getValue()));
        }

        plugin.doAsync(() -> {
            try (final Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
                JsonUtil.getObjectWriter().writeValue(writer, data);
                writer.flush();
            } catch (IOException ex) {
                Log.error(this, "Failed to save kits asynchronously: " + ex.getMessage(), ex);
            }
        });
    }

    @Nullable
    @Override
    public KitImpl get(@NotNull final String name) {
        Objects.requireNonNull(name, "name");
        return kits.get(name);
    }

    public KitImpl create(@NotNull final Player creator, @NotNull final String name, final boolean override) {
        Objects.requireNonNull(creator, "creator");
        Objects.requireNonNull(name, "name");

        if (!StringUtil.isAlphanumeric(name) || (!override && kits.containsKey(name))) {
            return null;
        }

        final KitImpl kit = new KitImpl(plugin, name, creator.getInventory());
        kits.put(name, kit);
        saveKits();

        final KitCreateEvent event = new KitCreateEvent(creator, kit);
        Bukkit.getPluginManager().callEvent(event);
        gui.calculatePages();
        return kit;
    }

    @Nullable
    @Override
    public KitImpl create(@NotNull final Player creator, @NotNull final String name) {
        return create(creator, name, false);
    }

    @Nullable
    @Override
    public KitImpl remove(@Nullable CommandSender source, @NotNull final String name) {
        Objects.requireNonNull(name, "name");

        final KitImpl kit = kits.remove(name);

        if (kit == null) {
            return null;
        }

        kit.setRemoved(true);
        plugin.getArenaManager().clearBinds(kit);
        saveKits();

        final KitRemoveEvent event = new KitRemoveEvent(source, kit);
        Bukkit.getPluginManager().callEvent(event);
        gui.calculatePages();
        return kit;
    }

    @Nullable
    @Override
    public KitImpl remove(@NotNull final String name) {
        return remove(null, name);
    }

    @NotNull
    @Override
    public List<Kit> getKits() {
        return Collections.unmodifiableList(Lists.newArrayList(kits.values()));
    }

    public List<String> getNames(final boolean nokit) {
        final List<String> names = new ArrayList<>(kits.keySet());

        if (nokit) {
            names.add("-"); // Special case: Change the nokit rating
        }

        return names;
    }
}