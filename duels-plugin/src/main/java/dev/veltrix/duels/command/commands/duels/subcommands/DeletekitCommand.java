package dev.veltrix.duels.command.commands.duels.subcommands;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.util.StringUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class DeletekitCommand extends BaseCommand {

    public DeletekitCommand(final DuelsPlugin plugin) {
        super(plugin, "deletekit", "deletekit [name]", "Deletes a kit.", 1, false);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final String name = StringUtil.join(args, " ", 0, args.length).replace("-", " ");

        if (kitManager.remove(sender, name) == null) {
            lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
            return;
        }

        lang.sendMessage(sender, "COMMAND.duels.delete-kit", "name", name);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) {
            return handleTabCompletion(args[0], getKitNames());
        }

        return null;
    }
}
