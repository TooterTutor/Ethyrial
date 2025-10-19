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

public class Blackhole extends Spell implements AutoRegisterSpell {

    // Tunables
    private static final double RADIUS = 10.0;
    private static final int DURATION_TICKS = 200;  // 10s
    private static final double BASE_PULL = 0.10;   // overall gravity strength
    private static final double SWIRL = 0.30;       // tangential “orbit” amount

    public Blackhole(Ethyrial plugin) {
        super(Ethyrial.getInstance(),
                "blackhole",
                "Black Hole",
                "Generates a field that draws mobs inwards with immense force.",
                60,
                200,
                SpellDomain.VEYRUUN);
    }

    @Override
    protected void onCast(Player caster) {
        // Fix: don’t mutate the original Location via add(); use clones.
        Location casterLoc = caster.getLocation();
        Vector dir = casterLoc.getDirection().normalize();
        Location blackHoleLocation = casterLoc.clone()
                .add(dir.clone().multiply(-2)) // 2 blocks behind
                .add(0, 3, 0); // 3 blocks above

        showBlackHoleEffect(blackHoleLocation);
        startField(blackHoleLocation, caster);
    }

    private void showBlackHoleEffect(Location center) {
        World world = center.getWorld();
        if (world == null)
            return;

        // Core “singularity”
        for (int i = 0; i < 20; i++) {
            double radius = 0.5;
            double x = (Math.random() * 2 - 1) * radius;
            double y = (Math.random() * 2 - 1) * radius;
            double z = (Math.random() * 2 - 1) * radius;
            double dist = Math.sqrt(x * x + y * y + z * z);
            if (dist > 0) {
                x = x / dist * radius;
                y = y / dist * radius;
                z = z / dist * radius;
            }
            world.spawnParticle(Particle.SMOKE, center.clone().add(x, y, z), 1, 0, 0, 0, 0);
        }

        // AoE boundary sphere
        for (int i = 0; i < 40; i++) {
            double theta = Math.random() * 2 * Math.PI;
            double phi = Math.acos(2 * Math.random() - 1);
            double x = RADIUS * Math.sin(phi) * Math.cos(theta);
            double y = RADIUS * Math.cos(phi);
            double z = RADIUS * Math.sin(phi) * Math.sin(theta);
            world.spawnParticle(Particle.DUST, center.clone().add(x, y, z), 1, 0, 0, 0, 1,
                    new Particle.DustOptions(Color.BLACK, 1));
        }
    }

    /** Runs the gravity + enter/leave tracking loop. */
    private void startField(Location center, Player caster) {
        World world = center.getWorld();
        if (world == null)
            return;

        // Tracks who is currently inside
        Set<UUID> inside = new HashSet<>();

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!center.isWorldLoaded()) {
                    cancel();
                    return;
                }

                // Visuals
                showBlackHoleEffect(center);

                // Query current entities each tick (bounding box cube big enough to cover the
                // sphere).
                // Filter to LivingEntity (skip armor stands/items), valid, and not the caster.
                Collection<Entity> current = world.getNearbyEntities(
                        center, RADIUS, RADIUS, RADIUS,
                        e -> e instanceof LivingEntity
                                && e.isValid()
                                && !e.equals(caster)
                                && e.getLocation().distanceSquared(center) <= RADIUS * RADIUS);

                // Build a Set<UUID> for quick diff
                Set<UUID> now = new HashSet<>();
                for (Entity e : current)
                    now.add(e.getUniqueId());

                // Detect ENTER: now - inside
                for (Entity e : current) {
                    UUID id = e.getUniqueId();
                    if (!inside.contains(id)) {
                        inside.add(id);
                        onEnter(e, center);
                    }
                }

                // Detect LEAVE: inside - now
                // Copy to avoid concurrent modification
                Set<UUID> toCheckLeaves = new HashSet<>(inside);
                for (UUID id : toCheckLeaves) {
                    if (!now.contains(id)) {
                        inside.remove(id);
                        // Try to resolve the entity; may be null if despawned — that still counts as
                        // leaving.
                        Entity left = world.getEntity(id);
                        onLeave(left, center);
                    }
                }

                // Apply forces to CURRENT entities
                for (Entity entity : current) {
                    applyGravity(entity, center);
                }

                // End condition
                ticks++;
                if (ticks >= DURATION_TICKS) {
                    // Send “leave” for anyone still inside (optional, but often useful for
                    // cleanup).
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

    /** Gravity + swirl force application. */
    private void applyGravity(Entity entity, Location center) {
        Location eLoc = entity.getLocation();
        Vector toCenter = center.clone().subtract(eLoc).toVector();
        double dist = toCenter.length();
        if (dist <= 0.0001)
            return;

        Vector dir = toCenter.clone().normalize();

        // Pull gets stronger when closer, capped by a minimum distance to avoid
        // divide-by-zero.
        double force = BASE_PULL * (RADIUS / Math.max(dist, 1.0));
        Vector pull = dir.multiply(force);

        // Tangential swirl (rotate around Y axis)
        Vector tangential = new Vector(-dir.getZ(), 0, dir.getX()).multiply(SWIRL);

        entity.setVelocity(pull.add(tangential));
    }

    /** Called the tick an entity crosses from outside -> inside the sphere. */
    private void onEnter(Entity entity, Location center) {
        if (entity == null)
            return;
        // Example feedback: subtle particles and a soft “whoosh”
        World w = center.getWorld();
        if (w != null) {
            w.spawnParticle(Particle.PORTAL, entity.getLocation().add(0, 1, 0), 10, 0.2, 0.4, 0.2, 0.1);
            // You can also play a sound here if desired
            // w.playSound(entity.getLocation(), Sound.ITEM_TRIDENT_RETURN, 0.5f, 1.4f);
        }
        // Optional: tag the entity via metadata/PDC if you want other systems to react.
    }

    /** Called the tick an entity leaves the sphere (or despawns/logs out). */
    private void onLeave(Entity entity, Location center) {
        if (entity == null)
            return;
        World w = center.getWorld();
        if (w != null) {
            w.spawnParticle(Particle.SMOKE, entity.getLocation().add(0, 1, 0), 8, 0.2, 0.3, 0.2, 0.02);
        }
        // Optional: remove tags, cancel effects, etc.
    }
}
