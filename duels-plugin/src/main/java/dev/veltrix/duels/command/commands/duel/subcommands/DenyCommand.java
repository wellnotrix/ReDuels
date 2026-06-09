package dev.veltrix.duels.command.commands.duel.subcommands;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.api.event.request.RequestDenyEvent;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.core.request.RequestImpl;
import dev.veltrix.duels.util.function.Pair;
import dev.veltrix.duels.util.validator.ValidatorUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class DenyCommand extends BaseCommand {

    public DenyCommand(final DuelsPlugin plugin) {
        super(plugin, "deny", "deny [player]", "Declines a duel request.", 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null || !player.canSee(target)) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[0]);
            return;
        }

        if (!ValidatorUtil.validate(validatorManager.getDuelDenyTargetValidators(), new Pair<>(player, target), partyManager.get(target), Collections.emptyList())) {
            return;
        }

        final RequestImpl request = requestManager.remove(target, player);
        final RequestDenyEvent event = new RequestDenyEvent(player, target, request);
        Bukkit.getPluginManager().callEvent(event);

        if (request.isPartyDuel()) {
            final Player targetPartyLeader = request.getTargetParty().getOwner().getPlayer();
            final Player senderPartyLeader = request.getSenderParty().getOwner().getPlayer();
            lang.sendMessage(Collections.singleton(senderPartyLeader), "COMMAND.duel.party-request.deny.receiver-party", "owner", player.getName(), "name", target.getName());
            lang.sendMessage(targetPartyLeader, "COMMAND.duel.party-request.deny.sender-party", "owner", target.getName(), "name", player.getName());
        } else {
            lang.sendMessage(player, "COMMAND.duel.request.deny.receiver", "name", target.getName());
            lang.sendMessage(target, "COMMAND.duel.request.deny.sender", "name", player.getName());
        }
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) {
            return handleTabCompletion(args[0], getPlayerNames());
        }

        return Collections.emptyList();
    }
}
