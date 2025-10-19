package io.github.tootertutor.ethyrial.registry;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import io.github.tootertutor.ethyrial.config.Effect;

public final class EffectRegistry {
    private final Map<String, Effect> effects = new HashMap<>();

    public EffectRegistry register(Effect e) {
        effects.put(e.type().toLowerCase(Locale.ROOT), e);
        return this;
    }

    public Optional<Effect> find(String type) {
        return Optional.ofNullable(effects.get(type.toLowerCase(Locale.ROOT)));
    }

    public static EffectRegistry defaultRegistry() {
        return new EffectRegistry();
    }
}
