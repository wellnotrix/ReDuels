package dev.veltrix.duels.command.commands.duel.subcommands;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.gui.inventory.InventoryGui;
import dev.veltrix.duels.util.UUIDUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class InventoryCommand extends BaseCommand {

    public InventoryCommand(final DuelsPlugin plugin) {
        super(plugin, "_", "_ [uuid]", "Displays player's inventories after match.", 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final UUID target = UUIDUtil.parseUUID(args[0]);

        if (target == null) {
            lang.sendMessage(sender, "ERROR.inventory-view.not-a-uuid", "input", args[0]);
            return;
        }

        final InventoryGui gui = inventoryManager.get(UUID.fromString(args[0]));

        if (gui == null) {
            lang.sendMessage(sender, "ERROR.inventory-view.not-found", "uuid", target);
            return;
        }

        gui.open((Player) sender);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return Collections.emptyList();
    }
}
