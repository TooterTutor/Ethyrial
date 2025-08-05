package io.github.tootertutor.ethyrial.spells;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.registry.SpellRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SpellUtils {
    public static NamespacedKey toKey(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        String[] parts = id.split(":");
        if (parts.length != 2) {
            return null;
        }
        return new NamespacedKey(Ethyrial.getInstance(), parts[1]); // plugin reference from singleton
    }

    public static NamespacedKey toPluginKey(String id) {
        String[] parts = id.split(":", 2);
        if (parts.length != 2 || !parts[0].equals("ethyrial"))
            return null;
        return new NamespacedKey(Ethyrial.getInstance(), parts[1]);
    }

    public static void castByKey(NamespacedKey key, Player player) {
        if (key == null) {
            player.sendMessage(Component.text("No spell bound to this slot!", NamedTextColor.RED));
            return;
        }
        
        Spell spell = SpellRegistry.get(key);

        if (spell != null) {
            spell.cast(player);
        } else {
            player.sendMessage(Component.text("No spell bound to this slot!", NamedTextColor.RED));
        }
    }
}
