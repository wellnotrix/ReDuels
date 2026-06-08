package dev.veltrix.duels.gui.settings.buttons;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.Permissions;
import dev.veltrix.duels.gui.BaseButton;
import dev.veltrix.duels.setting.Settings;
import dev.veltrix.duels.util.compat.Items;
import dev.veltrix.duels.util.inventory.ItemBuilder;
import org.bukkit.entity.Player;

public class ItemBettingButton extends BaseButton {

    public ItemBettingButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Items.from(plugin.getConfiguration().getItemBettingButtonType(), plugin.getConfiguration().getItemBettingButtonData()))
                .name(plugin.getLang().getMessage("GUI.settings.buttons.item-betting.name"), plugin.getLang()).build());
    }

    @Override
    public void update(final Player player) {
        if (config.isItemBettingUsePermission() && !player.hasPermission(Permissions.ITEM_BETTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
            setLore(lang, lang.getMessage("GUI.settings.buttons.item-betting.lore-no-permission").split("\n"));
            return;
        }

        final Settings settings = settingManager.getSafely(player);
        final String itemBetting = settings.isItemBetting() ? lang.getMessage("GENERAL.enabled") : lang.getMessage("GENERAL.disabled");
        final String lore = plugin.getLang().getMessage("GUI.settings.buttons.item-betting.lore", "item_betting", itemBetting);
        setLore(lang, lore.split("\n"));
    }

    @Override
    public void onClick(final Player player) {
        if (config.isItemBettingUsePermission() && !player.hasPermission(Permissions.ITEM_BETTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
            lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.ITEM_BETTING);
            return;
        }

        final Settings settings = settingManager.getSafely(player);

        if (settings.isPartyDuel()) {
            lang.sendMessage(player, "ERROR.party-duel.option-unavailable");
            return;
        }

        settings.setItemBetting(!settings.isItemBetting());
        settings.updateGui(player);
    }
}
