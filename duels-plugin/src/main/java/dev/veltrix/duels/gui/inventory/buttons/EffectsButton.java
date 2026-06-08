package dev.veltrix.duels.gui.inventory.buttons;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.gui.BaseButton;
import dev.veltrix.duels.util.StringUtil;
import dev.veltrix.duels.util.compat.CompatUtil;
import dev.veltrix.duels.util.compat.Items;
import dev.veltrix.duels.util.inventory.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.stream.Collectors;

public class EffectsButton extends BaseButton {

    public EffectsButton(final DuelsPlugin plugin, final Player player) {
        super(plugin, ItemBuilder
                .of(Items.WATER_BREATHING_POTION.clone())
                .name(plugin.getLang().getMessage("GUI.inventory-view.buttons.effects.name"), plugin.getLang())
                .lore(player.getActivePotionEffects().stream()
                        .map(effect -> plugin.getLang().getMessage("GUI.inventory-view.buttons.effects.lore-format",
                                "type", StringUtil.capitalize(effect.getType().getName().replace("_", " ").toLowerCase()),
                                "amplifier", StringUtil.toRoman(effect.getAmplifier() + 1),
                                "duration", (effect.getDuration() / 20))).collect(Collectors.toList()), plugin.getLang())
                .build());
        editMeta(meta -> {
            if (CompatUtil.hasItemFlag()) {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }
        });
    }
}
