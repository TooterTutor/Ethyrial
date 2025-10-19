package io.github.tootertutor.ethyrial.spells.auralis;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.interfaces.AutoRegisterSpell;
import io.github.tootertutor.ethyrial.spells.Spell;
import io.github.tootertutor.ethyrial.spells.SpellDomain;

public class FlameLash extends Spell implements AutoRegisterSpell {
    
    private static final double DAMAGE = 10.0; // 5 hearts
    private static final double RANGE = 8.0;
    private static final double WHIP_WIDTH = 2.0;
    private static final int PARTICLE_COUNT = 30;
    
    public FlameLash(Ethyrial plugin) {
        super(Ethyrial.getInstance(),
                "flamelash",
                "Flame Lash",
                "Whips enemies with a searing lash of fire, dealing damage and leaving a burning trail.",
                20,
                80,
                SpellDomain.AURALIS);
    }

    @Override
    protected void onCast(Player caster) {
        Location casterLocation = caster.getEyeLocation();
        Vector direction = casterLocation.getDirection().normalize();
        
        // Play cast sound
        caster.getWorld().playSound(casterLocation, Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.5f);
        
        // Track entities already hit to prevent multiple damage
        Set<UUID> hitEntities = new HashSet<>();
        
        // Generate random control points for Bezier curve
        long seed = caster.getUniqueId().hashCode() ^ System.currentTimeMillis();
        java.util.Random random = new java.util.Random(seed);
        
        // Create 2-3 random control points for the whip curve
        Vector right = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
        Vector up = new Vector(0, 1, 0);
        
        // Random control points for the Bezier curve - reduced upward bias
        Vector control1 = direction.clone()
            .multiply(RANGE * 0.33)
            .add(right.clone().multiply((random.nextDouble() - 0.5) * 5))
            .add(up.clone().multiply((random.nextDouble() - 0.5) * 2));
            
        Vector control2 = direction.clone()
            .multiply(RANGE * 0.66)
            .add(right.clone().multiply((random.nextDouble() - 0.5) * 5))
            .add(up.clone().multiply((random.nextDouble() - 0.5) * 2));
        
        // Create the flame whip effect with randomized path
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double progress = (double) i / PARTICLE_COUNT;
            
            // Calculate position using cubic Bezier curve
            Vector start = new Vector(0, 0, 0);
            Vector end = direction.clone().multiply(RANGE);
            
            // Cubic Bezier formula: (1-t)^3*P0 + 3(1-t)^2*t*P1 + 3(1-t)*t^2*P2 + t^3*P3
            double t = progress;
            double t2 = t * t;
            double t3 = t2 * t;
            double mt = 1 - t;
            double mt2 = mt * mt;
            double mt3 = mt2 * mt;
            
            Vector offset = start.clone().multiply(mt3)
                .add(control1.clone().multiply(3 * mt2 * t))
                .add(control2.clone().multiply(3 * mt * t2))
                .add(end.clone().multiply(t3));
            
            Location particleLocation = casterLocation.clone().add(offset);
            
            // Spawn flame particles
            caster.getWorld().spawnParticle(
                Particle.FLAME, 
                particleLocation, 
                2, 
                0.1, 0.1, 0.1, 
                0.02
            );
            
            // Spawn smoke particles for trail effect
            if (i % 3 == 0) {
                caster.getWorld().spawnParticle(
                    Particle.SMOKE,
                    particleLocation,
                    1,
                    0.2, 0.2, 0.2,
                    0.01
                );
            }
            
            // Check for entities at this position
            for (Entity entity : particleLocation.getWorld().getNearbyEntities(
                particleLocation, 
                WHIP_WIDTH / 2, 
                1.0, 
                WHIP_WIDTH / 2
            )) {
                if (entity instanceof LivingEntity && 
                    !entity.getUniqueId().equals(caster.getUniqueId()) && 
                    !hitEntities.contains(entity.getUniqueId())) {
                    
                    LivingEntity livingEntity = (LivingEntity) entity;
                    
                    // Add fire effect
                    livingEntity.setFireTicks(60); // 3 seconds of fire
                    
                    // Apply damage
                    livingEntity.damage(DAMAGE, caster);
                    
                    // Play hit sound
                    livingEntity.getWorld().playSound(
                        livingEntity.getLocation(), 
                        Sound.ENTITY_BLAZE_HURT, 
                        1.0f, 
                        1.0f
                    );
                    
                    hitEntities.add(entity.getUniqueId());
                }
            }
        }
        
        // Create a final burst effect at the end of the whip
        Location endLocation = casterLocation.clone().add(direction.multiply(RANGE));
        caster.getWorld().spawnParticle(
            Particle.EXPLOSION,
            endLocation,
            1,
            0, 0, 0,
            0
        );
        
        caster.getWorld().playSound(endLocation, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.8f, 1.2f);
    }
}
