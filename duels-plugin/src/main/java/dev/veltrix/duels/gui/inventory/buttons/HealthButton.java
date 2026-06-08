package dev.veltrix.duels.gui.inventory.buttons;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.gui.BaseButton;
import dev.veltrix.duels.util.compat.Items;
import dev.veltrix.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class HealthButton extends BaseButton {

    public HealthButton(final DuelsPlugin plugin, final Player player, final boolean dead) {
        super(plugin, ItemBuilder
                .of(dead ? Items.SKELETON_HEAD : Material.GOLDEN_APPLE)
                .name(plugin.getLang().getMessage("GUI.inventory-view.buttons.health.name", "health", dead ? 0 : Math.ceil(player.getHealth()) * 0.5), plugin.getLang())
                .build());
    }
}
