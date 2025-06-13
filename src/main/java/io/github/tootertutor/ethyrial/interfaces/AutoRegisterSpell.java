package io.github.tootertutor.ethyrial.interfaces;

/**
 * Marker interface for automatic registration of custom spells within the plugin.
 * 
 * Implementing classes must:
 * - Extend {@code Spell}
 * - Provide a constructor with a single {@code Ethyrial} parameter
 * - Reside in {@code io.github.tootertutor.ethyrial.spells} or subpackages
 */
public interface AutoRegisterSpell {
    // Empty on purpose
}
