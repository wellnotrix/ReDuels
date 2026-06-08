package dev.veltrix.duels.core.betting;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.gui.betting.BettingGui;
import dev.veltrix.duels.hook.hooks.VaultHook;
import dev.veltrix.duels.setting.Settings;
import dev.veltrix.duels.util.Loadable;
import dev.veltrix.duels.util.gui.GuiListener;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class BettingManager implements Loadable, Listener {

    private final DuelsPlugin plugin;
    private final GuiListener<DuelsPlugin> guiListener;

    public BettingManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.guiListener = plugin.getGuiListener();
    }

    @Override
    public void handleLoad() {
        final VaultHook vaultHook = plugin.getHookManager().getHook(VaultHook.class);

        if (vaultHook == null) {
            DuelsPlugin.sendMessage("&bVault was not found! Money betting feature will be automatically disabled.");
        } else if (vaultHook.getEconomy() == null) {
            DuelsPlugin.sendMessage("&bEconomy plugin supporting Vault was not found! Money betting feature will be automatically disabled.");
        }
    }

    @Override
    public void handleUnload() {
    }

    public void open(final Settings settings, final Player sender, final Player target) {
        final BettingGui gui = new BettingGui(plugin, settings, sender, target);
        guiListener.addGui(sender, gui).open(sender);
        guiListener.addGui(target, gui).open(target);
    }
}
