package io.github.tootertutor.ethyrial.spells;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import io.github.tootertutor.ethyrial.Ethyrial;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class SpellEventRouter implements Listener {
    private final Ethyrial plugin;

    // Triggers -> registered spells (Java + config)
    private final Map<TriggerType, Set<Spell>> byTrigger = new EnumMap<>(TriggerType.class);

    // Cooldowns & simple throttles (you likely have richer systems already)
    private final Map<UUID, Map<NamespacedKey, Long>> cooldownsByPlayer = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastMoveDispatch = new ConcurrentHashMap<>();

    public SpellEventRouter(Ethyrial plugin) {
        this.plugin = plugin;
        for (TriggerType t : TriggerType.values())
            byTrigger.put(t, ConcurrentHashMap.newKeySet());
    }

    public void registerSpell(Spell spell) {
        for (TriggerType t : spell.triggers())
            byTrigger.get(t).add(spell);
    }

    /** Hook this once in onEnable */
    public void registerBukkitListeners() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // ====== External API for “cast” intent (e.g., wand/spellbook) ======
    public void handlePlayerCast(Player caster) {
        Set<Spell> spells = byTrigger.getOrDefault(TriggerType.CAST, Collections.emptySet());
        if (spells.isEmpty())
            return;

        for (Spell spell : spells) {
            if (isOnCooldown(caster.getUniqueId(), spell))
                continue;
            if (!chargeMana(caster, spell)) {
                caster.sendMessage(Component.text("Not enough mana.", NamedTextColor.RED));
                continue;
            }
            setCooldown(caster.getUniqueId(), spell);
            try {
                spell.onCast(caster);
            } catch (Throwable t) {
                plugin.getLogger().warning("Spell " + spell.getKey() + " threw in onCast: " + t.getMessage());
                t.printStackTrace();
            }
        }
    }

    // ====== Bukkit listeners → trigger fanout ======
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p))
            return;
        dispatch(TriggerType.HIT_ENTITY, p, e);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent e) {
        dispatch(TriggerType.BLOCK_INTERACT, e.getPlayer(), e);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onMove(PlayerMoveEvent e) {
        // throttle move spam per player (adjust to taste)
        long now = System.currentTimeMillis();
        long last = lastMoveDispatch.getOrDefault(e.getPlayer().getUniqueId(), 0L);
        if (now - last < 150L)
            return;
        lastMoveDispatch.put(e.getPlayer().getUniqueId(), now);
        dispatch(TriggerType.PLAYER_MOVE, e.getPlayer(), e);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onProjectileLand(ProjectileHitEvent e) {
        // If you mark projectiles with metadata "ethyrial:caster", you can recover the
        // Player here
        Object tag = (e.getEntity().getShooter());
        if (!(tag instanceof Player p))
            return;
        dispatch(TriggerType.PROJECTILE_LAND, p, e);
    }

    private void dispatch(TriggerType trigger, Player contextPlayer, Event rawEvent) {
        Set<Spell> spells = byTrigger.getOrDefault(trigger, Collections.emptySet());
        if (spells.isEmpty())
            return;

        for (Spell spell : spells) {
            if (trigger == TriggerType.CAST)
                continue; // cast handled separately via handlePlayerCast
            if (isOnCooldown(contextPlayer.getUniqueId(), spell))
                continue;

            try {
                spell.onTriggered(plugin, trigger, contextPlayer, rawEvent);
            } catch (Throwable t) {
                plugin.getLogger().warning("Spell " + spell.getKey() + " threw in " + trigger + ": " + t.getMessage());
                t.printStackTrace();
            }
        }
    }

    // ====== Minimal cooldown/mana helpers (replace w/ your real systems) ======
    private boolean isOnCooldown(UUID uuid, Spell spell) {
        Map<NamespacedKey, Long> map = cooldownsByPlayer.get(uuid);
        if (map == null)
            return false;
        long now = System.currentTimeMillis();
        long end = map.getOrDefault(spell.getKey(), 0L);
        return now < end;
    }

    private void setCooldown(UUID uuid, Spell spell) {
        cooldownsByPlayer.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .put(spell.getKey(), System.currentTimeMillis() + ticksToMillis(spell.getCooldownTicks()));
    }

    private boolean chargeMana(Player player, Spell spell) {
        // integrate with your PlayerDataManager
        // return false if not enough, else deduct and return true
        return true;
    }

    private long ticksToMillis(long ticks) {
        return (ticks * 50L);
    }
}
