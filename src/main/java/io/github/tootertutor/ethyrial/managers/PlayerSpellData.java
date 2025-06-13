package io.github.tootertutor.ethyrial.managers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.NamespacedKey;

public class PlayerSpellData {
    private final Set<NamespacedKey> unlockedSpells = new HashSet<>();
    
    public void unlockSpell(NamespacedKey key) {
        unlockedSpells.add(key);
    }

    public boolean hasUnlocked(NamespacedKey key) {
        return unlockedSpells.contains(key);
    }

    public Set<NamespacedKey> getUnlockedSpells() {
        return Collections.unmodifiableSet(unlockedSpells);
    }
}
