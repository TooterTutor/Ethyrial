package io.github.tootertutor.ethyrial.spells.veyruun;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.interfaces.AutoRegisterSpell;
import io.github.tootertutor.ethyrial.spells.Spell;
import io.github.tootertutor.ethyrial.spells.SpellDomain;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Two-cast Wormhole with particles & sounds:
 * 1) First cast stores origin at player's location (centered).
 * 2) Second cast sets destination and starts a 30s bidirectional portal.
 *
 * Pads do not offset the player. Teleport only occurs if:
 * - Player center is within 0.25 blocks of the pad center
 * - Per-entity cooldown elapsed (prevents ping-pong)
 * - Destination is safe (feet+head clear, solid ground)
 */
public class Wormhole extends Spell implements AutoRegisterSpell {

    // ---- Tunables (can move to config later) ----
    public static final long LIFETIME_MS = 30_000L;         // 30 seconds
    public static final long TELEPORT_COOLDOWN_MS = 1_500L; // 1.5 seconds
    public static final double TRIGGER_RADIUS = 0.5D;       // 0.25 blocks from center
    public static final long TICK_PERIOD_TICKS = 2L;        // scan/effect tick every 2 ticks

    // VFX tunables
    private static final double RING_RADIUS = 0.45D;        // ambient ring radius
    private static final int RING_POINTS = 10;              // number of points in ambient ring
    private static final int AMBIENT_PORTAL_DOTS = 2;       // small portal dots per tick
    private static final int TELEPORT_BURST = 25;           // burst count on teleport

    private final Ethyrial plugin = Ethyrial.getInstance();

    public Wormhole(Ethyrial plugin) {
        super(Ethyrial.getInstance(),
                "wormhole",
                "Wormhole",
                "Opens a temporary portal between two locations.",
                90,
                600,
                SpellDomain.VEYRUUN,
                CooldownPolicy.START_ON_COMPLETE);
        // Ensure manager exists early (safe to call multiple times)
        State.getOrCreate(this.plugin);
    }

    @Override
    protected boolean isContinuation(Player caster) {
        // If the player has a pending origin, this cast is the "second half"
        return State.getOrCreate(plugin)
                .getPendingOrigin(caster.getUniqueId())
                .isPresent();
    }

