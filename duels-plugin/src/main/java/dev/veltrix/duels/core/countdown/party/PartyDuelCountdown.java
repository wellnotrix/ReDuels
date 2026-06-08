package dev.veltrix.duels.core.countdown.party;

import java.util.HashMap;
import java.util.Map;

import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.core.arena.ArenaImpl;
import dev.veltrix.duels.core.countdown.DuelCountdown;
import dev.veltrix.duels.core.match.party.PartyDuelMatch;
import dev.veltrix.duels.party.Party;
import dev.veltrix.duels.util.StringUtil;
import dev.veltrix.duels.util.compat.Titles;

public class PartyDuelCountdown extends DuelCountdown {

    private final PartyDuelMatch match;

    private final Map<Party, String> info = new HashMap<>();

    public PartyDuelCountdown(final DuelsPlugin plugin, final ArenaImpl arena, final PartyDuelMatch match) {
        super(plugin, arena, match, plugin.getConfiguration().getCdPartyDuelMessages(), plugin.getConfiguration().getCdPartyDuelTitles());
        this.match = match;
        match.getAllParties().forEach(party -> info.put(party, StringUtil.join(match.getNames(party), ", ")));
    }
    
    @Override
    protected void sendMessage(final String rawMessage, final String message, final String title) {
        final String kitName = match.getKit() != null ? match.getKit().getName() : lang.getMessage("GENERAL.none");
        match.getPlayerToParty().forEach((player, value) -> {
            config.playSound(player, rawMessage);
            player.sendMessage(message
                    .replace("%opponents%", info.get(arena.getOpponent(value)))
                    .replace("%kit%", kitName)
                    .replace("%arena%", arena.getName())
            );

            if (title != null) {
                Titles.send(player, title, null, 0, 20, 50);
            }
        });
    }
}
