package io.github.tootertutor.ethyrial.data;

import java.util.UUID;

import org.bukkit.NamespacedKey;

public class SpellSlots {
    private final UUID uuid;

    private NamespacedKey selectedSpell;

    public SpellSlots(UUID uuid, NamespacedKey selectedSpell) {
        this.uuid = uuid;
        this.selectedSpell = selectedSpell;
    }

    public UUID getUuid() {
        return uuid;
    }

    public NamespacedKey getSelectedSpell() {
        return selectedSpell;
    }
}
