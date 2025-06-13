package io.github.tootertutor.ethyrial.data;

public class PlayerStats {
    private final int mana;
    private final int spellPower;
    private final int bonusHealth;

    public PlayerStats(int mana, int spellPower, int bonusHealth) {
        this.mana = mana;
        this.spellPower = spellPower;
        this.bonusHealth = bonusHealth;
    }

    public int getMana() {
        return mana;
    }

    public int getSpellPower() {
        return spellPower;
    }

    public int getBonusHealth() {
        return bonusHealth;
    }
}
