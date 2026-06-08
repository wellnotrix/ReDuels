package dev.veltrix.duels.hook.hooks;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.core.arena.ArenaManagerImpl;
import dev.veltrix.duels.config.Config;
import dev.veltrix.duels.util.hook.PluginHook;
import me.jadenp.notbounties.bounty_events.BountyClaimEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NotBountiesHook extends PluginHook<DuelsPlugin> implements Listener {

    public static final String NAME = "NotBounties";

    private final Config config;
    private final ArenaManagerImpl arenaManager;

    public NotBountiesHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();
        this.arenaManager = plugin.getArenaManager();

        try{
            Class.forName("me.jadenp.notbounties.bounty_events.BountyClaimEvent");
        }catch(ClassNotFoundException e){
            throw new RuntimeException("This version of " + getName() + " is not supported. Please try upgrading to the latest version.");
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void on(final BountyClaimEvent event) {
        if (!config.isPreventBountyLossByNotBounties() || !arenaManager.isInMatch(event.getKiller())) {
            return;
        }

        event.setCancelled(true);
    }

}
