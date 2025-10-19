package io.github.tootertutor.ethyrial.spells.auralis;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.interfaces.AutoRegisterSpell;
import io.github.tootertutor.ethyrial.spells.Spell;
import io.github.tootertutor.ethyrial.spells.SpellDomain;

public class Shockwave extends Spell implements AutoRegisterSpell, Listener {
    private static final int LIGHTNING_RADIUS = 15;
    private static final int SHOCKWAVE_RADIUS = 3;
    private static final double PUSH_STRENGTH = 1.5;
    private static final double UPWARD_FORCE = 0.5;
    private static final double DAMAGE = 6.0;
    private static final int BLOCK_HEIGHT = 0;
    private static final int BLOCKS_PER_RING = 8;
    private static final int DELAY_BETWEEN_BLOCKS = 1;

    // Store the UUID of the caster for each lightning strike
    private Set<UUID> immunePlayers = new HashSet<>();

    public Shockwave(Ethyrial plugin) {
        super(Ethyrial.getInstance(),
                "shockwave",
                "Shock Wave",
                "Emits a burst of lightning that strikes nearby enemies.",
                40,
                240,
                SpellDomain.AURALIS);
    }

    @Override
    protected void onCast(Player caster) {
        // Add the caster to the immune list
        immunePlayers.add(caster.getUniqueId());

        // Find entities within the lightning radius
        Location center = caster.getLocation();
        caster.getWorld().getNearbyEntities(center, LIGHTNING_RADIUS, LIGHTNING_RADIUS, LIGHTNING_RADIUS)
                .stream()
                .filter(entity -> entity instanceof LivingEntity)
                .filter(entity -> !entity.getUniqueId().equals(caster.getUniqueId()))
                .forEach(entity -> {
                    // Summon lightning at the entity's location
                    Location strikeLocation = entity.getLocation();
                    caster.getWorld().strikeLightning(strikeLocation);
                });
    }

    @EventHandler
    public void onLightningStrike(LightningStrikeEvent event) {
        // Create shockwave effect at the lightning strike location
        spawnShockwave(event.getLightning().getLocation());
    }

    private void spawnShockwave(Location center) {
        new BukkitRunnable() {
            int currentRing = 1;
            Set<LivingEntity> affectedEntities = new HashSet<>();
            UUID immunePlayer = getAndRemoveImmunePlayer(); // Get the immune player for this shockwave

            @Override
            public void run() {
                if (currentRing <= SHOCKWAVE_RADIUS) {
                    spawnFallingBlocksInRing(center, currentRing, affectedEntities, immunePlayer);
                    currentRing++;
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(Ethyrial.getInstance(), 0L, DELAY_BETWEEN_BLOCKS);
    }

    private void spawnFallingBlocksInRing(Location center, int ring, Set<LivingEntity> affectedEntities,
            UUID immunePlayer) {
        for (int i = 0; i < BLOCKS_PER_RING; i++) {
            double angle = (2 * Math.PI / BLOCKS_PER_RING) * i;
            double x = ring * Math.cos(angle);
            double z = ring * Math.sin(angle);
            Location blockLocation = center.clone().add(x, BLOCK_HEIGHT, z);

            Block ground = findGround(blockLocation.getBlock());
            Block blockAbove = ground.getRelative(BlockFace.UP);

            if (blockAbove.getType().isAir()) {
                createJumpingBlock(ground, blockAbove);
            }

            // Push and damage entities in the ring
            pushAndDamageEntities(center, ring, affectedEntities, immunePlayer);
        }
    }

    private void pushAndDamageEntities(Location center, int ring, Set<LivingEntity> affectedEntities,
            UUID immunePlayer) {
        // Get all living entities near the shockwave center
        for (Entity entity : center.getWorld().getNearbyEntities(center, ring, 3, ring)) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;

                // Skip the immune player
                if (immunePlayer != null && livingEntity.getUniqueId().equals(immunePlayer)) {
                    continue;
                }

                // Skip players in Spectator or Creative mode
                if (livingEntity instanceof Player) {
                    Player player = (Player) livingEntity;
                    if (player.getGameMode().name().equals("SPECTATOR")
                            || player.getGameMode().name().equals("CREATIVE")) {
                        continue;
                    }
                }

                // Only affect each entity once
                if (affectedEntities.add(livingEntity)) {
                    // Calculate direction vector from center to entity
                    Vector direction = livingEntity.getLocation().toVector().subtract(center.toVector());

                    // Check if the vector is zero to avoid division by zero
                    if (direction.lengthSquared() > 0) {
                        direction.normalize();

                        // Apply push force
                        direction.multiply(PUSH_STRENGTH);
                        direction.setY(UPWARD_FORCE);

                        // Check if the vector values are finite before applying
                        if (Double.isFinite(direction.getX()) && Double.isFinite(direction.getY())
                                && Double.isFinite(direction.getZ())) {
                            livingEntity.setVelocity(direction);
                        }
                    }

                    // Apply damage
                    livingEntity.damage(DAMAGE);
                }
            }
        }
    }

    private Block findGround(Block block) {
        // Logic to find the ground block
        while (block.getType() == Material.AIR && block.getY() > 0) {
            block = block.getRelative(BlockFace.DOWN);
        }
        return block;
    }

    private void createJumpingBlock(Block ground, Block blockAbove) {
        Location location = blockAbove.getLocation().add(0.5, 0.0, 0.5);
        ground.getWorld().spawn(location, FallingBlock.class, fallingBlock -> {
            fallingBlock.setBlockData(ground.getBlockData());
            fallingBlock.setDropItem(false);
            fallingBlock.setCancelDrop(true);
            fallingBlock.setVelocity(new Vector(0, 0.4, 0));
            fallingBlock.setMetadata("shockwave",
                    new FixedMetadataValue(Ethyrial.getInstance(), "shockwave_block"));
        });
    }

    // Helper method to get and remove an immune player (FIFO)
    private UUID getAndRemoveImmunePlayer() {
        if (!immunePlayers.isEmpty()) {
            UUID player = immunePlayers.iterator().next();
            immunePlayers.remove(player);
            return player;
        }
        return null;
    }
}
