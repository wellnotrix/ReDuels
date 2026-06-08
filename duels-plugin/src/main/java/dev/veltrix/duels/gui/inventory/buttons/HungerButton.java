package dev.veltrix.duels.gui.inventory.buttons;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.gui.BaseButton;
import dev.veltrix.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class HungerButton extends BaseButton {

    public HungerButton(final DuelsPlugin plugin, final Player player) {
        super(plugin, ItemBuilder
                .of(Material.COOKED_BEEF)
                .name(plugin.getLang().getMessage("GUI.inventory-view.buttons.hunger.name", "hunger", player.getFoodLevel()), plugin.getLang())
                .build()
        );
    }
}
