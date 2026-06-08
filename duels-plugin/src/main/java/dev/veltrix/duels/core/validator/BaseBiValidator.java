package dev.veltrix.duels.core.validator;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.core.arena.ArenaManagerImpl;
import dev.veltrix.duels.config.Config;
import dev.veltrix.duels.config.Lang;
import dev.veltrix.duels.data.UserManagerImpl;
import dev.veltrix.duels.party.PartyManagerImpl;
import dev.veltrix.duels.core.request.RequestManager;
import dev.veltrix.duels.core.spectate.SpectateManagerImpl;
import dev.veltrix.duels.util.validator.BiValidator;

public abstract class BaseBiValidator<T1, T2> implements BiValidator<T1, T2> {

    protected final DuelsPlugin plugin;

    protected final Config config;
    protected final Lang lang;
    protected final UserManagerImpl userManager;
    protected final PartyManagerImpl partyManager;
    protected final ArenaManagerImpl arenaManager;
    protected final SpectateManagerImpl spectateManager;
    protected final RequestManager requestManager;

    public BaseBiValidator(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.userManager = plugin.getUserManager();
        this.partyManager = plugin.getPartyManager();
        this.arenaManager = plugin.getArenaManager();
        this.spectateManager = plugin.getSpectateManager();
        this.requestManager = plugin.getRequestManager();
    }

    @Override
    public boolean shouldValidate() {
        return true;
    }
}
