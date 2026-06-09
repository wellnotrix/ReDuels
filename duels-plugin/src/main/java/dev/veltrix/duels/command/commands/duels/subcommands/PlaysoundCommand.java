package dev.veltrix.duels.command.commands.duels.subcommands;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.config.Config.MessageSound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class PlaysoundCommand extends BaseCommand {

    public PlaysoundCommand(final DuelsPlugin plugin) {
        super(plugin, "playsound", "playsound [name]", "Plays the selected sound if defined.", 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final MessageSound sound = config.getSound(args[0]);

        if (sound == null) {
            lang.sendMessage(sender, "ERROR.sound.not-found", "name", args[0]);
            return;
        }

        final Player player = (Player) sender;
        player.playSound(player.getLocation(), sound.getType(), sound.getVolume(), sound.getPitch());
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) {
            return handleTabCompletion(args[0], config.getSounds());
        }

        return null;
    }
}
