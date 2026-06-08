package dev.veltrix.duels.core.validator.validators.request.target;

import java.util.Collection;
import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.party.Party;
import dev.veltrix.duels.util.function.Pair;
import dev.veltrix.duels.core.validator.BaseTriValidator;
import org.bukkit.entity.Player;

public class TargetCheckSelfValidator extends BaseTriValidator<Pair<Player, Player>, Party, Collection<Player>> {

    public TargetCheckSelfValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Pair<Player, Player> pair, final Party party, final Collection<Player> players) {
        if (pair.getKey().equals(pair.getValue())) {
            lang.sendMessage(pair.getKey(), "ERROR.duel.is-self");
            return false;
        }

        return true;
    }

    
}