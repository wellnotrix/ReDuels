package dev.veltrix.duels.command;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.core.arena.ArenaManagerImpl;
import dev.veltrix.duels.core.betting.BettingManager;
import dev.veltrix.duels.config.Config;
import dev.veltrix.duels.config.Lang;
import dev.veltrix.duels.data.UserManagerImpl;
import dev.veltrix.duels.core.DuelManager;
import dev.veltrix.duels.hook.HookManager;
import dev.veltrix.duels.inventories.InventoryManager;
import dev.veltrix.duels.core.kit.KitManagerImpl;
import dev.veltrix.duels.party.PartyManagerImpl;
import dev.veltrix.duels.core.player.PlayerInfoManager;
import dev.veltrix.duels.core.queue.QueueManager;
import dev.veltrix.duels.core.queue.sign.QueueSignManagerImpl;
import dev.veltrix.duels.core.request.RequestManager;
import dev.veltrix.duels.setting.SettingsManager;
import dev.veltrix.duels.core.spectate.SpectateManagerImpl;
import dev.veltrix.duels.util.command.AbstractCommand;
import dev.veltrix.duels.core.validator.ValidatorManager;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseCommand extends AbstractCommand<DuelsPlugin> {

    protected final DuelsPlugin plugin;
    protected final Config config;
    protected final Lang lang;
    protected final UserManagerImpl userManager;
    protected final KitManagerImpl kitManager;
    protected final ArenaManagerImpl arenaManager;
    protected final QueueManager queueManager;
    protected final QueueSignManagerImpl queueSignManager;
    protected final SettingsManager settingManager;
    protected final PlayerInfoManager playerManager;
    protected final SpectateManagerImpl spectateManager;
    protected final BettingManager bettingManager;
    protected final InventoryManager inventoryManager;
    protected final DuelManager duelManager;
    protected final RequestManager requestManager;
    protected final HookManager hookManager;
    protected final PartyManagerImpl partyManager;
    protected final ValidatorManager validatorManager;

    /**
     * Constructor for a sub command
     */
    protected BaseCommand(final DuelsPlugin plugin, final String name, final String usage, final String description, final String permission, final int length,
                          final boolean playerOnly, final String... aliases) {
        super(plugin, name, usage, description, permission, length, playerOnly, aliases);
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.userManager = plugin.getUserManager();
        this.partyManager = plugin.getPartyManager();
        this.kitManager = plugin.getKitManager();
        this.arenaManager = plugin.getArenaManager();
        this.queueManager = plugin.getQueueManager();
        this.queueSignManager = plugin.getQueueSignManager();
        this.settingManager = plugin.getSettingManager();
        this.playerManager = plugin.getPlayerManager();
        this.spectateManager = plugin.getSpectateManager();
        this.bettingManager = plugin.getBettingManager();
        this.inventoryManager = plugin.getInventoryManager();
        this.duelManager = plugin.getDuelManager();
        this.requestManager = plugin.getRequestManager();
        this.hookManager = plugin.getHookManager();
        this.validatorManager = plugin.getValidatorManager();
    }

    /**
     * Constructor for a sub command, inherits parent permission
     */
    protected BaseCommand(final DuelsPlugin plugin, final String name, final String usage, final String description, final int length, final boolean playerOnly,
                          final String... aliases) {
        this(plugin, name, usage, description, null, length, playerOnly, aliases);
    }

    /**
     * Constructor for a parent command
     */
    protected BaseCommand(final DuelsPlugin plugin, final String name, final String permission, final boolean playerOnly, final String... aliases) {
        this(plugin, name, null, null, permission, -1, playerOnly, aliases);
    }

    @Override
    protected void handleMessage(final CommandSender sender, final MessageType type, final String... args) {
        switch (type) {
            case PLAYER_ONLY:
                super.handleMessage(sender, type, args);
                break;
            case NO_PERMISSION:
                lang.sendMessage(sender, "ERROR.no-permission", "permission", args[0]);
                break;
            case SUB_COMMAND_INVALID:
                lang.sendMessage(sender, "ERROR.command.invalid-sub-command", "command", args[0], "argument", args[1]);
                break;
            case SUB_COMMAND_USAGE:
                if (sender instanceof org.bukkit.entity.Player) {
                    final String rawUsage = lang.getMessage("COMMAND.sub-command-usage", "command", args[0], "usage", args[1], "description", args[2]);
                    final String command = "/" + args[0] + " " + args[1].split(" \\[")[0].split(" <")[0];
                    dev.veltrix.duels.util.TextBuilder.of(dev.veltrix.duels.util.StringUtil.color(rawUsage))
                            .setClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.SUGGEST_COMMAND, command)
                            .setHoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, dev.veltrix.duels.util.StringUtil.color("&7Click to suggest command:\n&f" + command))
                            .send((org.bukkit.entity.Player) sender);
                } else {
                    lang.sendMessage(sender, "COMMAND.sub-command-usage", "command", args[0], "usage", args[1], "description", args[2]);
                }
                break;
        }
    }

    protected List<String> handleTabCompletion(final String argument, final Collection<String> collection) {
        if (argument == null || collection == null) return java.util.Collections.emptyList();
        
        final String lowerArg = argument.toLowerCase();
        return collection.stream()
                .filter(value -> value != null && value.toLowerCase().startsWith(lowerArg))
                .map(value -> value.replace(" ", "-"))
                .sorted()
                .collect(Collectors.toList());
    }

    protected void suggestNext(final CommandSender sender, final String... steps) {
        if (steps == null || steps.length == 0) return;
        
        lang.sendMessage(sender, "COMMAND.next-steps-header");
        if (sender instanceof org.bukkit.entity.Player) {
            final org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
            for (final String step : steps) {
                dev.veltrix.duels.util.TextBuilder.of(lang.getMessage("COMMAND.next-step", "step", step))
                        .setClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.SUGGEST_COMMAND, step)
                        .setHoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, "&7Click to suggest command:\n&f" + step)
                        .send(player);
            }
        } else {
            for (final String step : steps) {
                lang.sendMessage(sender, "COMMAND.next-step", "step", step);
            }
        }
    }

    protected List<String> getPlayerNames() {
        return org.bukkit.Bukkit.getOnlinePlayers().stream()
                .map(org.bukkit.entity.Player::getName)
                .collect(Collectors.toList());
    }

    protected List<String> getArenaNames() {
        return arenaManager.getNames();
    }

    protected List<String> getKitNames() {
        return kitManager.getNames(false);
    }

    protected List<String> getQueueNames() {
        return queueManager.getQueueNames();
    }
}
