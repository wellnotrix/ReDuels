package dev.veltrix.duels.data;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import dev.veltrix.duels.api.kit.Kit;
import dev.veltrix.duels.api.user.MatchInfo;
import dev.veltrix.duels.api.user.User;
import dev.veltrix.duels.util.Log;
import dev.veltrix.duels.util.json.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserData implements User {

    private static transient final String ERROR_USER_SAVE = "An error occured while saving userdata of %s!";
    transient File folder;
    transient int defaultRating;
    transient int matchesToDisplay;
    @Getter
    private UUID uuid;
    @Getter
    @Setter
    private String name;
    @Getter
    private volatile int wins;
    @Getter
    private volatile int losses;
    @Getter
    private volatile int matchesPlayed;
    @Getter
    private volatile long lastPlayed;
    @Getter
    private volatile long duelCooldownUntil;
    private boolean requests = true;
    ConcurrentHashMap<String, Integer> rating;

    public Map<String, Integer> getRatingsMap() {
        if (this.rating == null) {
            this.rating = new ConcurrentHashMap<>();
        }
        return this.rating;
    }
    private final List<MatchData> matches = Collections.synchronizedList(new ArrayList<>());
    private boolean partyRequests = true;

    private UserData() {
    }

    public UserData(final File folder, final int defaultRating, final int matchesToDisplay, final Player player) {
        this.folder = folder;
        this.defaultRating = defaultRating;
        this.matchesToDisplay = matchesToDisplay;
        this.uuid = player.getUniqueId();
        this.name = player.getName();
    }

    /**
     * Copy constructor for snapshotting
     */
    private UserData(UserData other) {
        this.folder = other.folder;
        this.defaultRating = other.defaultRating;
        this.matchesToDisplay = other.matchesToDisplay;
        this.uuid = other.uuid;
        this.name = other.name;
        this.wins = other.wins;
        this.losses = other.losses;
        this.duelCooldownUntil = other.duelCooldownUntil;
        this.requests = other.requests;
        this.partyRequests = other.partyRequests;
        if (other.rating != null) {
            this.rating = new ConcurrentHashMap<>(other.rating);
        }
        synchronized (other.matches) {
            this.matches.addAll(other.matches);
        }
    }

    public void asyncSave(dev.veltrix.duels.DuelsPlugin plugin) {
        final UserData snapshot = new UserData(this);
        plugin.doAsync(snapshot::trySave);
    }

    @Override
    public void setWins(final int wins) {
        this.wins = wins;

        if (isOffline()) {
            asyncSave(dev.veltrix.duels.DuelsPlugin.getInstance());
        }
    }

    @Override
    public void setLosses(final int losses) {
        this.losses = losses;

        if (isOffline()) {
            asyncSave(dev.veltrix.duels.DuelsPlugin.getInstance());
        }
    }

    @Override
    public boolean canRequest() {
        return requests;
    }

    @Override
    public void setRequests(final boolean requests) {
        this.requests = requests;

        if (isOffline()) {
            asyncSave(dev.veltrix.duels.DuelsPlugin.getInstance());
        }
    }

    @NotNull
    @Override
    public List<MatchInfo> getMatches() {
        return Collections.unmodifiableList(matches);
    }

    @Override
    public int getRating() {
        return getRatingUnsafe(null);
    }

    @Override
    public int getRating(@NotNull final Kit kit) {
        Objects.requireNonNull(kit, "kit");
        return getRatingUnsafe(kit);
    }

    @Override
    public void resetRating() {
        setRating(null, defaultRating);
    }

    @Override
    public void resetRating(@NotNull final Kit kit) {
        Objects.requireNonNull(kit, "kit");
        setRating(kit, defaultRating);
    }

    @Override
    public void reset() {
        wins = 0;
        losses = 0;
        duelCooldownUntil = 0L;
        matches.clear();
        rating.clear();

        if (isOffline()) {
            asyncSave(dev.veltrix.duels.DuelsPlugin.getInstance());
        }
    }

    public boolean canPartyRequest() {
        return partyRequests;
    }

    public void setPartyRequests(final boolean partyRequests) {
        this.partyRequests = partyRequests;

        if (isOffline()) {
            asyncSave(dev.veltrix.duels.DuelsPlugin.getInstance());
        }
    }

    public int getRatingUnsafe(@Nullable final Kit kit) {
        return this.rating != null ? this.rating.getOrDefault(kit == null ? "-" : kit.getName(), defaultRating) : defaultRating;
    }

    public void setRating(final Kit kit, final int rating) {
        if (this.rating == null) {
            this.rating = new ConcurrentHashMap<>();
        }

        this.rating.put(kit == null ? "-" : kit.getName(), rating);

        if (isOffline()) {
            asyncSave(dev.veltrix.duels.DuelsPlugin.getInstance());
        }
    }

    public void incrementMatchesPlayed() {
        this.matchesPlayed++;
        this.lastPlayed = System.currentTimeMillis();
        if (isOffline()) {
            asyncSave(dev.veltrix.duels.DuelsPlugin.getInstance());
        }
    }

    private boolean isOffline() {
        return Bukkit.getPlayer(uuid) == null;
    }

    public void addWin() {
        final int wins = this.wins;
        this.wins = wins + 1;
        incrementMatchesPlayed();
    }

    public void addLoss() {
        final int losses = this.losses;
        this.losses = losses + 1;
        incrementMatchesPlayed();
    }

    @Override
    public void setDuelCooldownUntil(final long duelCooldownUntil) {
        this.duelCooldownUntil = Math.max(duelCooldownUntil, 0L);

        if (isOffline()) {
            asyncSave(dev.veltrix.duels.DuelsPlugin.getInstance());
        }
    }

    public void addMatch(final MatchData matchData) {
        synchronized (matches) {
            if (!matches.isEmpty() && matches.size() >= matchesToDisplay) {
                matches.remove(0);
            }

            matches.add(matchData);
        }
    }
    void refreshMatches() {
        if (matches.size() < matchesToDisplay) {
            return;
        }

        final List<MatchData> division = Lists.newArrayList(matches.subList(matches.size() - matchesToDisplay, matches.size()));
        matches.clear();
        matches.addAll(division);
    }

    public void trySave() {
        final File file = new File(folder, uuid + ".json");

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            try (final Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
                JsonUtil.getObjectWriter().writeValue(writer, this);
                writer.flush();
            }
        } catch (IOException ex) {
            Log.error(String.format(ERROR_USER_SAVE, name), ex);
        }
    }

    @Override
    public String toString() {
        return "UserData{" +
                "uuid=" + uuid +
                ", name='" + name + '\'' +
                ", wins=" + wins +
                ", losses=" + losses +
                ", requests=" + requests +
                ", matches=" + matches +
                ", rating=" + rating +
                '}';
    }
}
