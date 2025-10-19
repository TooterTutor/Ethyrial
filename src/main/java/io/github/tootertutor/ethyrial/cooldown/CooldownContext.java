package io.github.tootertutor.ethyrial.cooldown;

import java.util.Map;

/**
 * Optional bag of info for modifiers to reference (spell power, perks, etc.).
 */
public record CooldownContext(Map<String, Object> data) {
    public static CooldownContext empty() {
        return new CooldownContext(Map.of());
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object v = data.get(key);
        return type.isInstance(v) ? (T) v : null;
    }
}
