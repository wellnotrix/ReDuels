package dev.veltrix.duels.data;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Getter;
import dev.veltrix.duels.api.folialib.task.WrappedTask;
import dev.veltrix.duels.api.kit.Kit;
import dev.veltrix.duels.api.user.User;
import dev.veltrix.duels.api.user.UserManager;
import dev.veltrix.duels.config.Config;
import dev.veltrix.duels.config.Lang;
import dev.veltrix.duels.DuelsPlugin;
import dev.veltrix.duels.Permissions;
import dev.veltrix.duels.api.event.user.UserCreateEvent;
import dev.veltrix.duels.core.kit.KitImpl;
import dev.veltrix.duels.core.match.DuelMatch;
import dev.veltrix.duels.core.match.party.PartyDuelMatch;
import dev.veltrix.duels.party.Party;
import dev.veltrix.duels.util.*;
import dev.veltrix.duels.util.json.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UserManagerImpl implements Loadable, Listener, UserManager {

    private static final Calendar GREGORIAN_CALENDAR = new GregorianCalendar();
    private static final String ADMIN_UPDATE_MESSAGE = "&9[Duels] &bDuels &fv%s &7is now available for download! Download at: &c%s";

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final File folder;

    // Currently connected players only
    private final Map<UUID, UserData> onlineUsers = new ConcurrentHashMap<>();

    // Cached offline player data for commands like /stats
    private final LoadingCache<UUID, UserData> offlineUserCache;

    // Lightweight stats used for leaderboards and rankings
    private final Map<UUID, UserStatsSummary> statsSummaryMap = new ConcurrentHashMap<>();
    
    private final Map<String, UUID> names = new ConcurrentHashMap<>();
    private final Map<Kit, TopEntry> topRatings = new ConcurrentHashMap<>();
    private volatile int defaultRating;
    private volatile int matchesToDisplay;
    @Getter
    private volatile boolean loaded;
    @Getter
    private volatile TopEntry wins;
    @Getter
    private volatile TopEntry losses;
    @Getter
    private volatile TopEntry noKit;
    private WrappedTask topTask;

    public UserManagerImpl(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.folder = new File(plugin.getDataFolder(), "users");

        if (!folder.exists()) {
            folder.mkdir();
        }

        // Init offline user cache
        this.offlineUserCache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(new CacheLoader<UUID, UserData>() {
                    @Override
                    public UserData load(UUID uuid) throws Exception {
                        // This will be called if the user is not in the cache
                        // We need a way to load without a Player object
                        return loadFromFile(uuid);
                    }
                });

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() {
        this.defaultRating = config.getDefaultRating();
        this.matchesToDisplay = config.getMatchesToDisplay();

        if (matchesToDisplay < 0) {
            matchesToDisplay = 0;
        }

        plugin.doAsync(() -> {
            final File[] files = folder.listFiles();

            if (files != null) {
                int count = 0;
                for (final File file : files) {
                    final String fileName = file.getName();

                    if (!fileName.endsWith(".json")) {
                        continue;
                    }

                    final String nameStr = fileName.substring(0, fileName.length() - 5);
                    final UUID uuid = UUIDUtil.parseUUID(nameStr);

                    if (uuid == null) {
                        continue;
                    }

                    try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
                        // Load ONLY the data needed for leaderboards
                        final UserData user = JsonUtil.getObjectMapper().readValue(reader, UserData.class);

                        if (user == null) {
                            continue;
                        }

                        // Populate summary and name map
                        UserStatsSummary summary = new UserStatsSummary(
                                uuid,
                                user.getName(),
                                user.getWins(),
                                user.getLosses(),
                                user.getRatingsMap()
                        );
                        statsSummaryMap.put(uuid, summary);
                        names.put(user.getName().toLowerCase(), uuid);
                        count++;
                    } catch (IOException ex) {
                        Log.error(this, "Could not load userdata summary from file: " + fileName, ex);
                    }
                }
                Log.info("Loaded " + count + " user summaries into memory.");
            }

            loaded = true;
        });

        this.topTask = plugin.doSyncRepeat(() -> {
            final Collection<? extends Kit> kits = plugin.getKitManager().getKits();

            plugin.doAsync(() -> {
                if (!loaded) {
                    return;
                }

                TopEntry top;

                if ((top = getTop(config.getTopUpdateInterval(), wins, UserStatsSummary::getWins, config.getTopWinsType(), config.getTopWinsIdentifier())) != null) {
                    wins = top;
                }

                if ((top = getTop(config.getTopUpdateInterval(), losses, UserStatsSummary::getLosses, config.getTopLossesType(), config.getTopLossesIdentifier())) != null) {
                    losses = top;
                }

                if ((top = getTop(config.getTopUpdateInterval(), noKit, s -> s.getRating("-"), config.getTopNoKitType(), config.getTopNoKitIdentifier())) != null) {
                    noKit = top;
                }

                topRatings.keySet().removeIf(kit -> !kits.contains(kit));

                for (final Kit kit : kits) {
                    final TopEntry entry = topRatings.get(kit);

                    if ((top = getTop(config.getTopUpdateInterval(), entry, s -> s.getRating(kit.getName()), config.getTopKitType().replace("%kit%", kit.getName()),
                            config.getTopKitIdentifier())) != null) {
                        topRatings.put(kit, top);
                    }
                }
            });
        }, 20L * 5, 20L);
    }

    @Override
    public void handleUnload() {
        plugin.cancelTask(topTask);
        loaded = false;
        
        // Save all online users async here
        for (UserData user : onlineUsers.values()) {
            user.asyncSave(plugin);
        }
        
        onlineUsers.clear();
        offlineUserCache.invalidateAll();
        statsSummaryMap.clear();
        names.clear();
        topRatings.clear();
    }

    @Nullable
    @Override
    public UserData get(@NotNull final String name) {
        Objects.requireNonNull(name, "name");
        final UUID uuid = names.get(name.toLowerCase());
        return uuid != null ? get(uuid) : null;
    }

    @Nullable
    @Override
    public UserData get(@NotNull final UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        
        UserData user = onlineUsers.get(uuid);
        if (user != null) return user;
        
        try {
            return offlineUserCache.get(uuid);
        } catch (Exception e) {
            // This will return null when the user aint on disk
            // or if there's an I/O error too
            return null;
        }
    }

    @Nullable
    @Override
    public UserData get(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");
        return get(player.getUniqueId());
    }

    /**
     * Loads a user from file without a Player object.
     */
    private UserData loadFromFile(UUID uuid) {
        final File file = new File(folder, uuid + ".json");
        if (!file.exists()) return null;

        try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
            final UserData user = JsonUtil.getObjectMapper().readValue(reader, UserData.class);
            if (user != null) {
                user.folder = folder;
                user.defaultRating = defaultRating;
                user.matchesToDisplay = matchesToDisplay;
                user.refreshMatches();
            }
            return user;
        } catch (IOException ex) {
            Log.error(this, "Failed to load offline user: " + uuid, ex);
            return null;
        }
    }

    private TopEntry getTop(final long interval, final TopEntry previous, final Function<UserStatsSummary, Integer> function, final String type, final String identifier) {
        if (previous == null || System.currentTimeMillis() - previous.getCreation() >= interval) {
            return new TopEntry(type, identifier, subList(sorted(function)));
        }

        return null;
    }

    private List<TopData> subList(final List<TopData> list) {
        return Collections.unmodifiableList(new ArrayList<>(list.size() > 10 ? list.subList(0, 10) : list));
    }

    @Nullable
    @Override
    public TopEntry getTopWins() {
        return wins;
    }

    @Nullable
    @Override
    public TopEntry getTopLosses() {
        return losses;
    }

    @Nullable
    @Override
    public TopEntry getTopRatings() {
        return noKit;
    }

    @Nullable
    @Override
    public TopEntry getTopRatings(@NotNull final Kit kit) {
        Objects.requireNonNull(kit, "kit");
        return topRatings.get(kit);
    }

    public String getNextUpdate(final long creation) {
        return DateUtil.format((creation + config.getTopUpdateInterval() - System.currentTimeMillis()) / 1000L);
    }

    private UserData tryLoad(final Player player) {
        final File file = new File(folder, player.getUniqueId() + ".json");

        if (!file.exists()) {
            final UserData user = new UserData(folder, defaultRating, matchesToDisplay, player);
            plugin.doSync(() -> Bukkit.getPluginManager().callEvent(new UserCreateEvent(user)));
            return user;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
            final UserData user = JsonUtil.getObjectMapper().readValue(reader, UserData.class);

            if (user == null) {
                return null;
            }

            user.folder = folder;
            user.defaultRating = defaultRating;
            user.matchesToDisplay = matchesToDisplay;
            user.refreshMatches();

            if (!player.getName().equals(user.getName())) {
                user.setName(player.getName());
            }

            return user;
        } catch (IOException ex) {
            Log.error(this, "An error occured while loading userdata of " + player.getName() + "!", ex);
            return null;
        }
    }

    private void saveUsers(final Collection<? extends Player> players) {
        for (final Player player : players) {
            final UserData user = onlineUsers.remove(player.getUniqueId());

            if (user != null) {
                user.asyncSave(plugin);
                updateSummary(user);
            }
        }
    }

    private void updateSummary(UserData user) {
        UserStatsSummary summary = statsSummaryMap.get(user.getUuid());
        if (summary == null) {
            summary = new UserStatsSummary(user.getUuid(), user.getName(), user.getWins(), user.getLosses(), user.getRatingsMap());
            statsSummaryMap.put(user.getUuid(), summary);
        } else {
            summary.setName(user.getName());
            summary.setWins(user.getWins());
            summary.setLosses(user.getLosses());
            summary.getRatings().clear();
            summary.getRatings().putAll(user.getRatingsMap());
        }
        names.put(user.getName().toLowerCase(), user.getUuid());
    }

    @EventHandler
    public void on(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        plugin.doSyncAfter(() -> {
            if (plugin.getUpdateManager() != null) {
                if (plugin.getUpdateManager().updateIsAvailable() && (player.isOp() || player.hasPermission(Permissions.ADMIN))) {
                    player.sendMessage(StringUtil.color(String.format(ADMIN_UPDATE_MESSAGE, plugin.getUpdateManager().getLatestVersion(), plugin.getDescription().getWebsite())));
                }
            }
        }, 5L);

        final UserData user = onlineUsers.get(player.getUniqueId());

        if (user != null) {
            if (!player.getName().equals(user.getName())) {
                user.setName(player.getName());
                names.put(player.getName().toLowerCase(), player.getUniqueId());
                updateSummary(user);
            }
            return;
        }

        plugin.doAsync(() -> {
            final UserData data = tryLoad(player);

            if (data == null) {
                lang.sendMessage(player, "ERROR.data.load-failure");
                return;
            }

            names.put(player.getName().toLowerCase(), player.getUniqueId());
            onlineUsers.put(player.getUniqueId(), data);
            updateSummary(data);
        });
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final UserData user = onlineUsers.remove(uuid);

        if (user != null) {
            user.asyncSave(plugin);
            updateSummary(user);
        }
    }

    public void handleMatchEnd(final DuelMatch match, final Set<Player> winners) {
        final Player winner = winners.iterator().next();
        final String kitName = match.getKit() != null ? match.getKit().getName() : lang.getMessage("GENERAL.none");
        final String message;

        if (!(match instanceof PartyDuelMatch partyMatch)) {
            final long duration = System.currentTimeMillis() - match.getStart();
            final long time = GREGORIAN_CALENDAR.getTimeInMillis();
            final Player loser = match.getArena().getOpponent(winner);
            final double health = Math.ceil(winner.getHealth()) * 0.5;
            final MatchData matchData = new MatchData(winner.getName(), loser.getName(), kitName, time, duration, health);
            final UserData winnerData = get(winner);
            final UserData loserData = get(loser);

            if (winnerData != null && loserData != null) {
                winnerData.addWin();
                loserData.addLoss();
                winnerData.addMatch(matchData);
                loserData.addMatch(matchData);

                final KitImpl kit = match.getKit();
                int winnerRating = winnerData.getRatingUnsafe(kit);
                int loserRating = loserData.getRatingUnsafe(kit);
                int change = 0;

                if (config.isRatingEnabled() && !(!match.isFromQueue() && config.isRatingQueueOnly())) {
                    change = NumberUtil.getChange(config.getKFactor(), winnerRating, loserRating, winnerData.getMatchesPlayed());
                    winnerData.setRating(kit, winnerRating = winnerRating + change);
                    loserData.setRating(kit, loserRating = loserRating - change);
                }

                // Update summaries for leaderboards
                updateSummary(winnerData);
                updateSummary(loserData);

                message = lang.getMessage("DUEL.on-end.opponent-defeat",
                        "winner", winner.getName(),
                        "loser", loser.getName(),
                        "health", matchData.getHealth(),
                        "kit", kitName,
                        "arena", match.getArena().getName(),
                        "winner_rating", winnerRating,
                        "loser_rating", loserRating,
                        "change", change
                );
            } else {
                message = null;
            }
        } else {
            final Party winnerParty = partyMatch.getPlayerToParty().get(winner);
            final Party loserParty = match.getArena().getOpponent(winnerParty);
            message = lang.getMessage("DUEL.on-end.party-opponent-defeat",
                    "winners", StringUtil.join(partyMatch.getNames(winnerParty), ", "),
                    "losers", StringUtil.join(partyMatch.getNames(loserParty), ", "),
                    "kit", kitName,
                    "arena", match.getArena().getName()
            );
        }

        applyDuelCooldown(match.getAllPlayers());

        if (message == null) {
            return;
        }

        if (config.isArenaOnlyEndMessage()) {
            match.getArena().broadcast(message);
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(message);
            }
        }
    }

    public void handleTeamMatchEnd(final DuelMatch match, final Set<Player> winners, final Set<Player> losers) {
        final String kitName = match.getKit() != null ? match.getKit().getName() : lang.getMessage("GENERAL.none");
        
        // Update wins and losses for all players
        for (final Player winner : winners) {
            final UserData winnerData = get(winner);
            if (winnerData != null) {
                winnerData.addWin();
            }
        }
        
        for (final Player loser : losers) {
            final UserData loserData = get(loser);
            if (loserData != null) {
                loserData.addLoss();
            }
        }
        
        // Handle rating updates for team matches
        if (config.isRatingEnabled() && !(!match.isFromQueue() && config.isRatingQueueOnly())) {
            final KitImpl kit = match.getKit();
            
            // Calculate average rating for each team
            int winnerTeamRating = 0;
            int loserTeamRating = 0;
            int winnerCount = 0;
            int loserCount = 0;
            
            for (final Player winner : winners) {
                final UserData winnerData = get(winner);
                if (winnerData != null) {
                    winnerTeamRating += winnerData.getRatingUnsafe(kit);
                    winnerCount++;
                }
            }
            
            for (final Player loser : losers) {
                final UserData loserData = get(loser);
                if (loserData != null) {
                    loserTeamRating += loserData.getRatingUnsafe(kit);
                    loserCount++;
                }
            }
            
            if (winnerCount > 0 && loserCount > 0) {
                winnerTeamRating /= winnerCount;
                loserTeamRating /= loserCount;
                
                final int change = NumberUtil.getChange(config.getKFactor(), winnerTeamRating, loserTeamRating, 0);
                
                // Apply rating changes to all players
                for (final Player winner : winners) {
                    final UserData winnerData = get(winner);
                    if (winnerData != null) {
                        final int currentRating = winnerData.getRatingUnsafe(kit);
                        winnerData.setRating(kit, currentRating + change);
                        updateSummary(winnerData);
                    }
                }
                
                for (final Player loser : losers) {
                    final UserData loserData = get(loser);
                    if (loserData != null) {
                        final int currentRating = loserData.getRatingUnsafe(kit);
                        loserData.setRating(kit, currentRating - change);
                        updateSummary(loserData);
                    }
                }
            }
        } else {
            // Update summaries even if ratings are disabled
            for (final Player winner : winners) {
                final UserData winnerData = get(winner);
                if (winnerData != null) updateSummary(winnerData);
            }
            for (final Player loser : losers) {
                final UserData loserData = get(loser);
                if (loserData != null) updateSummary(loserData);
            }
        }
        
        final String winnerNames = winners.stream().map(Player::getName).collect(Collectors.joining(", "));
        final String loserNames = losers.stream().map(Player::getName).collect(Collectors.joining(", "));
        
        final String message = lang.getMessage("DUEL.on-end.team-opponent-defeat",
                "winners", winnerNames,
                "losers", loserNames,
                "kit", kitName,
                "arena", match.getArena().getName()
        );
        
        if (config.isArenaOnlyEndMessage()) {
            match.getArena().broadcast(message);
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(message);
            }
        }

        applyDuelCooldown(winners);
        applyDuelCooldown(losers);
    }

    public boolean isOnDuelCooldown(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");
        final UserData user = get(player);
        return user != null && user.isInDuelCooldown();
    }

    public long getDuelCooldownRemaining(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");
        final UserData user = get(player);
        return user != null ? user.getDuelCooldownRemaining() : 0L;
    }

    public Player getCooldownPlayer(@NotNull final Collection<Player> players) {
        Objects.requireNonNull(players, "players");
        return players.stream().filter(this::isOnDuelCooldown).findFirst().orElse(null);
    }

    public boolean hasDuelCooldown(@NotNull final Collection<Player> players) {
        Objects.requireNonNull(players, "players");
        return getCooldownPlayer(players) != null;
    }

    public void applyDuelCooldown(@NotNull final Collection<Player> players) {
        Objects.requireNonNull(players, "players");

        final long cooldown = config.getDuelCooldown();

        if (cooldown <= 0L) {
            return;
        }

        final long until = System.currentTimeMillis() + (cooldown * 1000L);

        for (final Player player : players) {
            final UserData user = get(player);

            if (user != null) {
                user.setDuelCooldownUntil(until);
            }
        }
    }

    private List<TopData> sorted(final Function<UserStatsSummary, Integer> function) {
        return statsSummaryMap.values().stream()
                .map(data -> new TopData(data.getUuid(), data.getName(), function.apply(data)))
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

}
