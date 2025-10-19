package io.github.tootertutor.ethyrial.data;

import java.util.Objects;

public final class PlayerStats {
    // Core pools
    private final int mana;
    private final int maxMana;
    private final int manaRegen;

    // Combat
    private final int healthBonus; // extra hearts in half-hearts (1.0 heart = 2)
    private final int strength; // melee scaling
    private final int agility; // movement/dodge/etc
    private final int defense; // flat or % DR
    private final int spellPower; // boosts spells

    private final int critChance; // in percent (0–100)
    private final int critDamage; // in percent extra (e.g., 50 = +50%)

    private PlayerStats(Builder builder) {
        this.mana = builder.mana;
        this.maxMana = builder.maxMana;
        this.manaRegen = builder.manaRegen;
        this.healthBonus = builder.healthBonus;
        this.strength = builder.strength;
        this.agility = builder.agility;
        this.defense = builder.defense;
        this.spellPower = builder.spellPower;
        this.critChance = builder.critChance;
        this.critDamage = builder.critDamage;
    }

    public int getMana() {
        return mana;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public int getManaRegen() {
        return manaRegen;
    }

    public int getHealthBonus() {
        return healthBonus;
    }

    public int getStrength() {
        return strength;
    }

    public int getAgility() {
        return agility;
    }

    public int getDefense() {
        return defense;
    }

    public int getSpellPower() {
        return spellPower;
    }

    public int getCritChance() {
        return critChance;
    }

    public int getCritDamage() {
        return critDamage;
    }

    public Builder toBuilder() {
        return new Builder()
                .mana(mana).maxMana(maxMana).manaRegen(manaRegen)
                .healthBonus(healthBonus)
                .strength(strength).agility(agility).defense(defense)
                .spellPower(spellPower)
                .critChance(critChance).critDamage(critDamage);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (!(object instanceof PlayerStats))
            return false;
        PlayerStats playerStats = (PlayerStats) object;
        return mana == playerStats.mana && maxMana == playerStats.maxMana && manaRegen == playerStats.manaRegen &&
                healthBonus == playerStats.healthBonus && strength == playerStats.strength &&
                agility == playerStats.agility && defense == playerStats.defense && spellPower == playerStats.spellPower
                &&
                critChance == playerStats.critChance && critDamage == playerStats.critDamage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mana, maxMana, manaRegen, healthBonus, strength,
                agility, defense, spellPower, critChance, critDamage);
    }

    public static class Builder {
        private int mana;
        private int maxMana;
        private int manaRegen;
        private int healthBonus;
        private int strength;
        private int agility;
        private int defense;
        private int spellPower;
        private int critChance;
        private int critDamage;

        public Builder mana(int v) {
            this.mana = v;
            return this;
        }

        public Builder maxMana(int v) {
            this.maxMana = v;
            return this;
        }

        public Builder manaRegen(int v) {
            this.manaRegen = v;
            return this;
        }

        public Builder healthBonus(int v) {
            this.healthBonus = v;
            return this;
        }

        public Builder strength(int v) {
            this.strength = v;
            return this;
        }

        public Builder agility(int v) {
            this.agility = v;
            return this;
        }

        public Builder defense(int v) {
            this.defense = v;
            return this;
        }

        public Builder spellPower(int v) {
            this.spellPower = v;
            return this;
        }

        public Builder critChance(int v) {
            this.critChance = v;
            return this;
        }

        public Builder critDamage(int v) {
            this.critDamage = v;
            return this;
        }

        public PlayerStats build() {
            return new PlayerStats(this);
        }
    }
}
