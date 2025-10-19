package io.github.tootertutor.ethyrial.registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.github.tootertutor.ethyrial.infusions.Infusion;

public class InfusionRegistry {

    private static final Map<String, Infusion> REGISTERED_INFUSIONS = new HashMap<>();

    public static void register(Infusion infusion) {
        REGISTERED_INFUSIONS.put(infusion.getId(), infusion);
    }

    public static Infusion get(String id) {
        return REGISTERED_INFUSIONS.get(id.toLowerCase());
    }

    public static boolean isRegistered(String id) {
        return REGISTERED_INFUSIONS.containsKey(id.toLowerCase());
    }

    public static Collection<Infusion> all() {
        return REGISTERED_INFUSIONS.values();
    }

}
