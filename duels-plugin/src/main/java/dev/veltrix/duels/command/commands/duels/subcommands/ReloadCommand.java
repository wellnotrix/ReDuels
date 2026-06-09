package dev.veltrix.duels.command.commands.duels.subcommands;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.util.Loadable;
import dev.veltrix.duels.util.Reloadable;
import dev.veltrix.duels.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class ReloadCommand extends BaseCommand {

    public ReloadCommand(final DuelsPlugin plugin) {
        super(plugin, "reload", null, null, 0, false, "rl");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        if (args.length > 0) {
            final Loadable target = plugin.find(args[0]);

            if (!(target instanceof Reloadable)) {
                sender.sendMessage(ChatColor.RED + "Invalid module. The following modules are available for a reload: " + StringUtil.join(plugin.getReloadables(), ", "));
                return;
            }

            final String name = target.getClass().getSimpleName();

            if (plugin.reload(target)) {
                sender.sendMessage(ChatColor.GREEN + "[" + plugin.getDescription().getFullName() + "] Successfully reloaded " + name + ".");
            } else {
                sender.sendMessage(ChatColor.RED + "An error occured while reloading " + name + "! Please check the console for more information.");
            }

            return;
        }

        if (plugin.reload()) {
            sender.sendMessage(ChatColor.GREEN + "[" + plugin.getDescription().getFullName() + "] Reload complete.");
        } else {
            sender.sendMessage(ChatColor.RED + "An error occured while reloading the plugin! Please check the console for more information.");
        }
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) {
            return handleTabCompletion(args[0], plugin.getReloadables());
        }

        return null;
    }
}
