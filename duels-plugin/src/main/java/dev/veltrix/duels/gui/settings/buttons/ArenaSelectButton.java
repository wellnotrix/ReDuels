package dev.veltrix.duels.gui.settings.buttons;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.Permissions;
import dev.veltrix.duels.gui.BaseButton;
import dev.veltrix.duels.setting.Settings;
import dev.veltrix.duels.util.compat.Items;
import dev.veltrix.duels.util.inventory.ItemBuilder;
import org.bukkit.entity.Player;

public class ArenaSelectButton extends BaseButton {

    public ArenaSelectButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Items.from(plugin.getConfiguration().getArenaSelectorButtonType(), plugin.getConfiguration().getArenaSelectorButtonData()))
                .name(plugin.getLang().getMessage("GUI.settings.buttons.arena-selector.name"), plugin.getLang()).build());
    }

    @Override
    public void update(final Player player) {
        if (config.isArenaSelectingUsePermission() && !player.hasPermission(Permissions.ARENA_SELECTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
            setLore(lang, lang.getMessage("GUI.settings.buttons.arena-selector.lore-no-permission").split("\n"));
            return;
        }

        final Settings settings = settingManager.getSafely(player);
        final String arena = settings.getArena() != null ? settings.getArena().getName() : lang.getMessage("GENERAL.random");
        final String lore = lang.getMessage("GUI.settings.buttons.arena-selector.lore", "arena", arena);
        setLore(lang, lore.split("\n"));
    }

    @Override
    public void onClick(final Player player) {
        if (config.isArenaSelectingUsePermission() && !player.hasPermission(Permissions.ARENA_SELECTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
            lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.ARENA_SELECTING);
            return;
        }

        arenaManager.getGui().open(player);
    }
}
