package io.github.tootertutor.ethyrial.config;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import io.github.tootertutor.ethyrial.Ethyrial;

public interface Effect {
    String type(); // e.g., "damage", "particle", "teleport"

    /** Build an executable effect instance from config values */
    EffectInstance build(Ethyrial plugin, Map<String, Object> cfg) throws IllegalArgumentException;

    interface EffectInstance {
        void run(Ethyrial plugin, Player caster, Event rawEvent);
    }
}