    @Override
    protected void onCast(Player caster) {
        final UUID id = caster.getUniqueId();
        final State state = State.getOrCreate(plugin);

        Optional<Location> pending = state.getPendingOrigin(id);
        if (pending.isEmpty()) {
            // First cast: anchor origin at exact block center, same Y
            Location origin = centerToBlock(caster.getLocation());
            state.beginOrigin(id, origin);

            // FX: subtle anchor cue
            fxAnchor(caster.getWorld(), origin);
            caster.sendMessage(Component.text("Wormhole Origin set.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, true));
            return;
        }

        // Second cast: set destination and start pair
        Location destination = centerToBlock(caster.getLocation());
        boolean ok = state.finishAndStartPair(id, pending.get(), destination);
        if (ok) {
            // FX: opening at both pads
            fxOpen(destination.getWorld(), pending.get());
            fxOpen(destination.getWorld(), destination);
            caster.sendMessage(Component.text("Wormhole will collapse in 30 seconds!", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, true));
            markCompleted(caster);

        } else {
            caster.sendMessage(Component.text("The Wormhole cannot be set here.", NamedTextColor.RED));
        }
    }

    // ---- helpers ----

    private static Location centerToBlock(Location loc) {
        return new Location(
                loc.getWorld(),
                Math.floor(loc.getX()) + 0.5,
                loc.getY(),
                Math.floor(loc.getZ()) + 0.5,
                loc.getYaw(),
                loc.getPitch());
    }

    // --- Simple FX helpers (kept minimal & inline) ---

    private static void fxAnchor(World w, Location at) {
        if (w == null)
            return;
        w.playSound(at, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.6f, 1.3f);
        w.spawnParticle(Particle.END_ROD, at.clone().add(0, 1.0, 0), 12, 0.15, 0.3, 0.15, 0.01);
    }

    private static void fxOpen(World w, Location at) {
        if (w == null)
            return;
        w.playSound(at, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.15f);
        w.playSound(at, Sound.BLOCK_PORTAL_AMBIENT, 0.3f, 0.9f);
        w.spawnParticle(Particle.PORTAL, at, 30, 0.25, 0.5, 0.25, 0.02);
        w.spawnParticle(Particle.END_ROD, at, 18, 0.25, 0.5, 0.25, 0.015);
    }

    private static void fxTeleportBurst(World w, Location at) {
        if (w == null)
            return;
        w.playSound(at, Sound.ENTITY_ENDERMAN_TELEPORT, 0.9f, 1.0f);
        w.spawnParticle(Particle.PORTAL, at, TELEPORT_BURST, 0.35, 0.6, 0.35, 0.05);
        w.spawnParticle(Particle.END_ROD, at, TELEPORT_BURST / 2, 0.25, 0.5, 0.25, 0.02);
    }

    private static void fxCollapse(World w, Location at) {
        if (w == null)
            return;
        w.playSound(at, Sound.BLOCK_BEACON_DEACTIVATE, 0.8f, 0.8f);
        w.spawnParticle(Particle.SMOKE, at, 10, 0.25, 0.4, 0.25, 0.0);
    }

    /**
     * All runtime state and logic bundled inside the spell file for low external
     * coupling.
     */
    private static final class State {

        private static State INSTANCE;

        static State getOrCreate(Ethyrial plugin) {
            if (INSTANCE == null) {
                INSTANCE = new State(plugin);
            }
            return INSTANCE;
        }

        private final Ethyrial plugin;

        // building state (per-caster first cast)
        private final Map<UUID, Location> pendingOrigin = new ConcurrentHashMap<>();

        // active pairs keyed by "owner caster"
        private final Map<UUID, PortalPair> activePairs = new ConcurrentHashMap<>();

        // per-entity cooldown to prevent ping-pong
        private final Map<UUID, Long> lastTeleportAt = new ConcurrentHashMap<>();

        private State(Ethyrial plugin) {
            this.plugin = plugin;
        }

        Optional<Location> getPendingOrigin(UUID casterId) {
            return Optional.ofNullable(pendingOrigin.get(casterId));
        }

        void beginOrigin(UUID casterId, Location origin) {
            pendingOrigin.put(casterId, origin.clone());
        }

        boolean finishAndStartPair(UUID casterId, Location origin, Location destination) {
            pendingOrigin.remove(casterId);

            if (origin == null || destination == null)
                return false;
            if (origin.getWorld() == null || destination.getWorld() == null)
                return false;

            // Replace any previous pair owned by this caster
            destroyPair(casterId, true);

            final PortalPair pair = new PortalPair(origin.clone(), destination.clone(),
                    System.currentTimeMillis() + LIFETIME_MS);

            // periodic scan + ambient
            pair.scanTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> tickPair(pair), TICK_PERIOD_TICKS,
                    TICK_PERIOD_TICKS);

            // hard expiry
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Collapse FX at both ends (defensive world-null checks inside)
                fxCollapse(origin.getWorld(), origin);
                fxCollapse(destination.getWorld(), destination);
                destroyPair(casterId, true);
            }, LIFETIME_MS / 50L);

            activePairs.put(casterId, pair);
            return true;
        }

        void destroyPair(UUID casterId, boolean quietly) {
            PortalPair existing = activePairs.remove(casterId);
            if (existing != null) {
                if (existing.scanTask != null)
                    existing.scanTask.cancel();
                if (!quietly) {
                    fxCollapse(existing.origin.getWorld(), existing.origin);
                    fxCollapse(existing.destination.getWorld(), existing.destination);
                }
            }
        }

        // ---- core loop ----

        private void tickPair(PortalPair pair) {
            // Expire defensively
            if (System.currentTimeMillis() >= pair.expiresAt) {
                UUID owner = ownerOf(pair);
                if (owner != null)
                    destroyPair(owner, true);
                return;
            }

            // Ambient visuals
            pair.ticks += TICK_PERIOD_TICKS;
            ambientRing(pair.origin, pair.ticks);
            ambientRing(pair.destination, pair.ticks);

            // Check both directions
            checkPadTeleport(pair.origin, pair.destination);
            checkPadTeleport(pair.destination, pair.origin);
        }

