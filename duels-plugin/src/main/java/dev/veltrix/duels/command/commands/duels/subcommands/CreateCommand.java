package dev.veltrix.duels.command.commands.duels.subcommands;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.util.StringUtil;
import org.bukkit.command.CommandSender;

public class CreateCommand extends BaseCommand {

    public CreateCommand(final DuelsPlugin plugin) {
        super(plugin, "create", "create [name]", "Creates an arena with given name.", 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final String name = StringUtil.join(args, " ", 0, args.length);

        if (!StringUtil.isAlphanumeric(name)) {
            lang.sendMessage(sender, "ERROR.command.name-not-alphanumeric", "name", name);
            return;
        }

        if (!arenaManager.create(sender, name)) {
            lang.sendMessage(sender, "ERROR.arena.already-exists", "name", name);
            return;
        }

        lang.sendMessage(sender, "COMMAND.duels.create", "name", name);
        suggestNext(sender, "/duels arena set " + name + " 1", "/duels arena set " + name + " 2");
    }
}
