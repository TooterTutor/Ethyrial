package io.github.tootertutor.ethyrial.spells.veyruun;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.interfaces.AutoRegisterSpell;
import io.github.tootertutor.ethyrial.spells.Spell;
import io.github.tootertutor.ethyrial.spells.SpellDomain;

public class EventHorizon extends Spell implements AutoRegisterSpell {

    // Tunables
    private static final double RADIUS = 8.0; // Dome radius
    private static final int DURATION_TICKS = 140; // ~7s
    private static final double BASE_PUSH = 0.18; // Repulsion magnitude
    private static final double LIFT = 0.02; // Small vertical lift to avoid “sticky” ground friction
    private static final double FALLOFF_MIN_DIST = 1.25; // Avoid extreme forces at center
    private static final boolean AFFECT_PLAYERS = true; // Toggle if it should hit other players

    public EventHorizon(Ethyrial plugin) {
        super(Ethyrial.getInstance(),
                "eventhorizon",
                "Event Horizon",
                "Creates a repulsive field that shoves foes away from the caster’s position.",
                45,
                300,
                SpellDomain.VEYRUUN,
                CooldownPolicy.START_ON_CAST);
    }

    @Override
    public void onCast(Player caster) {
        Location center = caster.getLocation().clone().add(0, 0.5, 0); // bias to chest height
        startField(center, caster);
        showFieldEffect(center, 24); // initial burst
    }

    private void startField(Location center, Player caster) {
        World world = center.getWorld();
        if (world == null)
            return;

        Set<UUID> inside = new HashSet<>();

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!center.isWorldLoaded()) {
                    cancel();
                    return;
                }

                // Gentle idle visuals each tick
                showFieldEffect(center, 6);

                // Query entities currently inside the sphere
                Collection<Entity> current = world.getNearbyEntities(
                        center, RADIUS, RADIUS, RADIUS,
                        e -> e instanceof LivingEntity
                                && e.isValid()
                                && !e.equals(caster)
                                && AFFECT_PLAYERS
                                && e.getLocation().distanceSquared(center) <= RADIUS * RADIUS);

                // Build now-set
                Set<UUID> now = new HashSet<>();
                for (Entity e : current)
                    now.add(e.getUniqueId());

                // Enter events
                for (Entity e : current) {
                    UUID id = e.getUniqueId();
                    if (!inside.contains(id)) {
                        inside.add(id);
                        onEnter(e, center);
                    }
                }

                // Leave events
                Set<UUID> snapshot = new HashSet<>(inside);
                for (UUID id : snapshot) {
                    if (!now.contains(id)) {
                        inside.remove(id);
                        Entity left = world.getEntity(id);
                        onLeave(left, center);
                    }
                }

                // Apply repulsion
                for (Entity entity : current) {
                    applyRepulsion(entity, center);
                }

                // Lifetime
                ticks++;
                if (ticks >= DURATION_TICKS) {
                    // Flush leaves for any still-inside (optional cleanup)
                    for (UUID id : inside) {
                        Entity e = world.getEntity(id);
                        onLeave(e, center);
                    }
                    inside.clear();
                    cancel();
                }
            }
        }.runTaskTimer(Ethyrial.getInstance(), 0L, 1L);
    }

    /** Visual dome & sparkles at the boundary. */
    private void showFieldEffect(Location center, int boundaryPoints) {
        World world = center.getWorld();
        if (world == null)
            return;

        // Core shimmer
        world.spawnParticle(Particle.WITCH, center.clone().add(0, 0.5, 0), 12, 0.4, 0.5, 0.4, 0.02);

        // Boundary ring/sphere (random sampling)
        for (int i = 0; i < boundaryPoints; i++) {
            double theta = Math.random() * 2 * Math.PI;
            double phi = Math.acos(2 * Math.random() - 1);
            double x = RADIUS * Math.sin(phi) * Math.cos(theta);
            double y = RADIUS * Math.cos(phi);
            double z = RADIUS * Math.sin(phi) * Math.sin(theta);
            world.spawnParticle(Particle.DUST, center.clone().add(x, y, z), 1, 0, 0, 0, 1,
                    new Particle.DustOptions(Color.fromRGB(40, 40, 80), 1.1f));
        }
    }

    /**
     * Pushes entities away from center with distance-based falloff and a tiny
     * upward lift.
     */
    private void applyRepulsion(Entity entity, Location center) {
        Vector away = entity.getLocation().toVector().subtract(center.toVector());
        double dist = away.length();
        if (dist < 1e-4)
            return;

        Vector dir = away.normalize();
        double scaled = BASE_PUSH * (RADIUS / Math.max(dist, FALLOFF_MIN_DIST));

        Vector push = dir.multiply(scaled);
        push.setY(push.getY() + LIFT);

        entity.setVelocity(entity.getVelocity().add(push));
    }

    /** Called when an entity first enters the dome. */
    private void onEnter(Entity entity, Location center) {
        if (entity == null)
            return;
        World w = center.getWorld();
        if (w != null) {
            w.spawnParticle(Particle.CRIT, entity.getLocation().add(0, 1, 0), 10, 0.2, 0.3, 0.2, 0.05);
        }
        // Optional: give brief slowness or stagger here if you like.
    }

    /** Called when an entity leaves the dome or despawns. */
    private void onLeave(Entity entity, Location center) {
        if (entity == null)
            return;
        World w = center.getWorld();
        if (w != null) {
            w.spawnParticle(Particle.SMOKE, entity.getLocation().add(0, 1, 0), 8, 0.2, 0.3, 0.2, 0.02);
        }
        // Optional: remove status tags, etc.
    }
}
