package dev.veltrix.duels.gui.inventory.buttons;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.gui.BaseButton;
import dev.veltrix.duels.util.compat.CompatUtil;
import dev.veltrix.duels.util.compat.Items;
import dev.veltrix.duels.util.inventory.ItemBuilder;
import org.bukkit.inventory.ItemFlag;

public class PotionCounterButton extends BaseButton {

    public PotionCounterButton(final DuelsPlugin plugin, final int count) {
        super(plugin, ItemBuilder
                .of(Items.HEAL_SPLASH_POTION.clone())
                .name(plugin.getLang().getMessage("GUI.inventory-view.buttons.potion-counter.name", "potions", count), plugin.getLang())
                .build()
        );
        editMeta(meta -> {
            if (CompatUtil.hasItemFlag()) {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }
        });
    }
}
