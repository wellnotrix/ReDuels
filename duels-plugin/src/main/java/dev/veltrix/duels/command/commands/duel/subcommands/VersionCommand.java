package dev.veltrix.duels.command.commands.duel.subcommands;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.Permissions;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.util.StringUtil;
import dev.veltrix.duels.util.TextBuilder;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("deprecation")
public class VersionCommand extends BaseCommand {

    public VersionCommand(final DuelsPlugin plugin) {
        super(plugin, "version", null, null, Permissions.DUEL, 0, true, "v");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final PluginDescriptionFile info = plugin.getDescription();
        TextBuilder
                .of(StringUtil.color("&b" + info.getFullName() + " by " + info.getAuthors().getFirst() + " &l[Click]"))
                .setClickEvent(Action.OPEN_URL, info.getWebsite())
                .send((Player) sender);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return Collections.emptyList();
    }
}
