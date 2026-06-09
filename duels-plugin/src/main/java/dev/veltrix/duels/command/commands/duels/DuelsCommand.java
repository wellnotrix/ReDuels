package dev.veltrix.duels.command.commands.duels;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.Permissions;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.command.commands.duels.subcommands.*;
import dev.veltrix.duels.command.commands.duels.subcommands.*;
import dev.veltrix.duels.config.CommandsConfig.CommandSettings;
import org.bukkit.command.CommandSender;
import java.util.Objects;

public class DuelsCommand extends BaseCommand {

    public DuelsCommand(final DuelsPlugin plugin, final CommandSettings settings) {
        super(plugin, Objects.requireNonNull(settings, "settings").getName(), Permissions.ADMIN, false, settings.getAliasArray());
        child(
                new HelpCommand(plugin),
                new InfoCommand(plugin),
                new ListCommand(plugin),
                new ArenaCommand(plugin),
                new KitAdminCommand(plugin),
                new QueueAdminCommand(plugin),
                new SignCommand(plugin),
                new UserAdminCommand(plugin),
                new SystemCommand(plugin),
                new ReloadCommand(plugin) // Keep reload at top level too
        );
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        // Block placeholder-like arguments
        if (containsPlaceholder(args)) {
            lang.sendMessage(sender, "ERROR.command.invalid-argument", "arg", String.join(" ", args));
            return;
        }

        lang.sendMessage(sender, "COMMAND.duels.usage", "command", label);
    }

    private boolean containsPlaceholder(String[] args) {
        for (String arg : args) {
            if (arg.contains("%") || arg.contains("<") || arg.contains(">")) {
                return true;
            }
        }
        return false;
    }
}
