package io.github.tootertutor.ethyrial.data;

import java.util.UUID;

import org.bukkit.NamespacedKey;

public class WandLoadout {
    private final UUID playerId;
    private final String wandId;
    private final String name;
    private final NamespacedKey leftSpell;
    private final NamespacedKey rightSpell;

    public WandLoadout(UUID playerId, String wandId, String name,
            NamespacedKey leftSpell, NamespacedKey rightSpell) {
        this.playerId = playerId;
        this.wandId = wandId;
        this.name = name;
        this.leftSpell = leftSpell;
        this.rightSpell = rightSpell;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getWandId() {
        return wandId;
    }

    public String getName() {
        return name;
    }

    public NamespacedKey getLeftSpell() {
        return leftSpell;
    }

    public NamespacedKey getRightSpell() {
        return rightSpell;
    }

    @Override
    public String toString() {
        return "WandLoadout{" +
                "playerId=" + playerId +
                ", wandId='" + wandId + '\'' +
                ", name='" + name + '\'' +
                ", leftSpell=" + leftSpell +
                ", rightSpell=" + rightSpell +
                '}';
    }
}
