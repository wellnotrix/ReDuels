package dev.veltrix.duels.command.commands.duels.subcommands;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.core.kit.KitImpl;
import dev.veltrix.duels.util.NumberUtil;
import dev.veltrix.duels.util.StringUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class DeletequeueCommand extends BaseCommand {

    public DeletequeueCommand(final DuelsPlugin plugin) {
        super(plugin, "deletequeue", "deletequeue [bet] [-:kit]", "Deletes a queue.", 2, false, "delqueue", "delq");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final int bet = NumberUtil.parseInt(args[0]).orElse(0);
        KitImpl kit = null;

        if (!args[1].equals("-")) {
            String name = StringUtil.join(args, " ", 1, args.length).replace("-", " ");
            kit = kitManager.get(name);

            if (kit == null) {
                lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
                return;
            }
        }

        final String kitName = kit != null ? kit.getName() : lang.getMessage("GENERAL.none");

        if (queueManager.remove(sender, kit, bet) == null) {
            lang.sendMessage(sender, "ERROR.queue.not-found", "bet_amount", bet, "kit", kitName);
            return;
        }

        lang.sendMessage(sender, "COMMAND.duels.delete-queue", "kit", kitName, "bet_amount", bet);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) {
            return Arrays.asList("0", "10", "50", "100", "500", "1000");
        }

        if (args.length >= 2) {
            return handleTabCompletion(args[1], kitManager.getNames(true));
        }

        return null;
    }
}
