package io.github.tootertutor.ethyrial.infusions;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.tootertutor.ethyrial.spells.SpellDomain;

public class InfusedItemData {
    private final Map<String, InfusionInstance> infusions = null;

    public InfusionInstance getInfusion(String id) {
        return infusions.get(id);
    }

    public void addOrReplaceInfusion(String id, InfusionInstance instance) {
        infusions.put(id, instance);
    }

    public void setInfusionLevel(String id, int level) {
        InfusionInstance instance = infusions.get(id);
        if (instance != null) {
            instance.setLevel(level);
        }
    }

    public Map<SpellDomain, Double> getAffinityBonuses() {
        return infusions.values().stream().collect(Collectors.groupingBy(
                instance -> instance.getInfusion().domain,
                Collectors.summingDouble(instance -> instance.getInfusion().getAffinityModifier())));
    }

    public Duration getTotalCooldown() {
        return infusions.values().stream()
                .map(InfusionInstance::getCooldown)
                .reduce(Duration.ZERO, Duration::plus);
    }
}
