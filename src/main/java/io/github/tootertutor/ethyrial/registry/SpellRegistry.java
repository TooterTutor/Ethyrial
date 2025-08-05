package io.github.tootertutor.ethyrial.registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.NamespacedKey;

import io.github.tootertutor.ethyrial.spells.Spell;
import io.github.tootertutor.ethyrial.spells.SpellUtils;

public class SpellRegistry {
    private static final Map<NamespacedKey, Spell> spells = new HashMap<>();

    public static void register(Spell spell) {
        spells.put(spell.getKey(), spell);
    }

    public static NamespacedKey toKey(String id) {
        if (id == null || !id.contains(":"))
            return null;
        String[] parts = id.split(":");
        if (parts.length != 2)
            return null;
        return new NamespacedKey(parts[0], parts[1]);
    }

    public static Spell get(NamespacedKey key) {
        return spells.get(key);
    }

    public static Spell get(String id) {
        NamespacedKey key = SpellUtils.toKey(id);
        return spells.get(key);
    }

    public static Collection<Spell> all() {
        return spells.values();
    }
}
