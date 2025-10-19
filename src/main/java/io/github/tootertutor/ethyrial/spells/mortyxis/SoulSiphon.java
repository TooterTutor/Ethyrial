package io.github.tootertutor.ethyrial.spells.mortyxis;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SoulSiphon extends Spell implements AutoRegisterSpell {

    // Core tuning
    private static final double RANGE = 12.0;
    private static final int DURATION_TICKS = 150;  // 7.5s
    private static final double DPS = 0.75;          // damage per second
    private static final double HEAL_RATIO = 0.75;   // portion of damage healed
    private static final int TICK_INTERVAL = 25;    // apply every 0.5s

    // FX tuning
    private static final double BEAM_SPACING = 0.28; // particle spacing along beam
    private static final float SOUND_VOLUME = 0.65f;
    private static final float SOUND_PITCH_HIT = 0.85f;
    private static final float SOUND_PITCH_LOOP = 1.15f;

    public SoulSiphon(Ethyrial plugin) {
        super(Ethyrial.getInstance(),
                "soulsiphon",
                "Soul Siphon",
                "Drains the essence of a single foe, healing you for a portion of the damage dealt.",
                20,
                140,
                SpellDomain.MORTYXIS);
    }

    @Override
    protected void onCast(Player caster) {
        LivingEntity target = findTarget(caster);
        if (target == null) {
            caster.sendMessage(Component.text("No valid target in range!", NamedTextColor.RED));
            fail(caster, "No valid target in range!");
            return;
        }
        startDrain(caster, target);
    }

    /**
     * Very simple target pick: first LOS living entity within RANGE. Replace with
     * ray-trace if you want strict crosshair targeting.
     */
    private LivingEntity findTarget(Player caster) {
        for (Entity entity : caster.getNearbyEntities(RANGE, RANGE, RANGE)) {
            if (!(entity instanceof LivingEntity) || entity.equals(caster))
                continue;
            if (entity.getLocation().distanceSquared(caster.getLocation()) > RANGE * RANGE)
                continue;
            if (!caster.hasLineOfSight(entity))
                continue;
            return (LivingEntity) entity;
        }
        return null;
    }

    private void startDrain(Player caster, LivingEntity target) {
        World world = caster.getWorld();

        // On-cast visual/audio cue
        onStartFX(caster, target);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!caster.isOnline() || !caster.isValid() || !target.isValid()) {
                    cancel();
                    return;
                }
                if (caster.getLocation().distanceSquared(target.getLocation()) > RANGE * RANGE) {
                    cancel();
                    return;
                }
                if (!caster.hasLineOfSight(target)) {
                    cancel();
                    return;
                }

                // Render beam each tick + a soft looping “siphon” sound every 10 ticks
                drawBeam(caster.getEyeLocation(), target.getEyeLocation());
                if (ticks % 10 == 0) {
                    world.playSound(caster.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, SOUND_VOLUME * 0.6f,
                            SOUND_PITCH_LOOP);
                }

                // Apply damage/heal + “lifesteal pop” FX at a fixed interval
                if (ticks % TICK_INTERVAL == 0) {
                    double damage = DPS * (TICK_INTERVAL / 20.0);
                    target.damage(damage, caster);

                    caster.heal(damage * HEAL_RATIO);

                    onTickFX(caster, target); // small crimson pulse + sfx
                }

                ticks++;
                if (ticks >= DURATION_TICKS)
                    cancel();
            }
        }.runTaskTimer(Ethyrial.getInstance(), 0L, 1L);
    }

    /**
     * Beam from source to target using DUST dust to get a deep crimson line.
     */
    private void drawBeam(Location from, Location to) {
        World world = from.getWorld();
        if (world == null)
            return;

        Vector diff = to.toVector().subtract(from.toVector());
        double length = diff.length();
        if (length < 1e-4)
            return;

        Vector step = diff.normalize().multiply(BEAM_SPACING);
        Location point = from.clone();

        Particle.DustOptions crimson = new Particle.DustOptions(Color.fromRGB(120, 0, 20), 1.5f);
        for (double d = 0; d < length; d += BEAM_SPACING) {
            world.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, crimson);
            // occasional soul flecks drifting toward caster
            if ((int) (d / BEAM_SPACING) % 5 == 0) {
                world.spawnParticle(Particle.SOUL, point, 1, 0.02, 0.02, 0.02, 0.001);
            }
            point.add(step);
        }
    }

    /** FX when the siphon starts. */
    private void onStartFX(Player caster, LivingEntity target) {
        World world = caster.getWorld();
        if (world == null)
            return;

        // Subtle ring at caster + wisps at target
        world.spawnParticle(Particle.WITCH, caster.getLocation().add(0, 0.6, 0), 18, 0.4, 0.4, 0.4, 0.02);
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, target.getLocation().add(0, 1.0, 0), 14, 0.2, 0.3, 0.2, 0.01);

        world.playSound(caster.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, SOUND_VOLUME, 0.8f);
        world.playSound(target.getLocation(), Sound.ENTITY_PHANTOM_FLAP, SOUND_VOLUME * 0.7f, 0.6f);
    }

    /**
     * FX each time damage/heal ticks: crimson burst at target, faint pull at
     * caster.
     */
    private void onTickFX(Player caster, LivingEntity target) {
        World world = caster.getWorld();
        if (world == null)
            return;

        // Target: red burst + faint smoke
        world.spawnParticle(
                Particle.DUST,
                target.getLocation().add(0, 1.0, 0),
                14, 0.25, 0.35, 0.25, 0,
                new Particle.DustOptions(Color.fromRGB(160, 10, 20), 1.8f));
        world.spawnParticle(Particle.SMOKE, target.getLocation().add(0, 1.0, 0), 6, 0.2, 0.2, 0.2, 0.01);

        // Caster: inward swirl hint
        world.spawnParticle(Particle.HEART, caster.getLocation().add(0, 0.9, 0), 10, 0.25, 0.25, 0.25, 0.02);

        // Soft “lifesteal pop”
        world.playSound(target.getLocation(), Sound.ENTITY_GENERIC_DRINK, SOUND_VOLUME * 0.5f, SOUND_PITCH_HIT);
        world.playSound(caster.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SOUND_VOLUME * 0.45f, 0.7f);
    }
}
