package dev.veltrix.duels.command.commands.duels.subcommands;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.api.kit.Kit;
import dev.veltrix.duels.api.queue.DQueue;
import dev.veltrix.duels.core.arena.ArenaImpl;
import dev.veltrix.duels.command.BaseCommand;
import dev.veltrix.duels.core.queue.sign.QueueSignImpl;
import dev.veltrix.duels.util.StringUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListCommand extends BaseCommand {

    public ListCommand(final DuelsPlugin plugin) {
        super(plugin, "list", null, null, 0, false, "ls");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        if (sender instanceof org.bukkit.entity.Player) {
            final org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
            player.sendMessage(dev.veltrix.duels.util.StringUtil.color(lang.getMessage("STRINGS.LINE")));
            
            // Arenas
            final dev.veltrix.duels.util.TextBuilder arenasBuilder = dev.veltrix.duels.util.TextBuilder.of("&7Arenas (&f" + arenaManager.getArenasImpl().size() + "&7): ");
            final List<dev.veltrix.duels.core.arena.ArenaImpl> arenas = new ArrayList<>(arenaManager.getArenasImpl());
            for (int i = 0; i < arenas.size(); i++) {
                final dev.veltrix.duels.core.arena.ArenaImpl arena = arenas.get(i);
                final String name = arena.getName();
                final String color = "&" + getColor(arena);
                
                arenasBuilder.add(color + name, 
                        net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/duels arena info " + name,
                        net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
                        "&eArena: &f" + name + "\n" +
                        "&eStatus: &r" + (arena.isDisabled() ? "&cDisabled" : (arena.isUsed() ? "&cIn Use" : "&aAvailable")) + "\n" +
                        "&eSpawnpoints: &f" + arena.getPositions().size() + "/2\n\n" +
                        "&7Click for more info."
                );
                
                if (i < arenas.size() - 1) {
                    arenasBuilder.add("&7, ");
                }
            }
            arenasBuilder.send(player);

            // Kits
            final dev.veltrix.duels.util.TextBuilder kitsBuilder = dev.veltrix.duels.util.TextBuilder.of("&7Kits (&f" + kitManager.getKits().size() + "&7): ");
            final List<Kit> kits = kitManager.getKits();
            for (int i = 0; i < kits.size(); i++) {
                final Kit kit = kits.get(i);
                final String name = kit.getName();
                
                kitsBuilder.add("&3" + name, 
                        net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/duels kit options " + name,
                        net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
                        "&bKit: &f" + name + "\n" +
                        "&7Click to configure options."
                );
                
                if (i < kits.size() - 1) {
                    kitsBuilder.add("&7, ");
                }
            }
            kitsBuilder.send(player);

            // Queues & Signs
            player.sendMessage(dev.veltrix.duels.util.StringUtil.color("&7Queues: &b" + queueManager.getQueues().size() + " &7| Queue Signs: &f" + queueSignManager.getSigns().size()));
            player.sendMessage(dev.veltrix.duels.util.StringUtil.color("&7Lobby: &f" + dev.veltrix.duels.util.StringUtil.parse(playerManager.getLobby())));
            player.sendMessage(dev.veltrix.duels.util.StringUtil.color(lang.getMessage("STRINGS.LINE")));
            return;
        }

        // Console fallback
        final List<String> arenas = new ArrayList<>();
        arenaManager.getArenasImpl().forEach(arena -> arenas.add("&" + getColor(arena) + arena.getName()));
        final String kits = StringUtil.join(kitManager.getKits().stream().map(Kit::getName).collect(Collectors.toList()), ", ");
        final String queues = StringUtil.join(queueManager.getQueues().stream().map(dev.veltrix.duels.api.queue.DQueue::toString).collect(Collectors.toList()), ", ");
        final String signs = StringUtil.join(queueSignManager.getSigns().stream().map(dev.veltrix.duels.core.queue.sign.QueueSignImpl::toString).collect(Collectors.toList()), ", ");
        lang.sendMessage(sender, "COMMAND.duels.list",
                "arenas", !arenas.isEmpty() ? StringUtil.join(arenas, "&r, &r") : lang.getMessage("GENERAL.none"),
                "kits", !kits.isEmpty() ? kits : lang.getMessage("GENERAL.none"),
                "queues", !queues.isEmpty() ? queues : lang.getMessage("GENERAL.none"),
                "queue_signs", !signs.isEmpty() ? signs : lang.getMessage("GENERAL.none"),
                "lobby", StringUtil.parse(playerManager.getLobby()));
    }

    private String getColor(final ArenaImpl arena) {
        return arena.isDisabled() ? "4" : (arena.getPositions().size() < 2 ? "9" : arena.isUsed() ? "c" : "a");
    }
}