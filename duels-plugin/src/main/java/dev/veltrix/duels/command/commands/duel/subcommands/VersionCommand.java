package dev.veltrix.duels.command.commands.duel.subcommands;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.Permissions;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.util.StringUtil;
import dev.veltrix.duels.util.TextBuilder;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

@SuppressWarnings("deprecation")
public class VersionCommand extends BaseCommand {

    public VersionCommand(final DuelsPlugin plugin) {
        super(plugin, "version", null, null, Permissions.DUEL, 1, true, "v");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final PluginDescriptionFile info = plugin.getDescription();
        TextBuilder
                .of(StringUtil.color("&b" + info.getFullName() + " by " + info.getAuthors().getFirst() + " &l[Click]"))
                .setClickEvent(Action.OPEN_URL, info.getWebsite())
                .send((Player) sender);
    }
}
