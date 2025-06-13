package io.github.tootertutor.ethyrial.registry;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.NamespacedKey;

import io.github.tootertutor.ethyrial.spells.Spell;

public class SpellRegistry {
    private static final Map<NamespacedKey, Spell> spells = new HashMap<>();

    public static void register(Spell spell) {
        spells.put(spell.getKey(), spell);
    }

    public static Spell get(NamespacedKey key) {
        return spells.get(key);
    }

    public static Iterable<Spell> all() {
        return spells.values();
    }
}
