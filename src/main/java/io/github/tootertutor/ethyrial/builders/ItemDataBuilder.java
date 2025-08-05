package io.github.tootertutor.ethyrial.builders;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import io.github.tootertutor.ethyrial.Ethyrial;

/**
 * Builder class for managing custom item data using Bukkit's PersistentDataContainer.
 * Allows setting, getting, and checking for custom data keys and values.
 */
public class ItemDataBuilder {
    private final Ethyrial plugin;
    private final Map<NamespacedKey, Object> data = new HashMap<>();

    /**
     * Constructs an ItemDataBuilder with the given plugin instance.
     * 
     * @param plugin The plugin instance used to create NamespacedKeys.
     */
    public ItemDataBuilder(Ethyrial plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads data from an ItemMeta's PersistentDataContainer into this builder.
     * 
     * @param meta The ItemMeta to load data from.
     * @return This builder instance for chaining.
     */
    public ItemDataBuilder fromMeta(ItemMeta meta) {
        if (meta != null) {
            meta.getPersistentDataContainer().getKeys().forEach(key -> {
                data.put(key, meta.getPersistentDataContainer().get(key, getType(key)));
            });
        }
        return this;
    }

    /**
     * Sets a custom data value for the given key.
     * 
     * @param key The key to set.
     * @param value The value to associate with the key. If null, the key is removed.
     * @return This builder instance for chaining.
     */
    public ItemDataBuilder set(String key, Object value) {
        NamespacedKey nskey = new NamespacedKey(plugin, key);
        if (value == null) {
            data.remove(nskey);
        } else {
            data.put(nskey, value);
        }
        return this;
    }

    /**
     * Applies the stored data to the given ItemMeta's PersistentDataContainer.
     * 
     * @param meta The ItemMeta to apply data to.
     */
    public void applyTo(ItemMeta meta) {
        if (meta == null)
            return;

        data.forEach((key, value) -> {
            if (value instanceof Boolean) {
                byte byteValue = (Boolean) value ? (byte) 1 : (byte) 0;
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, byteValue);
            } else if (value instanceof Byte) {
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (Byte) value);
            } else if (value instanceof Integer) {
                meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, (Integer) value);
            } else if (value instanceof Float) {
                meta.getPersistentDataContainer().set(key, PersistentDataType.FLOAT, (Float) value);
            } else if (value instanceof Double) {
                meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, (Double) value);
            } else if (value instanceof String) {
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, (String) value);
            }
        });
    }

    /**
     * Checks if the given key exists and is a boolean or byte value.
     * 
     * @param key The key to check.
     * @return True if the key exists and is a boolean or byte, false otherwise.
     */
    public boolean hasBoolean(String key) {
        NamespacedKey nskey = new NamespacedKey(plugin, key);
        Object value = data.get(nskey);
        return value instanceof Boolean || value instanceof Byte;
    }

    /**
     * Checks if the given key exists in the data.
     * 
     * @param key The key to check.
     * @return True if the key exists, false otherwise.
     */
    public boolean has(String key) {
        return data.containsKey(new NamespacedKey(plugin, key));
    }

    /**
     * Determines the PersistentDataType for the given key based on the stored value.
     * 
     * @param key The NamespacedKey to check.
     * @return The PersistentDataType corresponding to the value, or null if unknown.
     */
    private PersistentDataType<?, ?> getType(NamespacedKey key) {
        Object value = data.get(key);
        if (value instanceof Boolean || (value instanceof Byte && (Byte) value <= 1)) {
            return PersistentDataType.BYTE;
        } else if (value instanceof String) {
            return PersistentDataType.STRING;
        } else if (value instanceof Integer) {
            return PersistentDataType.INTEGER;
        } else if (value instanceof Float) {
            return PersistentDataType.FLOAT;
        } else if (value instanceof Double) {
            return PersistentDataType.DOUBLE;
        }
        return null; // Fallback type
    }

    /**
     * Gets the value associated with the given key from the data map.
     * 
     * @param key The key to look up.
     * @return The value associated with the key, or null if not present.
     */
    public Object get(String key) {
        NamespacedKey nskey = new NamespacedKey(plugin, key);
        return data.get(nskey);
    }

}
