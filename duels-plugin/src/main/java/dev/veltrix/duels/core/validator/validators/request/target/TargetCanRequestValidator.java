package dev.veltrix.duels.core.validator.validators.request.target;

import java.util.Collection;
import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.party.Party;
import dev.veltrix.duels.util.function.Pair;
import dev.veltrix.duels.core.validator.BaseTriValidator;
import org.bukkit.entity.Player;
import dev.veltrix.duels.data.UserData;
import dev.veltrix.duels.Permissions;

public class TargetCanRequestValidator extends BaseTriValidator<Pair<Player, Player>, Party, Collection<Player>> {
    
    public TargetCanRequestValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Pair<Player, Player> pair, final Party party, final Collection<Player> players) {
        final UserData user = userManager.get(pair.getValue());

        if (user == null) {
            lang.sendMessage(pair.getKey(), "ERROR.data.not-found", "name", pair.getValue().getName());
            return false;
        }

        if (!pair.getKey().hasPermission(Permissions.ADMIN) && !user.canRequest()) {
            lang.sendMessage(pair.getKey(), "ERROR.duel.requests-disabled", "name",  pair.getValue().getName());
            return false;
        }

        return true;
    }

}
