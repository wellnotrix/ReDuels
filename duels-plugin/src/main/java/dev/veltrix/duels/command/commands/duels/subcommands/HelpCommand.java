package dev.veltrix.duels.command.commands.duels.subcommands;

import com.google.common.collect.Lists;
import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.command.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand extends BaseCommand {

    private final List<String> categories = Lists.newArrayList("arena", "kit", "queue", "sign", "user", "extra");

    public HelpCommand(final DuelsPlugin plugin) {
        super(plugin, "help", null, null, 0, false, "h");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        if (args.length == 0 || !categories.contains(args[0].toLowerCase())) {
            if (sender instanceof org.bukkit.entity.Player) {
                sendMainHelp((org.bukkit.entity.Player) sender, label);
            } else {
                lang.sendMessage(sender, "COMMAND.duels.usage", "command", label);
            }
            return;
        }

        final String category = args[0].toLowerCase();

        final List<String> rawMessages = lang.getMessages("COMMAND.duels.help." + category);
        
        if (sender instanceof org.bukkit.entity.Player) {
            final org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
            for (String rawMessage : rawMessages) {

                String message = rawMessage.replace("%command%", label);
                
                if (message.contains("/" + label)) {
                    final String command = extractCommand(message, label);
                    dev.veltrix.duels.util.TextBuilder.of(dev.veltrix.duels.util.StringUtil.color(message))
                            .setClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.SUGGEST_COMMAND, command)
                            .setHoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, dev.veltrix.duels.util.StringUtil.color("&7Click to suggest command:\n&f" + command))
                            .send(player);
                } else {
                    player.sendMessage(dev.veltrix.duels.util.StringUtil.color(message));
                }
            }
        } else {
            lang.sendMessage(sender, "COMMAND.duels.help." + category, "command", label);
        }
    }

    private void sendMainHelp(org.bukkit.entity.Player player, String label) {
        final List<String> header = lang.getMessages("COMMAND.duels.usage");
        for (String rawLine : header) {
            String line = rawLine.replace("%command%", label);
            if (line.contains(" help ")) {
                final String category = extractCategory(line);
                final String command = "/" + label + " help " + category;
                dev.veltrix.duels.util.TextBuilder.of(dev.veltrix.duels.util.StringUtil.color(line))
                        .setClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, command)
                        .setHoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, dev.veltrix.duels.util.StringUtil.color("&7Click to view &f" + category + " &7help."))
                        .send(player);
            } else {
                player.sendMessage(dev.veltrix.duels.util.StringUtil.color(line));
            }
        }
    }

    private String extractCommand(String message, String label) {

        int start = message.indexOf("/" + label);
        int end = message.indexOf(" ", message.indexOf(" ", start) + 1); // Get command + subcommand
        if (end == -1) end = message.length();

        String cmd = message.substring(start, end).replaceAll("&[0-9a-fk-or]", "").trim();
        return cmd.split(" \\[")[0].split(" <")[0];
    }

    private String extractCategory(String line) {
        for (String cat : categories) {
            if (line.contains(" " + cat)) return cat;
        }
        return "extra";
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) {
            return categories.stream()
                    .filter(category -> category.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return null;
    }
}
