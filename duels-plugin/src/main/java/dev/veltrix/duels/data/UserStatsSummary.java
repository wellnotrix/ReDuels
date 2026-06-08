package dev.veltrix.duels.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@AllArgsConstructor
public class UserStatsSummary {
    private final UUID uuid;
    private String name;
    private int wins;
    private int losses;
    private final Map<String, Integer> ratings;

    public int getRating(String kitName) {
        return ratings.getOrDefault(kitName, 0); // Default rating are handled by UserManager so 0
    }

    public void setRating(String kitName, int rating) {
        ratings.put(kitName, rating);
    }
}
