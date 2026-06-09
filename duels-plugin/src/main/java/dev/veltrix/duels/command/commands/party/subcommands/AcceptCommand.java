package dev.veltrix.duels.command.commands.party.subcommands;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.Permissions;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.party.Party;
import dev.veltrix.duels.party.PartyInvite;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class AcceptCommand extends BaseCommand {
    
    public AcceptCommand(final DuelsPlugin plugin) {
        super(plugin, "accept", "accept [player]", "Accepts a party invitation.", Permissions.PARTY, 1, true, "a");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;

        if (partyManager.isInParty(player)) {
            lang.sendMessage(sender, "ERROR.party.already-in-party.sender");
            return;
        }

        final Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null || !player.canSee(target)) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[0]);
            return;
        }

        final PartyInvite invite = partyManager.removeInvite(target, player);

        if (invite == null) {
            lang.sendMessage(sender, "ERROR.party.no-invite", "name", target.getName());
            return;
        }

        final Party party = invite.getParty();

        if (party.isRemoved()) {
            lang.sendMessage(sender, "ERROR.party.not-found");
            return;
        }

        if (party.size() >= config.getPartyMaxSize()) {
            lang.sendMessage(sender, "ERROR.party.max-size-reached.target", "name", target.getName());
            return;
        }
        
        lang.sendMessage(player, "COMMAND.party.invite.accept.receiver", "name", target.getName());
        lang.sendMessage(party.getOnlineMembers(), "COMMAND.party.invite.accept.members", "name", player.getName());
        partyManager.join(player, party);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) {
            return handleTabCompletion(args[0], getPlayerNames());
        }

        return Collections.emptyList();
    }
}
