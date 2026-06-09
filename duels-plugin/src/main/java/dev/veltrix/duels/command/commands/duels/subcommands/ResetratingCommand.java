package dev.veltrix.duels.command.commands.duels.subcommands;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.data.UserData;
import dev.veltrix.duels.core.kit.KitImpl;
import dev.veltrix.duels.util.StringUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ResetratingCommand extends BaseCommand {

    public ResetratingCommand(final DuelsPlugin plugin) {
        super(plugin, "resetrating", "resetrating [name] [-:kit:all]", "Resets specified kit's rating or all.", 2, false, "resetr");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final UserData user = userManager.get(args[0]);

        if (user == null) {
            lang.sendMessage(sender, "ERROR.data.not-found", "name", args[0]);
            return;
        }

        if (args[1].equalsIgnoreCase("all")) {
            user.resetRating();
            kitManager.getKits().forEach(user::resetRating);
            lang.sendMessage(sender, "COMMAND.duels.reset-rating", "name", user.getName(), "kit", "all");
        } else if (args[1].equals("-")) {
            user.resetRating();
            lang.sendMessage(sender, "COMMAND.duels.reset-rating", "name", user.getName(), "kit", lang.getMessage("GENERAL.none"));
        } else {
            final String name = StringUtil.join(args, " ", 1, args.length).replace("-", " ");
            final KitImpl kit = kitManager.get(name);

            if (kit == null) {
                lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
                return;
            }

            user.resetRating(kit);
            lang.sendMessage(sender, "COMMAND.duels.reset-rating", "name", user.getName(), "kit", name);
        }
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) {
            return handleTabCompletion(args[0], getPlayerNames());
        }

        if (args.length == 2) {
            return handleTabCompletion(args[1], kitManager.getNames(true));
        }

        return null;
    }
}
