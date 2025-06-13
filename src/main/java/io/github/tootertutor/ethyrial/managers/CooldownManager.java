package io.github.tootertutor.ethyrial.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

public class CooldownManager {
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public boolean isOnCooldown(Player player, String spellId) {
        long currentTime = System.currentTimeMillis();
        return cooldowns.getOrDefault(player.getUniqueId(), Map.of())
                .getOrDefault(spellId, 0L) > currentTime;
    }

    public void setCooldown(Player player, String spellId, long durationMs) {
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(spellId, System.currentTimeMillis() + durationMs);
    }

    public long getRemainingCooldown(Player player, String spellId) {
        long now = System.currentTimeMillis();
        return Math.max(0, cooldowns.getOrDefault(player.getUniqueId(), Map.of())
                .getOrDefault(spellId, 0L) - now);
    }
}
