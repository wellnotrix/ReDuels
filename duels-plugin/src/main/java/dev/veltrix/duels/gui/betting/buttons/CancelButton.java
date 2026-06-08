package dev.veltrix.duels.gui.betting.buttons;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.gui.BaseButton;
import dev.veltrix.duels.util.compat.Items;
import dev.veltrix.duels.util.inventory.ItemBuilder;
import org.bukkit.entity.Player;

public class CancelButton extends BaseButton {

    public CancelButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder
                .of(Items.RED_PANE.clone())
                .name(plugin.getLang().getMessage("GUI.item-betting.buttons.cancel.name"), plugin.getLang())
                .lore(plugin.getLang(), plugin.getLang().getMessage("GUI.item-betting.buttons.cancel.lore").split("\n"))
                .build()
        );
    }

    @Override
    public void onClick(final Player player) {
        player.closeInventory();
    }
}
