package io.github.tootertutor.ethyrial.spells.noctyra;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.interfaces.AutoRegisterSpell;
import io.github.tootertutor.ethyrial.spells.Spell;
import io.github.tootertutor.ethyrial.spells.SpellDomain;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class MentalLink extends Spell implements AutoRegisterSpell {

    private static final double MAX_RANGE = 10.0;
    private static final int LINK_DURATION = 600; // 30 seconds

    public MentalLink(Ethyrial plugin) {
        super(Ethyrial.getInstance(),
                "mentallink",
                "Mental Link",
                "Tethers you to a target, sharing damage between both of you.",
                50,
                800,
                SpellDomain.NOCTYRA);
    }

    @Override
    protected void onCast(Player caster) {
        // Ray trace to find target entity
        Location eyeLocation = caster.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        RayTraceResult result = caster.getWorld().rayTrace(
                eyeLocation,
                direction,
                MAX_RANGE,
                FluidCollisionMode.NEVER,
                true,
                0.5,
                entity -> entity instanceof LivingEntity && entity != caster);

        if (result != null && result.getHitEntity() != null) {
            LivingEntity target = (LivingEntity) result.getHitEntity();

            // Visual effects
            caster.getWorld().spawnParticle(
                    Particle.WITCH,
                    caster.getLocation().add(0, 1, 0),
                    20,
                    0.5,
                    0.5,
                    0.5,
                    0.1);

            target.getWorld().spawnParticle(
                    Particle.WITCH,
                    target.getLocation().add(0, 1, 0),
                    20,
                    0.5,
                    0.5,
                    0.5,
                    0.1);

            caster.playSound(caster.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.5f);
            caster.sendMessage(Component.text("Mental link established with " + target.getName() + "!",
                    NamedTextColor.DARK_PURPLE, TextDecoration.ITALIC));

        } else {
            caster.sendMessage(Component.text("No valid target found!", NamedTextColor.RED));
            caster.playSound(caster.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {

        // double damage = event.getFinalDamage();
        // double remainingHealth = Math.max(0, temp.getHealth() - damage);
        // linked.setHealth(remainingHealth);

        // Halve the damage taken by the originally hit entity
        // event.setDamage(damage);
    }
}