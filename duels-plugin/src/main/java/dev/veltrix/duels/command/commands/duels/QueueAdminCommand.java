package dev.veltrix.duels.command.commands.duels;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.Permissions;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.command.commands.duels.subcommands.*;
import org.bukkit.command.CommandSender;

public class QueueAdminCommand extends BaseCommand {

    public QueueAdminCommand(final DuelsPlugin plugin) {
        super(plugin, "queue", Permissions.ADMIN, false, "queues", "q");
        child(
                new CreatequeueCommand(plugin),
                new DeletequeueCommand(plugin)
        );
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        lang.sendMessage(sender, "COMMAND.duels.help.queue", "command", label);
    }
}
