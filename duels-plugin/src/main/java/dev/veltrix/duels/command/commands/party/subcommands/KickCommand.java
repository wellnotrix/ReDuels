package dev.veltrix.duels.command.commands.party.subcommands;

import dev.veltrix.duels.Permissions;
import dev.veltrix.duels.party.PartyMember;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.party.Party;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class KickCommand extends BaseCommand {
    
    public KickCommand(final DuelsPlugin plugin) {
        super(plugin, "kick", "kick [player]", "Kicks a player from your party.", Permissions.PARTY, 1, true, "remove");
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
        
        final PartyMember member = party.get(args[0]);

        if (member == null) {
            lang.sendMessage(sender, "ERROR.party.not-a-member", "name", args[0]);
            return;
        }
        
        if (member.getUuid().equals(player.getUniqueId())) {
            lang.sendMessage(sender, "ERROR.party.kick-self");
            return;
        }

        partyManager.remove(member, party);

        final Player target = member.getPlayer();

        if (target != null) {
            lang.sendMessage(target, "COMMAND.party.kick.receiver", "owner", player.getName());
        }

        lang.sendMessage(party.getOnlineMembers(), "COMMAND.party.kick.members", "owner", player.getName(), "name", member.getName());
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1 && sender instanceof Player) {
            final Party party = partyManager.get((Player) sender);

            if (party != null) {
                return handleTabCompletion(args[0], party.getMembers().stream().map(PartyMember::getName).collect(Collectors.toList()));
            }
        }

        return Collections.emptyList();
    }
}
