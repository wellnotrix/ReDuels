package dev.veltrix.duels.core.validator.validators.request.self;

import java.util.Collection;
import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.party.Party;
import dev.veltrix.duels.util.DateUtil;
import dev.veltrix.duels.core.validator.BaseTriValidator;
import org.bukkit.entity.Player;

public class SelfCooldownValidator extends BaseTriValidator<Player, Party, Collection<Player>> {

    public SelfCooldownValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean shouldValidate() {
        return config.getDuelCooldown() > 0L;
    }

    @Override
    public boolean validate(final Player sender, final Party party, final Collection<Player> players) {
        final Player cooldownPlayer = userManager.getCooldownPlayer(players);

        if (cooldownPlayer != null) {
            lang.sendMessage(sender, "ERROR.duel.in-cooldown",
                    "name", cooldownPlayer.getName(),
                    "time", DateUtil.formatMilliseconds(userManager.getDuelCooldownRemaining(cooldownPlayer)));
            return false;
        }

        return true;
    }
}