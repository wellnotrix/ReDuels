package dev.veltrix.duels.hook.hooks;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.config.Config;
import dev.veltrix.duels.data.UserData;
import dev.veltrix.duels.data.UserManagerImpl;
import dev.veltrix.duels.util.hook.PluginHook;
import me.robin.leaderheads.datacollectors.OnlineDataCollector;
import me.robin.leaderheads.objects.BoardType;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class LeaderHeadsHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "LeaderHeads";

    private final Config config;
    private final UserManagerImpl userManager;

    public LeaderHeadsHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();
        this.userManager = plugin.getUserManager();

        // Wait for config to load
        plugin.doSyncAfter(() -> {
            new DuelWinsCollector(config.getLhWinsTitle(), config.getLhWinsCmd());
            new DuelLossesCollector(config.getLhLossesTitle(), config.getLhLossesCmd());
        }, 1L);
    }

    public class DuelWinsCollector extends OnlineDataCollector {

        DuelWinsCollector(final String title, final String command) {
            super("duels-wins", "Duels", BoardType.DEFAULT, title, command, Arrays.asList(null, null, null, null));
        }

        @Override
        public Double getScore(final Player player) {
            final UserData user;
            return (user = userManager.get(player)) != null ? user.getWins() : 0.0;
        }
    }

    public class DuelLossesCollector extends OnlineDataCollector {

        DuelLossesCollector(final String title, final String command) {
            super("duels-losses", "Duels", BoardType.DEFAULT, title, command, Arrays.asList(null, null, null, null));
        }

        @Override
        public Double getScore(final Player player) {
            final UserData user;
            return (user = userManager.get(player)) != null ? user.getLosses() : 0.0;
        }
    }
}
