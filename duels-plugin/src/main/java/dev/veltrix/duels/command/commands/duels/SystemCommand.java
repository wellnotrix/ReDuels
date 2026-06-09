package dev.veltrix.duels.command.commands.duels;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.Permissions;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.command.commands.duels.subcommands.*;
import org.bukkit.command.CommandSender;

public class SystemCommand extends BaseCommand {

    public SystemCommand(final DuelsPlugin plugin) {
        super(plugin, "system", Permissions.ADMIN, false, "sys");
        child(
                new ReloadCommand(plugin),
                new SetlobbyCommand(plugin),
                new LobbyCommand(plugin),
                new PlaysoundCommand(plugin),
                new SetKitLobbyCommand(plugin)
        );
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        lang.sendMessage(sender, "COMMAND.duels.help.extra", "command", label);
    }
}
