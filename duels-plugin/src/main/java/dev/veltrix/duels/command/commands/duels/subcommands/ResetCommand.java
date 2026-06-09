package dev.veltrix.duels.command.commands.duels.subcommands;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.data.UserData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ResetCommand extends BaseCommand {

    public ResetCommand(final DuelsPlugin plugin) {
        super(plugin, "reset", "reset [name]", "Resets player's stats.", 1, false);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final UserData user = userManager.get(args[0]);

        if (user == null) {
            lang.sendMessage(sender, "ERROR.data.not-found", "name", args[0]);
            return;
        }

        user.reset();
        lang.sendMessage(sender, "COMMAND.duels.reset", "name", user.getName());
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) {
            return handleTabCompletion(args[0], getPlayerNames());
        }

        return null;
    }
}
