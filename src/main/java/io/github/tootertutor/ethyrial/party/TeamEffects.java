package io.github.tootertutor.ethyrial.party;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.NamespacedKey;

public final class TeamEffects {
    // teamId -> (spellKey -> expiryMillis)
    private final ConcurrentMap<UUID, ConcurrentMap<NamespacedKey, Long>> immunities = new ConcurrentHashMap<>();

    public void grantImmunity(UUID teamId, NamespacedKey spellKey, long durationMillis) {
        immunities.computeIfAbsent(teamId, k -> new ConcurrentHashMap<>())
                .put(spellKey, System.currentTimeMillis() + durationMillis);
    }

    public boolean hasImmunity(UUID teamId, NamespacedKey spellKey) {
        ConcurrentMap<NamespacedKey, Long> map = immunities.get(teamId);
        if (map == null)
            return false;
        Long exp = map.get(spellKey);
        if (exp == null)
            return false;
        if (System.currentTimeMillis() > exp) {
            map.remove(spellKey);
            return false;
        }
        return true;
    }

    public void clearTeam(UUID teamId) {
        immunities.remove(teamId);
    }

    public void clearPlayerFromTeam(UUID teamId, UUID player) {
        // nothing per player here yet; placeholder for future per‑player buffs
    }
}
