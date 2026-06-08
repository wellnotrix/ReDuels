package dev.veltrix.duels.gui.settings.buttons;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.gui.BaseButton;
import dev.veltrix.duels.util.compat.Items;
import dev.veltrix.duels.util.inventory.ItemBuilder;
import org.bukkit.entity.Player;

public class CancelButton extends BaseButton {

    public CancelButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Items.from(plugin.getConfiguration().getCancelButtonType(), plugin.getConfiguration().getCancelButtonData()))
                .name(plugin.getLang().getMessage("GUI.settings.buttons.cancel.name"), plugin.getLang()).build());
    }

    @Override
    public void onClick(final Player player) {
        player.closeInventory();
    }
}
