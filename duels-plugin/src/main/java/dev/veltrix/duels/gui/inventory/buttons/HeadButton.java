package dev.veltrix.duels.gui.inventory.buttons;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.gui.BaseButton;
import dev.veltrix.duels.util.compat.Items;
import dev.veltrix.duels.util.inventory.ItemBuilder;
import org.bukkit.entity.Player;

public class HeadButton extends BaseButton {

    public HeadButton(final DuelsPlugin plugin, final Player owner) {
        super(plugin, ItemBuilder
                .of(Items.HEAD.clone())
                .name(plugin.getLang().getMessage("GUI.inventory-view.buttons.head.name", "name", owner.getName()), plugin.getLang())
                .build()
        );
        setOwner(owner);
    }
}
