package dev.veltrix.duels.core.validator.validators.request.target;

import java.util.Collection;
import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.party.Party;
import dev.veltrix.duels.util.function.Pair;
import dev.veltrix.duels.core.validator.BaseTriValidator;
import org.bukkit.entity.Player;


public class TargetCheckMatchValidator extends BaseTriValidator<Pair<Player, Player>, Party, Collection<Player>> {
    
    private static final String MESSAGE_KEY = "ERROR.duel.already-in-match.target";
    private static final String PARTY_MESSAGE_KEY = "ERROR.party-duel.already-in-match.target";

    public TargetCheckMatchValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Pair<Player, Player> pair, final Party party, final Collection<Player> players) {
        if (players.stream().anyMatch(player -> arenaManager.isInMatch(player))) {
            lang.sendMessage(pair.getKey(), party != null ? PARTY_MESSAGE_KEY : MESSAGE_KEY, "name", pair.getValue().getName());
            return false;
        }
        
        return true;
    }
}
