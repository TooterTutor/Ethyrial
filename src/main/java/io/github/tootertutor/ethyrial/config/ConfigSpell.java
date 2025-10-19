package io.github.tootertutor.ethyrial.config;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.config.Effect.EffectInstance;
import io.github.tootertutor.ethyrial.spells.Spell;
import io.github.tootertutor.ethyrial.spells.SpellDomain;
import io.github.tootertutor.ethyrial.spells.TriggerType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class ConfigSpell extends Spell {
    private final Set<TriggerType> triggers;
    private final List<EffectInstance> effectChain;

    public ConfigSpell(
            Ethyrial plugin,
            String keyPath,
            String name,
            String description,
            int manaCost,
            int cooldownTicks,
            SpellDomain domain,
            Set<TriggerType> triggers,
            List<EffectInstance> effectChain) {
        super(plugin, keyPath, name, description, cooldownTicks, cooldownTicks, domain);

        this.triggers = EnumSet.copyOf(triggers);
        this.effectChain = List.copyOf(effectChain);
    }

    @Override
    public Set<TriggerType> triggers() {
        return triggers;
    }

    @Override
    public void onCast(Player caster) {
        // Delegate to the same internal runner used by non-CAST triggers.
        runAll(caster, null);
    }

    @Override
    public void onTriggered(Ethyrial plugin, TriggerType trigger, Player contextPlayer, Event rawEvent) {
        runAll(contextPlayer, rawEvent);
    }

    private void runAll(Player player, Event rawEvent) {
        for (var effect : effectChain) {
            try {
                effect.run(plugin, player, rawEvent);
            } catch (Throwable throwed) {
                plugin.getLogger().warning("ConfigSpell '" + getKey() + "' effect failed: " + throwed.getMessage());
                player.sendMessage(Component.text("A spell effect failed. Check console.", NamedTextColor.RED));
            }
        }
    }
}
