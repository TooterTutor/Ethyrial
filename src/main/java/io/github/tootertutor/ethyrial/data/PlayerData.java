package io.github.tootertutor.ethyrial.data;

import java.util.Set;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private final PlayerStats stats;
    private final Set<String> unlockedSpells;

    public PlayerData(UUID uuid, PlayerStats stats, Set<String> unlockedSpells) {
        this.uuid = uuid;
        this.stats = stats;
        this.unlockedSpells = unlockedSpells;
    }

    public UUID getUuid() {
        return uuid;
    }

    public PlayerStats getStats() {
        return stats;
    }

    public Set<String> getUnlockedSpells() {
        return unlockedSpells;
    }
}
