package dev.veltrix.duels.command.commands.party.subcommands;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.Permissions;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.party.Party;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class FriendlyfireCommand extends BaseCommand {

    public FriendlyfireCommand(final DuelsPlugin plugin) {
        super(plugin, "friendlyfire", null, null, Permissions.PARTY, 0, true, "ff");
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

        party.setFriendlyFire(!party.isFriendlyFire());
        lang.sendMessage(party.getOnlineMembers(), "COMMAND.party.friendly-fire." + (party.isFriendlyFire() ? "enabled" : "disabled"));
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return Collections.emptyList();
    }
}
