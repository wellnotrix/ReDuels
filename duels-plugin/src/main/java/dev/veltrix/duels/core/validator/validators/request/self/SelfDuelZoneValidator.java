package dev.veltrix.duels.core.validator.validators.request.self;

import java.util.Collection;
import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.hook.hooks.worldguard.WorldGuardHook;
import dev.veltrix.duels.party.Party;
import dev.veltrix.duels.core.validator.BaseTriValidator;
import org.bukkit.entity.Player;

public class SelfDuelZoneValidator extends BaseTriValidator<Player, Party, Collection<Player>> {
    
    private static final String MESSAGE_KEY = "ERROR.duel.not-in-duelzone";
    private static final String PARTY_MESSAGE_KEY = "ERROR.party-duel.not-in-duelzone";

    private final WorldGuardHook worldGuard;

    public SelfDuelZoneValidator(final DuelsPlugin plugin) {
        super(plugin);
        this.worldGuard = plugin.getHookManager().getHook(WorldGuardHook.class);
    }

    @Override
    public boolean shouldValidate() {
        return config.isDuelzoneEnabled() && worldGuard != null;
    }

    @Override
    public boolean validate(final Player sender, final Party party, final Collection<Player> players) {
        if (players.stream().anyMatch(player -> worldGuard.findDuelZone(player) == null)) {
            lang.sendMessage(sender, party != null ? PARTY_MESSAGE_KEY : MESSAGE_KEY, "regions", config.getDuelzones());
        }

        return true;
    }
}
