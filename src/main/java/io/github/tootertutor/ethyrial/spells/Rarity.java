package io.github.tootertutor.ethyrial.spells;

public enum Rarity {
    COMMON("Common"),
    RARE("Rare"),
    EPIC("Epic"),
    LEGENDARY("Legendary");

    private final String displayName;

    Rarity(String name) {
        this.displayName = name;
    }

    public String getDisplayName() {
        return displayName;
    }
}
