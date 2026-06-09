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

public class CreatequeueCommand extends BaseCommand {

    public CreatequeueCommand(final DuelsPlugin plugin) {
        super(plugin, "createqueue", "createqueue [name] [bet] [size] [-:kit]", "Creates a queue with given name, bet, team size and kit.", 4, false, "createq");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final String queueName = args[0];
        final int bet = NumberUtil.parseInt(args[1]).orElse(0);
        final int size = Math.max(1, NumberUtil.parseInt(args[2]).orElse(1));
        KitImpl kit = null;

        if (!args[3].equals("-")) {
            String kitName = StringUtil.join(args, " ", 3, args.length).replace("-", " ");
            kit = kitManager.get(kitName);

            if (kit == null) {
                lang.sendMessage(sender, "ERROR.kit.not-found", "name", kitName);
                return;
            }
        }

        final String kitName = kit != null ? kit.getName() : lang.getMessage("GENERAL.none");

        if (queueManager.create(sender, queueName, kit, bet, size) == null) {
            lang.sendMessage(sender, "ERROR.queue.already-exists", "name", queueName, "kit", kitName, "bet_amount", bet);
            return;
        }

        lang.sendMessage(sender, "COMMAND.duels.create-queue", "name", queueName, "kit", kitName, "bet_amount", bet);
        suggestNext(sender, "/duels queue list", "/duels queue info " + queueName);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) {
            return Arrays.asList("Name", "Example", "Example_2", "ANY_NAME");
        }

        if (args.length == 2) {
            return Arrays.asList("0", "10", "50", "100", "500", "1000");
        }

        if (args.length == 3) {
            return Arrays.asList("1", "2", "3", "4", "5");
        }

        if (args.length >= 4) {
            return handleTabCompletion(args[3], kitManager.getNames(true));
        }

        return null;
    }
}
