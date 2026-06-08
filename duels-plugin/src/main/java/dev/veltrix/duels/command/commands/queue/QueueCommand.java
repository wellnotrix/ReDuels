package dev.veltrix.duels.command.commands.queue;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.Permissions;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.command.commands.queue.subcommands.JoinCommand;
import dev.veltrix.duels.command.commands.queue.subcommands.LeaveCommand;
import dev.veltrix.duels.config.CommandsConfig.CommandSettings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Objects;

public class QueueCommand extends BaseCommand {

    public QueueCommand(final DuelsPlugin plugin, final CommandSettings settings) {
        super(plugin, Objects.requireNonNull(settings, "settings").getName(), Permissions.QUEUE, true, settings.getAliasArray());
        child(
                new JoinCommand(plugin),
                new LeaveCommand(plugin)
        );
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;

        if (userManager.get(player) == null) {
            lang.sendMessage(sender, "ERROR.data.load-failure");
            return;
        }

        queueManager.getGui().open(player);
    }
}
