package dev.veltrix.duels.gui.settings.buttons;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.Permissions;
import dev.veltrix.duels.gui.BaseButton;
import dev.veltrix.duels.setting.Settings;
import dev.veltrix.duels.util.compat.Items;
import dev.veltrix.duels.util.inventory.ItemBuilder;
import org.bukkit.entity.Player;

public class KitSelectButton extends BaseButton {

    public KitSelectButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Items.from(plugin.getConfiguration().getKitSelectorButtonType(), plugin.getConfiguration().getKitSelectorButtonData()))
                .name(plugin.getLang().getMessage("GUI.settings.buttons.kit-selector.name"), plugin.getLang()).build());
    }

    @Override
    public void update(final Player player) {
        if (config.isKitSelectingUsePermission() && !player.hasPermission(Permissions.KIT_SELECTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
            setLore(lang, lang.getMessage("GUI.settings.buttons.kit-selector.lore-no-permission").split("\n"));
            return;
        }

        final Settings settings = settingManager.getSafely(player);
        final String kit = settings.getKit() != null ? settings.getKit().getName() : lang.getMessage("GENERAL.not-selected");
        final String lore = lang.getMessage("GUI.settings.buttons.kit-selector.lore", "kit", kit);
        setLore(lang, lore.split("\n"));
    }

    @Override
    public void onClick(final Player player) {
        if (config.isKitSelectingUsePermission() && !player.hasPermission(Permissions.KIT_SELECTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
            lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.KIT_SELECTING);
            return;
        }

        kitManager.getGui().open(player);
    }
}
