package dev.veltrix.duels.command.commands.party.subcommands;

import java.util.ArrayList;
import java.util.List;
import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.Permissions;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.party.Party;
import dev.veltrix.duels.party.PartyMember;
import dev.veltrix.duels.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListCommand extends BaseCommand {
    
    public ListCommand(final DuelsPlugin plugin) {
        super(plugin, "list", null, null, Permissions.PARTY, 0, true, "ls");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final Party party;

        if (args.length > 0) {
            if (!sender.hasPermission(Permissions.PARTY_LIST_OTHERS)) {
                lang.sendMessage(sender, "ERROR.no-permission", "permission", Permissions.PARTY_LIST_OTHERS);
                return;
            }

            final Player target = Bukkit.getPlayerExact(args[0]);

            if (target == null || !player.canSee(target)) {
                lang.sendMessage(sender, "ERROR.player.not-found", "name", args[0]);
                return;
            }

            party = partyManager.get(target);

            if (party == null) {
                lang.sendMessage(sender, "ERROR.party.not-in-party.target", "name", target.getName());
                return;
            }

            showList(sender, party);
            return;
        }

        party = partyManager.get(player);

        if (party == null) {
            lang.sendMessage(sender, "ERROR.party.not-in-party.sender");
            return;
        }

        showList(sender, party);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1 && sender.hasPermission(Permissions.PARTY_LIST_OTHERS)) {
            return handleTabCompletion(args[0], getPlayerNames());
        }

        return null;
    }

    private void showList(final CommandSender sender, final Party party) {
        if (party == null) {
            lang.sendMessage(sender, "ERROR.party.not-in-party.sender");
            return;
        }

        final List<String> memberNames = new ArrayList<>(party.size());
        final List<String> onlineNames = new ArrayList<>();
        
        for (final PartyMember member : party.getMembers()) {
            memberNames.add(member.getName());

            if (member.isOnline()) {
                onlineNames.add(member.getName());
            }
        }

        lang.sendMessage(sender, "COMMAND.party.list",
            "members_count", memberNames.size(),
            "members", !memberNames.isEmpty() ? StringUtil.join(memberNames, ", ") : lang.getMessage("GENERAL.none"),
            "online_count", onlineNames.size(),
            "online_members", !onlineNames.isEmpty() ? StringUtil.join(onlineNames, ", ") : lang.getMessage("GENERAL.none"),
            "owner", party.getOwner().getName()
        );
    }
}
