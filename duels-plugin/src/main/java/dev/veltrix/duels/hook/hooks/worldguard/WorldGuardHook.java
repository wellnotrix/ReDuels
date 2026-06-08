package dev.veltrix.duels.hook.hooks.worldguard;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.config.Config;
import dev.veltrix.duels.util.hook.PluginHook;
import org.bukkit.entity.Player;

import java.util.Collection;

public class WorldGuardHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "WorldGuard";

    private final Config config;
    private final WorldGuardHandler handler;

    public WorldGuardHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();
        this.handler = new WorldGuard7Handler();
    }

    public String findDuelZone(final Player player) {
        if (!config.isDuelzoneEnabled()) {
            return null;
        }

        final Collection<String> allowedRegions = config.getDuelzones();

        if (allowedRegions.isEmpty()) {
            return null;
        }

        return handler.findRegion(player, allowedRegions);
    }
}
