package io.github.tootertutor.ethyrial.spells.veyruun;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.interfaces.AutoRegisterSpell;
import io.github.tootertutor.ethyrial.spells.Spell;
import io.github.tootertutor.ethyrial.spells.SpellDomain;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Blink extends Spell implements AutoRegisterSpell {
    public Blink(Ethyrial plugin) {
        super(Ethyrial.getInstance(),
                "blink",
                "Blink",
                "Instantly teleport a short distance in the direction you're facing.",
                20,
                40,
                SpellDomain.VEYRUUN,
                CooldownPolicy.START_ON_CAST);
    }

    @Override
    protected void onCast(Player caster) {
        Vector direction = caster.getLocation().getDirection().normalize().multiply(5);
        Location destination = caster.getLocation().add(direction);

        // Check if destination is safe (not inside blocks)
        if (!isLocationSafe(destination)) {
            caster.sendMessage(Component.text("Cannot blink there - destination is blocked!", NamedTextColor.RED));
            caster.playSound(caster.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            fail(caster, "Cannot blink there - destination is blocked!");
            return;
        }

        // Play teleport sound at both locations
        caster.playSound(caster.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);

        // Teleport the player
        caster.teleport(destination);

        // Play teleport sound and particles at destination
        caster.playSound(destination, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
        destination.getWorld().spawnParticle(Particle.PORTAL, destination, 20, 0.5, 1.0, 0.5, 0.1);
    }

    private boolean isLocationSafe(Location location) {
        // Check if the destination has enough space for a player (2 blocks high)
        Location ground = location.clone();
        ground.setY(Math.floor(ground.getY()));

        Location head = ground.clone().add(0, 1, 0);
        Location feet = ground.clone();

        // Check if both feet and head positions are not solid blocks
        Material feetBlock = feet.getBlock().getType();
        Material headBlock = head.getBlock().getType();

        return !feetBlock.isSolid() && !headBlock.isSolid();
    }
}
