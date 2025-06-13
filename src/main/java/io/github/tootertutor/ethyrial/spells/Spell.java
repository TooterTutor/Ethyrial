package io.github.tootertutor.ethyrial.spells;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.Ethyrial;

public abstract class Spell implements Keyed {
    protected final Ethyrial plugin;
    protected NamespacedKey key;
    protected String name;
    protected String description;
    protected int manaCost;
    protected int cooldownTicks;
    protected SpellDomain domain;

    protected Spell(Ethyrial plugin, NamespacedKey key, String name, String description, int manaCost,
            int cooldownTicks, SpellDomain domain) {
        this.plugin = plugin;
        this.key = key;
        this.name = name;
        this.description = description;
        this.manaCost = manaCost;
        this.cooldownTicks = cooldownTicks;
        this.domain = domain;
    }

    public abstract void cast(Player caster);

    public NamespacedKey getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getManaCost() {
        return manaCost;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public SpellDomain getDomain() {
        return domain;
    }
}
