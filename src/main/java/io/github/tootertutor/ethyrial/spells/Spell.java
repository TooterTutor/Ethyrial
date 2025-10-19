package io.github.tootertutor.ethyrial.spells;

import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.builders.ItemBuilder;
import io.github.tootertutor.ethyrial.cooldown.CooldownContext;
import io.github.tootertutor.ethyrial.cooldown.CooldownManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public abstract class Spell implements Keyed {
    protected final Ethyrial plugin;
    private NamespacedKey key;
    private String name;
    private String description;
    private int manaCost;
    private int cooldownTicks;
    private SpellDomain domain;
    protected final CooldownPolicy cooldownPolicy;

    public enum CooldownPolicy {
        START_ON_CAST,
        START_ON_COMPLETE
    }

    private final Map<UUID, CastSignal> castSignals = new ConcurrentHashMap<>();

    private static final class CastSignal {
        boolean completed;
        boolean failed;
    }

    protected Spell(Ethyrial plugin, String key, String name, String description, int manaCost, int cooldownTicks,
            SpellDomain domain) {
        this(plugin, key, name, description, manaCost, cooldownTicks, domain, CooldownPolicy.START_ON_CAST);
    }

    protected Spell(Ethyrial plugin, String key, String name, String description, int manaCost,
            int cooldownTicks, SpellDomain domain, CooldownPolicy cooldownPolicy) {
        this.plugin = plugin;
        this.key = new NamespacedKey(Ethyrial.getInstance(), key);
        this.name = name;
        this.description = description;
        this.manaCost = manaCost;
        this.cooldownTicks = cooldownTicks;
        this.domain = domain;
        this.cooldownPolicy = cooldownPolicy;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getManaCost() {
        return manaCost;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public SpellDomain getDomain() {
        return domain;
    }

    public int getPowerLevel() {
        return 1; // Or fetch dynamically
    }

    public Rarity getRarity() {
        return Rarity.COMMON; // Replace with actual logic
    }

    public boolean isUnlocked(Player player) {
        return true; // Implement based on player data
    }

    public ItemStack toItemStack(Player viewer) {
        List<Component> lore = List.of(
                Component.text(description, NamedTextColor.GRAY),
                Component.text("Mana: ", NamedTextColor.AQUA)
                        .append(Component.text(String.valueOf(manaCost), NamedTextColor.WHITE)),
                Component.text("Cooldown: ", NamedTextColor.YELLOW)
                        .append(Component.text((cooldownTicks / 20.0) + "s", NamedTextColor.WHITE)),
                Component.empty(),
                Component.text("Power Level: ", NamedTextColor.DARK_GREEN)
                        .append(Component.text(String.valueOf(getPowerLevel()), NamedTextColor.GREEN)),
                Component.text("Rarity: ", NamedTextColor.GOLD)
                        .append(Component.text(getRarity().getDisplayName(), NamedTextColor.WHITE)),
                Component.text(isUnlocked(viewer) ? "Unlocked" : "Locked",
                        isUnlocked(viewer) ? NamedTextColor.GREEN : NamedTextColor.RED));

        return new ItemBuilder(plugin)
                .setMaterial(domain.getIconMaterial())
                .name(Component.text(name, domain.getTextColor())
                        .decoration(TextDecoration.ITALIC, false))
                .lore(lore)
                .buildItemStack();
    }

    public Duration getBaseCooldown(Player caster) {
        return Duration.ofMillis(Math.max(0, (long) cooldownTicks) * 50L);
    }

    protected CooldownContext buildCooldownContext(Player caster) {
        return new CooldownContext(Map.of());
    }

    protected void onCooldownFeedback(Player caster, Duration remaining) {
        long ms = remaining.toMillis();
        String msg = (ms < 1000) ? (ms + "ms") : (((ms + 500) / 1000) + "s");
        caster.sendActionBar(Component.text("On cooldown: " + msg, NamedTextColor.RED));
    }

    public final void cast(Player caster) {
        final NamespacedKey cooldownKey = composeCooldownKey(caster);
        final Duration base = getBaseCooldown(caster);
        final CooldownContext ctx = buildCooldownContext(caster);

        if (cooldownPolicy == CooldownPolicy.START_ON_CAST) {
            if (!CooldownManager.get().tryStart(caster, cooldownKey, base, ctx)) {
                onCooldownFeedback(caster, CooldownManager.get().getRemaining(caster, cooldownKey));
                return;
            }
            try {
                onCast(caster);
            } catch (SpellFailedException ex) {
                refundCooldown(caster);
                onFailFeedback(caster, ex.getMessage());
            }
            return;
        }

        // allow continuations (e.g., Wormhole second cast)
        if (CooldownManager.get().isActive(caster, cooldownKey) && !isContinuation(caster)) {
            onCooldownFeedback(caster, CooldownManager.get().getRemaining(caster, cooldownKey));
            return;
        }

        // START_ON_COMPLETE:
        // 1) Let the spell run first…
        CastSignal signal = new CastSignal();
        castSignals.put(caster.getUniqueId(), signal);
        try {
            onCast(caster);
        } finally {
            // 2) Then decide whether to start cooldown
            castSignals.remove(caster.getUniqueId());
        }

        if (signal.failed) {
            // explicit failure => no cooldown
            return;
        }
        if (signal.completed) {
            // start cooldown now (with modifiers)
            if (!CooldownManager.get().tryStart(caster, cooldownKey, base, ctx)) {
                // If somehow already on cooldown (e.g., concurrent edge case) just feedback
                onCooldownFeedback(caster, CooldownManager.get().getRemaining(caster, cooldownKey));
            }
        } else {
            // neither failed nor completed => partial stage (e.g., Wormhole stage 1). No
            // cooldown yet.
        }
    }

    /** Spells call this exactly when their multi-stage action truly completes. */
    protected final void markCompleted(Player caster) {
        CastSignal s = castSignals.get(caster.getUniqueId());
        if (s != null)
            s.completed = true;
    }

    /**
     * Optional: mark as failed to explicitly skip cooldown even under
     * START_ON_COMPLETE.
     */
    protected final void markFailed(Player caster) {
        CastSignal s = castSignals.get(caster.getUniqueId());
        if (s != null)
            s.failed = true;
    }

    // unchanged:
    protected abstract void onCast(Player caster);

    /** Override if you need per-slot keys. Default: the spell's key. */
    protected NamespacedKey composeCooldownKey(Player caster) {
        return getKey();
    }

    public boolean isOnCooldown(Player caster) {
        return CooldownManager.get().isActive(caster, composeCooldownKey(caster));
    }

    /**
     * Override for multi-stage spells to declare that the player is mid-sequence.
     */
    protected boolean isContinuation(Player caster) {
        return false;
    }

    public Duration getRemaining(Player caster) {
        return CooldownManager.get().getRemaining(caster, composeCooldownKey(caster));
    }

    protected void refundCooldown(Player caster) {
        CooldownManager.get().end(caster, composeCooldownKey(caster));
    }

    public static class SpellFailedException extends RuntimeException {
        public SpellFailedException(String message) {
            super(message);
        }
    }

    protected void failWithThrow(String reason) {
        throw new SpellFailedException(reason);
    }

    /**
     * Called when a cast fails (validation, blocked line-of-sight, etc.).
     * START_ON_CAST: refunds the cooldown. START_ON_COMPLETE: just sends feedback.
     */
    protected void fail(Player caster, String reason) {
        if (reason != null && !reason.isBlank()) {
            caster.sendActionBar(Component.text(reason, NamedTextColor.RED));
        }
        if (cooldownPolicy == CooldownPolicy.START_ON_CAST) {
            refundCooldown(caster); // safe even if not active
        } else {
            markFailed(caster); // optional; just records intent
        }
    }

    protected void onFailFeedback(Player caster, String reason) {
        caster.sendActionBar(Component.text(reason, NamedTextColor.RED));
    }

    /* Helper to derive variant keys safely (for left/right, etc.) */
    protected static NamespacedKey withSuffix(NamespacedKey base, String suffix) {
        // NamespacedKey path allows [a-z0-9/._-]; sanitize just in case.
        String clean = suffix.toLowerCase().replaceAll("[^a-z0-9/._-]", "_");
        return new NamespacedKey(base.getNamespace(), base.getKey() + "/" + clean);
    }

    /** Which triggers should route to this spell? */
    public Set<TriggerType> triggers() {
        return EnumSet.of(TriggerType.CAST);
    }

    /** Generic dispatcher for other triggers (router passes the event safely) */
    public void onTriggered(Ethyrial plugin, TriggerType trigger, Player contextPlayer, Event rawEvent) {
    }
}
