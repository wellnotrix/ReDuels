package dev.veltrix.duels.command.commands.duels;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.Permissions;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.command.commands.duels.subcommands.*;
import org.bukkit.command.CommandSender;

public class SignCommand extends BaseCommand {

    public SignCommand(final DuelsPlugin plugin) {
        super(plugin, "sign", Permissions.ADMIN, false, "signs", "s");
        child(
                new AddsignCommand(plugin),
                new DeletesignCommand(plugin)
        );
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        lang.sendMessage(sender, "COMMAND.duels.help.sign", "command", label);
    }
}
