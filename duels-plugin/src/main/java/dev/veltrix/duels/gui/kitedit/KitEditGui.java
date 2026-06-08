package dev.veltrix.duels.gui.kitedit;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.gui.kitedit.buttons.KitEditButton;
import dev.veltrix.duels.core.kit.KitImpl;
import dev.veltrix.duels.util.compat.Items;
import dev.veltrix.duels.util.gui.MultiPageGui;
import dev.veltrix.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for selecting a kit to edit.
 */
public class KitEditGui extends MultiPageGui<DuelsPlugin> {

    public KitEditGui(final DuelsPlugin plugin, final List<KitImpl> kits) {
        super(plugin, 
            plugin.getLang().getMessage("GUI.kit-edit-selector.title"), 
            plugin.getConfiguration().getKitSelectorRows(), 
            convertToButtons(plugin, kits));
        
        setSpaceFiller(Items.from(
            plugin.getConfiguration().getKitSelectorFillerType(), 
            plugin.getConfiguration().getKitSelectorFillerData()));
        
        setPrevButton(ItemBuilder.of(Material.PAPER)
            .name(plugin.getLang().getMessage("GUI.kit-edit-selector.buttons.previous-page.name"), plugin.getLang())
            .build());
        
        setNextButton(ItemBuilder.of(Material.PAPER)
            .name(plugin.getLang().getMessage("GUI.kit-edit-selector.buttons.next-page.name"), plugin.getLang())
            .build());
        
        setEmptyIndicator(ItemBuilder.of(Material.PAPER)
            .name(plugin.getLang().getMessage("GUI.kit-edit-selector.buttons.empty.name"), plugin.getLang())
            .build());
        
        // Calculate pages before opening
        calculatePages();
    }

    private static List<KitEditButton> convertToButtons(DuelsPlugin plugin, List<KitImpl> kits) {
        List<KitEditButton> buttons = new ArrayList<>();
        for (KitImpl kit : kits) {
            buttons.add(new KitEditButton(plugin, kit));
        }
        return buttons;
    }
}
