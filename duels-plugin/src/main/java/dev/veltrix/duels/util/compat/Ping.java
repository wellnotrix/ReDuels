package dev.veltrix.duels.util.compat;
import org.bukkit.entity.Player;

public final class Ping {

    public static int getPing(final Player player) {

        return player.getPing();

    }
}