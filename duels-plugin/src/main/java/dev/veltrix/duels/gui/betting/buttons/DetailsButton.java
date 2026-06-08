package dev.veltrix.duels.gui.betting.buttons;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.gui.BaseButton;
import dev.veltrix.duels.setting.Settings;
import dev.veltrix.duels.util.compat.Items;
import dev.veltrix.duels.util.inventory.ItemBuilder;
import org.bukkit.entity.Player;

public class DetailsButton extends BaseButton {

    private final Settings settings;

    public DetailsButton(final DuelsPlugin plugin, final Settings settings) {
        super(plugin, ItemBuilder
                .of(Items.SIGN)
                .name(plugin.getLang().getMessage("GUI.item-betting.buttons.details.name"), plugin.getLang())
                .build()
        );
        this.settings = settings;
    }

    @Override
    public void update(final Player player) {
        final String lore = lang.getMessage("GUI.item-betting.buttons.details.lore",
                "kit", settings.getKit() != null ? settings.getKit().getName() : lang.getMessage("GENERAL.not-selected"),
                "arena", settings.getArena() != null ? settings.getArena().getName() : lang.getMessage("GENERAL.random"),
                "bet_amount", settings.getBet()
        );
        setLore(lang, lore.split("\n"));
    }
}
