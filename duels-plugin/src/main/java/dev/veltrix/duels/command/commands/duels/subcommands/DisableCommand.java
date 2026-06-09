package dev.veltrix.duels.command.commands.duels.subcommands;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.core.arena.ArenaImpl;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.util.StringUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class DisableCommand extends BaseCommand {


    public DisableCommand(final DuelsPlugin plugin) {
        super(plugin, "disable", "disable [name]", "Disables an arena.", 1, false);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final String name = StringUtil.join(args, " ", 0, args.length).replace("-", " ");
        final ArenaImpl arena = arenaManager.get(name);

        if (arena == null) {
            lang.sendMessage(sender, "ERROR.arena.not-found", "name", name);
            return;
        }

        if (arena.isDisabled()) {
            lang.sendMessage(sender, "COMMAND.duels.already-disabled", "name", name);
            return;
        }

        arena.setDisabled(sender, true);
        lang.sendMessage(sender, "COMMAND.duels.disable", "name", name);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) {
            return handleTabCompletion(args[0], getArenaNames());
        }

        return null;
    }
}