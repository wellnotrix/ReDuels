package dev.veltrix.duels.command.commands.duels;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.Permissions;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.command.commands.duels.subcommands.*;
import org.bukkit.command.CommandSender;

public class ArenaCommand extends BaseCommand {

    public ArenaCommand(final DuelsPlugin plugin) {
        super(plugin, "arena", Permissions.ADMIN, false, "arenas", "a");
        child(
                new CreateCommand(plugin),
                new DeleteCommand(plugin),
                new ListCommand(plugin),
                new InfoCommand(plugin),
                new SetCommand(plugin),
                new SetarenaitemCommand(plugin),
                new TeleportCommand(plugin),
                new EnableCommand(plugin),
                new DisableCommand(plugin),
                new ToggleCommand(plugin)
        );
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        lang.sendMessage(sender, "COMMAND.duels.help.arena", "command", label);
    }
}
