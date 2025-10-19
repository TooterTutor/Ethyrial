package io.github.tootertutor.ethyrial.infusions;

import java.time.Duration;

public class InfusionInstance {

    private final Infusion infusion;
    private int usesRemaining; // Only used for temporary infusions
    private long lastUsed; // Epoch millis
    private final boolean isPermanent; // Whether the infusion is permanent or not
    private int level;

    public InfusionInstance(Infusion infusion, boolean isPermanent, int usesRemaining) {
        this.infusion = infusion;
        this.isPermanent = isPermanent;
        this.usesRemaining = usesRemaining;
        this.lastUsed = 0;
    }

    public Infusion setInfusion() {
        return infusion;
    }

    public Infusion getInfusion() {
        return infusion;
    }

    public boolean isPermanent() {
        return isPermanent;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getUsesRemaining() {
        return usesRemaining;
    }

    public void decrementUses() {
        if (!isPermanent && usesRemaining > 0) {
            usesRemaining--;
        }
    }

    public boolean isOnCooldown() {
        Duration cooldown = infusion.getCooldown();
        return System.currentTimeMillis() - lastUsed < cooldown.toMillis();
    }

    public void markUsed() {
        this.lastUsed = System.currentTimeMillis();
    }

    public Duration getCooldown() {
        return infusion.getCooldown();
    }
}
