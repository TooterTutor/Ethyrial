package io.github.tootertutor.ethyrial.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.InvalidConfigurationException;
import org.yaml.snakeyaml.Yaml;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.registry.EffectRegistry;
import io.github.tootertutor.ethyrial.spells.Spell;
import io.github.tootertutor.ethyrial.spells.SpellDomain;
import io.github.tootertutor.ethyrial.spells.TriggerType;

public final class ConfigSpellLoader {
    private final Ethyrial plugin;
    private final EffectRegistry effectRegistry;

    public ConfigSpellLoader(Ethyrial plugin, EffectRegistry effectRegistry) {
        this.plugin = plugin;
        this.effectRegistry = effectRegistry;
    }

    public List<Spell> loadAll(File dataFolder) throws Exception {
        File file = new File(dataFolder, "spells.yml");
        if (!file.exists())
            return List.of();

        Map<String, Object> root;
        try (FileInputStream in = new FileInputStream(file)) {
            root = new Yaml().load(in);
        }

        if (root == null)
            return List.of();

        List<Spell> out = new ArrayList<>();
        for (Map.Entry<String, Object> entry : root.entrySet()) {
            String keyPath = entry.getKey(); // e.g. "fire_blast"
            Object value = entry.getValue();

            if (!(value instanceof Map)) {
                plugin.getLogger().warning("Spell '" + keyPath + "' must be a map.");
                continue;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> cfg = (Map<String, Object>) value;

            try {
                out.add(parseSpell(keyPath, cfg));
            } catch (InvalidConfigurationException ex) {
                plugin.getLogger().warning("Failed to load spell '" + keyPath + "': " + ex.getMessage());
            } catch (Throwable t) {
                plugin.getLogger().warning("Failed to load spell '" + keyPath + "': " + t);
                t.printStackTrace();
            }
        }
        return out;
    }

    private Spell parseSpell(String keyPath, Map<String, Object> cfg) throws InvalidConfigurationException {
        String name = reqString(cfg, "name");
        String description = reqString(cfg, "description");

        String domainStr = reqString(cfg, "domain");
        SpellDomain domain = SpellDomain.valueOf(domainStr.toUpperCase(Locale.ROOT));

        int manaCost = ((Number) cfg.getOrDefault("mana_cost", 0)).intValue();
        int cooldownTicks = parseDurationTicks(String.valueOf(cfg.getOrDefault("cooldown", "0")));

        Set<TriggerType> triggers = new HashSet<>();
        Object trg = cfg.getOrDefault("triggers", List.of("CAST"));
        if (trg instanceof List<?> list) {
            for (Object o : list) {
                triggers.add(TriggerType.valueOf(String.valueOf(o).toUpperCase(Locale.ROOT)));
            }
        } else {
            triggers.add(TriggerType.valueOf(String.valueOf(trg).toUpperCase(Locale.ROOT)));
        }

        // effects
        Object eff = cfg.get("effects");
        if (!(eff instanceof List<?>)) {
            throw new InvalidConfigurationException("Spell '" + keyPath + "' must define a non-empty 'effects' list.");
        }

        List<Effect.EffectInstance> chain = new ArrayList<>();
        for (Object o : (List<?>) eff) {
            if (!(o instanceof Map<?, ?>)) {
                throw new InvalidConfigurationException("'effects' entries must be maps.");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> eCfg = (Map<String, Object>) o;

            String type = reqString(eCfg, "type");
            var effect = effectRegistry.find(type)
                    .orElseThrow(() -> new InvalidConfigurationException("Unknown effect type: " + type));

            chain.add(effect.build(plugin, eCfg));
        }

        // ✅ Construct with the new signature (string keyPath + name + description)
        return new ConfigSpell(
                plugin,
                keyPath,
                name,
                description,
                manaCost,
                cooldownTicks,
                domain,
                triggers,
                chain);
    }

    private static String reqString(Map<String, Object> m, String key) throws InvalidConfigurationException {
        Object v = m.get(key);
        if (v == null)
            throw new InvalidConfigurationException("Missing required field: " + key);
        String s = String.valueOf(v).trim();
        if (s.isEmpty())
            throw new InvalidConfigurationException("Field '" + key + "' cannot be empty.");
        return s;
    }

    private static int parseDurationTicks(String s) throws InvalidConfigurationException {
        s = s.trim().toLowerCase(Locale.ROOT);
        try {
            if (s.endsWith("ms")) {
                int ms = Integer.parseInt(s.substring(0, s.length() - 2).trim());
                return ms / 50;
            } else if (s.endsWith("s")) {
                int sec = Integer.parseInt(s.substring(0, s.length() - 1).trim());
                return sec * 20;
            } else if (s.endsWith("t") || s.endsWith("ticks")) {
                String num = s.replace("ticks", "").replace("t", "").trim();
                return Integer.parseInt(num);
            } else {
                // bare number assumed ticks
                return Integer.parseInt(s);
            }
        } catch (NumberFormatException ex) {
            throw new InvalidConfigurationException("Invalid cooldown duration: " + s);
        }
    }
}
