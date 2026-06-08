package dev.veltrix.duels.gui;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.core.arena.ArenaManagerImpl;
import dev.veltrix.duels.config.Config;
import dev.veltrix.duels.config.Lang;
import dev.veltrix.duels.core.kit.KitManagerImpl;
import dev.veltrix.duels.core.queue.QueueManager;
import dev.veltrix.duels.core.queue.sign.QueueSignManagerImpl;
import dev.veltrix.duels.core.request.RequestManager;
import dev.veltrix.duels.setting.SettingsManager;
import dev.veltrix.duels.core.spectate.SpectateManagerImpl;
import dev.veltrix.duels.util.gui.Button;
import org.bukkit.inventory.ItemStack;

public abstract class BaseButton extends Button<DuelsPlugin> {

    protected final Config config;
    protected final Lang lang;
    protected final KitManagerImpl kitManager;
    protected final ArenaManagerImpl arenaManager;
    protected final SettingsManager settingManager;
    protected final QueueManager queueManager;
    protected final QueueSignManagerImpl queueSignManager;
    protected final SpectateManagerImpl spectateManager;
    protected final RequestManager requestManager;

    protected BaseButton(final DuelsPlugin plugin, final ItemStack displayed) {
        super(plugin, displayed);
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.kitManager = plugin.getKitManager();
        this.arenaManager = plugin.getArenaManager();
        this.settingManager = plugin.getSettingManager();
        this.queueManager = plugin.getQueueManager();
        this.queueSignManager = plugin.getQueueSignManager();
        this.spectateManager = plugin.getSpectateManager();
        this.requestManager = plugin.getRequestManager();
    }
}
