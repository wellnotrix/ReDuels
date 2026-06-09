package dev.veltrix.duels.command.commands.duels.subcommands;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.gui.bind.BindGui;
import dev.veltrix.duels.core.kit.KitImpl;
import dev.veltrix.duels.util.StringUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BindCommand extends BaseCommand {

    public BindCommand(final DuelsPlugin plugin) {
        super(plugin, "bind", "bind [kit]", "Opens the arena bind gui for kit.", 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final String name = StringUtil.join(args, " ", 0, args.length).replace("-", " ");
        final KitImpl kit = kitManager.get(name);

        if (kit == null) {
            lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
            return;
        }

        final Player player = (Player) sender;
        plugin.getGuiListener().addGui(player, new BindGui(plugin, kit), true).open(player);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) {
            return handleTabCompletion(args[0], getKitNames());
        }

        return null;
    }
}
