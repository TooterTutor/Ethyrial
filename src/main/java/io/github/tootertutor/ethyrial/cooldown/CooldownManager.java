package io.github.tootertutor.ethyrial.cooldown;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.Ethyrial;

public final class CooldownManager {

    private static final class Entry {
        final long endsAtNanos;

        Entry(long endsAtNanos) {
            this.endsAtNanos = endsAtNanos;
        }
    }

    // player -> (spellKey -> Entry)
    private final Map<UUID, Map<NamespacedKey, Entry>> map = new ConcurrentHashMap<>();

    // modifiers: id -> modifier
    private final Map<String, CooldownModifier> modifiers = new ConcurrentHashMap<>();

    private static CooldownManager instance;

    public static CooldownManager get() {
        if (instance == null)
            instance = new CooldownManager();
        return instance;
    }

    private CooldownManager() {
    }

    /**
     * Returns effective cooldown after applying all registered modifiers in
     * priority order.
     */
    public Duration computeEffective(Player player, NamespacedKey spellKey, Duration base, CooldownContext ctx) {
        Duration out = base;
        var ordered = modifiers.values().stream()
                .sorted((a, b) -> Integer.compare(a.priority(), b.priority()))
                .toList();
        for (CooldownModifier mod : ordered) {
            out = mod.modify(player, spellKey, out, ctx);
            if (out.isZero() || out.isNegative())
                return Duration.ZERO;
        }
        return out;
    }

    /** Attempt to start. Returns true if started, false if already active. */
    public boolean tryStart(Player player, NamespacedKey spellKey, Duration baseDuration, CooldownContext ctx) {
        final Duration effective = computeEffective(player, spellKey, baseDuration, ctx);
        final Map<NamespacedKey, Entry> bySpell = map.computeIfAbsent(player.getUniqueId(),
                k -> new ConcurrentHashMap<>());

        if (!effective.isZero() && !effective.isNegative()) {
            final long now = System.nanoTime();
            final Entry existing = bySpell.get(spellKey);
            if (existing != null && existing.endsAtNanos > now)
                return false;

            final long endsAt = now + effective.toNanos();
            bySpell.put(spellKey, new Entry(endsAt));
            fireStart(player, spellKey, effective);

            final long delayTicks = Math.max(1L, nanosToTicks(effective.toNanos()));
            Bukkit.getScheduler().runTaskLater(Ethyrial.getInstance(), () -> {
                Entry e = bySpell.get(spellKey);
                if (e == null) {
                    fireEnd(player, spellKey);
                    return;
                }
                if (System.nanoTime() >= e.endsAtNanos) {
                    bySpell.remove(spellKey);
                    fireEnd(player, spellKey);
                }
            }, delayTicks);
            return true;
        } else {
            // zero/negative cooldown: start & immediately end (for listeners/UI
            // consistency)
            fireStart(player, spellKey, Duration.ZERO);
            fireEnd(player, spellKey);
            return true;
        }
    }

    public boolean isActive(Player player, NamespacedKey spellKey) {
        final Map<NamespacedKey, Entry> bySpell = map.get(player.getUniqueId());
        if (bySpell == null)
            return false;
        final Entry e = bySpell.get(spellKey);
        return e != null && e.endsAtNanos > System.nanoTime();
    }

    public Duration getRemaining(Player player, NamespacedKey spellKey) {
        final Map<NamespacedKey, Entry> bySpell = map.get(player.getUniqueId());
        if (bySpell == null)
            return Duration.ZERO;
        final Entry e = bySpell.get(spellKey);
        if (e == null)
            return Duration.ZERO;
        final long now = System.nanoTime();
        if (e.endsAtNanos <= now)
            return Duration.ZERO;
        return Duration.ofNanos(e.endsAtNanos - now);
    }

    public void end(Player player, NamespacedKey spellKey) {
        final Map<NamespacedKey, Entry> bySpell = map.get(player.getUniqueId());
        if (bySpell == null)
            return;
        if (bySpell.remove(spellKey) != null)
            fireEnd(player, spellKey);
    }

    public void clearAll(Player player) {
        final Map<NamespacedKey, Entry> bySpell = map.remove(player.getUniqueId());
        if (bySpell != null)
            bySpell.clear();
    }

    public void registerModifier(String id, CooldownModifier modifier) {
        modifiers.put(id, modifier);
    }

    public void unregisterModifier(String id) {
        modifiers.remove(id);
    }

    private static long nanosToTicks(long nanos) {
        long ms = Math.max(1L, nanos / 1_000_000L);
        return Math.max(1L, ms / 50L);
    }

    private void fireStart(Player player, NamespacedKey key, Duration effective) {
        Bukkit.getPluginManager().callEvent(new SpellCooldownStartEvent(player, key, effective));
    }

    private void fireEnd(Player player, NamespacedKey key) {
        Bukkit.getPluginManager().callEvent(new SpellCooldownEndEvent(player, key));
    }
}
