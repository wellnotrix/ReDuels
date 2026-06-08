package dev.veltrix.duels.command.commands.duels.subcommands;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetKitLobbyCommand extends BaseCommand {

    public SetKitLobbyCommand(final DuelsPlugin plugin) {
        super(plugin, "setkitlobby", null, null, 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        if (!playerManager.setKitLobby((Player) sender)) {
            lang.sendMessage(sender, "ERROR.command.lobby-save-failure");
            return;
        }

        lang.sendMessage(sender, "COMMAND.duels.set-lobby");
    }
}
