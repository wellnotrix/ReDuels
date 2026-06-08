package dev.veltrix.duels.inventories;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.config.Config;
import dev.veltrix.duels.config.Lang;
import dev.veltrix.duels.gui.inventory.InventoryGui;
import dev.veltrix.duels.core.match.DuelMatch;
import dev.veltrix.duels.util.Loadable;
import dev.veltrix.duels.util.StringUtil;
import dev.veltrix.duels.util.TextBuilder;
import dev.veltrix.duels.util.gui.GuiListener;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.entity.Player;
import dev.veltrix.duels.api.folialib.task.WrappedTask;

import java.util.*;

@SuppressWarnings("deprecation")
public class InventoryManager implements Loadable {

    private final DuelsPlugin plugin;
    private final GuiListener<DuelsPlugin> guiListener;
    private final Map<UUID, InventoryGui> inventories = new HashMap<>();
    private final Config config;
    private final Lang lang;

    private WrappedTask expireTask;

    public InventoryManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.guiListener = plugin.getGuiListener();
    }

    @Override
    public void handleLoad() {
        this.expireTask = plugin.doSyncRepeat(() -> {
            final long now = System.currentTimeMillis();

            inventories.entrySet().removeIf(entry -> {
                if (now - entry.getValue().getCreation() >= 1000L * 60 * 5) {
                    guiListener.removeGui(entry.getValue());
                    return true;
                }

                return false;
            });
        }, 20L, 20L * 5);
    }

    @Override
    public void handleUnload() {
        plugin.cancelTask(expireTask);
        inventories.clear();
    }

    public InventoryGui get(final UUID uuid) {
        return inventories.get(uuid);
    }

    public void create(final Player player, final boolean dead) {
        // Remove previously existing gui
        InventoryGui gui = inventories.remove(player.getUniqueId());

        if (gui != null) {
            guiListener.removeGui(gui);
        }

        gui = new InventoryGui(plugin, player, dead);
        guiListener.addGui(gui);
        inventories.put(player.getUniqueId(), gui);
    }

    public void handleMatchEnd(final DuelMatch match) {
        if (!config.isDisplayInventories()) {
            return;
        }

        String color = lang.getMessage("DUEL.inventories.name-color");
        final TextBuilder builder = TextBuilder.of(lang.getMessage("DUEL.inventories.message"));
        final Set<Player> players = match.getAllPlayers();
        final Iterator<Player> iterator = players.iterator();

        while (iterator.hasNext()) {
            final Player player = iterator.next();
            builder.add(StringUtil.color(color + player.getName()), ClickEvent.Action.RUN_COMMAND, "/duel _ " + player.getUniqueId());

            if (iterator.hasNext()) {
                builder.add(StringUtil.color(color + ", "));
            }
        }

        builder.send(players);
    }

}
