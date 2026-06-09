package dev.veltrix.duels.command.commands.duels;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.Permissions;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.command.commands.duels.subcommands.*;
import org.bukkit.command.CommandSender;

public class KitAdminCommand extends BaseCommand {

    public KitAdminCommand(final DuelsPlugin plugin) {
        super(plugin, "kit", Permissions.ADMIN, false, "kits", "k");
        child(
                new SavekitCommand(plugin),
                new DeletekitCommand(plugin),
                new LoadkitCommand(plugin),
                new SetitemCommand(plugin),
                new OptionsCommand(plugin),
                new BindCommand(plugin),
                new ListCommand(plugin)
        );
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        lang.sendMessage(sender, "COMMAND.duels.help.kit", "command", label);
    }
}
