package dev.veltrix.duels.command.commands.duels;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.Permissions;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.command.commands.duels.subcommands.*;
import org.bukkit.command.CommandSender;

public class UserAdminCommand extends BaseCommand {

    public UserAdminCommand(final DuelsPlugin plugin) {
        super(plugin, "user", Permissions.ADMIN, false, "users", "u");
        child(
                new ResetCommand(plugin),
                new SetratingCommand(plugin),
                new ResetratingCommand(plugin),
                new EditCommand(plugin)
        );
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        lang.sendMessage(sender, "COMMAND.duels.help.user", "command", label);
    }
}