        private UUID ownerOf(PortalPair p) {
            for (Map.Entry<UUID, PortalPair> e : activePairs.entrySet()) {
                if (e.getValue() == p)
                    return e.getKey();
            }
            return null;
        }

        private void checkPadTeleport(Location fromPad, Location toPad) {
            World w = fromPad.getWorld();
            if (w == null)
                return;

            final double triggerRadiusSq = TRIGGER_RADIUS * TRIGGER_RADIUS;

            for (Player player : w.getPlayers()) {
                Location loc = player.getLocation();
                if (loc.getWorld() != w)
                    continue;
                if (loc.distanceSquared(fromPad) > triggerRadiusSq)
                    continue;

                long now = System.currentTimeMillis();
                long last = lastTeleportAt.getOrDefault(player.getUniqueId(), 0L);
                if (now - last < TELEPORT_COOLDOWN_MS)
                    continue;

                if (!isSafeDestination(toPad))
                    continue;

                // Teleport exactly to pad center; preserve yaw/pitch
                Location target = new Location(
                        toPad.getWorld(),
                        toPad.getX(), toPad.getY(), toPad.getZ(),
                        loc.getYaw(), loc.getPitch());

                if (player.teleport(target)) {
                    lastTeleportAt.put(player.getUniqueId(), now);
                    fxTeleportBurst(fromPad.getWorld(), fromPad);
                    fxTeleportBurst(toPad.getWorld(), toPad);
                }
            }
        }

        // ---- safety check: feet & head passable, solid ground below, no liquids, no
        // hostile blocks, no tight entity overlap ----

        private boolean isSafeDestination(Location destination) {
            World world = destination.getWorld();
            if (world == null)
                return false;

            Block feet = world.getBlockAt(destination);
            Block head = world.getBlockAt(destination.clone().add(0, 1, 0));
            Block below = world.getBlockAt(destination.clone().add(0, -1, 0));

            if (!feet.isPassable() || feet.isLiquid())
                return false;
            if (!head.isPassable() || head.isLiquid())
                return false;
            if (!below.getType().isSolid() || below.getType().isAir())
                return false;

            switch (feet.getType()) {
                case CACTUS, CAMPFIRE, SOUL_CAMPFIRE, FIRE, SOUL_FIRE, MAGMA_BLOCK -> {
                    return false;
                }
                default -> {
                    /* ok */ }
            }

            Collection<LivingEntity> near = world.getNearbyLivingEntities(destination, 0.3, 1.0, 0.3);
            return near.isEmpty();
        }

        // ---- ambient ring / portal wisps ----

        private void ambientRing(Location center, long ticks) {
            World world = center.getWorld();
            if (world == null)
                return;

            // Subtle portal wisps
            world.spawnParticle(Particle.PORTAL, center, AMBIENT_PORTAL_DOTS, 0.15, 0.4, 0.15, 0.01);

            // Rotating flat ring of end-rod particles
            double tick = (ticks % 200) / 200.0; // 0..1
            double phase = tick * Math.PI * 2.0;

            // Keep ring flat at a constant Y offset (e.g., 0.05 above pad center)
            double yFlat = center.getY() + 0.05;

            for (int i = 0; i < RING_POINTS; i++) {
                double a = phase + (i * (Math.PI * 2.0 / RING_POINTS));
                double x = center.getX() + Math.cos(a) * RING_RADIUS;
                double z = center.getZ() + Math.sin(a) * RING_RADIUS;

                world.spawnParticle(Particle.END_ROD, x, yFlat, z, 1, 0, 0, 0, 0.0);
            }
        }

        // ---- data ----
        private static final class PortalPair {
            final Location origin;
            final Location destination;
            final long expiresAt;
            long ticks = 0L;
            BukkitTask scanTask;

            PortalPair(Location origin, Location destination, long expiresAt) {
                this.origin = origin;
                this.destination = destination;
                this.expiresAt = expiresAt;
            }
        }
    }
}
