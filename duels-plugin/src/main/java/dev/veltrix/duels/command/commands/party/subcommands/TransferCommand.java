package dev.veltrix.duels.command.commands.party.subcommands;

import dev.veltrix.duels.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.party.Party;

import java.util.List;

public class TransferCommand extends BaseCommand {
    
    public TransferCommand(final DuelsPlugin plugin) {
        super(plugin, "transfer", "transfer [player]", "Transfers the party ownership to another member of your party.", Permissions.PARTY, 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final Party party = partyManager.get(player);

        if (party == null) {
            lang.sendMessage(sender, "ERROR.party.not-in-party.sender");
            return;
        }

        if (!party.isOwner(player)) {
            lang.sendMessage(sender, "ERROR.party.is-not-owner");
            return;
        }

        final Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null || !player.canSee(target)) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[0]);
            return;
        }

        if (!party.isMember(target)) {
            lang.sendMessage(sender, "ERROR.party.not-a-member", "name", target.getName());
            return;
        }
        
        party.setOwner(target);
        lang.sendMessage(party.getOnlineMembers(), "COMMAND.party.transfer", "owner", player.getName(), "name", target.getName());
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) {
            return handleTabCompletion(args[0], getPlayerNames());
        }

        return null;
    }
}
