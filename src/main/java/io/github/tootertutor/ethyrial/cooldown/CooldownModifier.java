package io.github.tootertutor.ethyrial.cooldown;

import java.time.Duration;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public interface CooldownModifier {
    int priority();

    Duration modify(Player player, NamespacedKey spellKey, Duration current, CooldownContext ctx);
}
