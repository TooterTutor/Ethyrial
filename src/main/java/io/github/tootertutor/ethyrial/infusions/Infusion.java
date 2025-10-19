package io.github.tootertutor.ethyrial.infusions;

import java.time.Duration;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import io.github.tootertutor.ethyrial.spells.SpellDomain;

public abstract class Infusion {
    protected final String id;
    protected final String name;
    protected final int level;
    protected final SpellDomain domain;
    protected final boolean isPermanent;

    public Infusion(String id, String name, int level, SpellDomain domain, boolean isPermanent) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.domain = domain;
        this.isPermanent = isPermanent;
    }

    public String getId() {
        return id;
    }

    public abstract void onEquip(Player player, ItemStack item);
    public abstract void onUnequip(Player player, ItemStack item);
    public abstract void onTrigger(EntityDamageByEntityEvent event, Player player, ItemStack item);
    public abstract void onTick(Player player, ItemStack item);

    public boolean isPermanent() { return isPermanent; }

    public abstract String getDescription();
    public abstract Duration getCooldown();

    public double getAffinityModifier() {
        return 0.25 * level;
    }
}
