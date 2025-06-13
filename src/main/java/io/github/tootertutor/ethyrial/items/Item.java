package io.github.tootertutor.ethyrial.items;

import java.net.http.WebSocket.Listener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import io.github.tootertutor.ethyrial.builders.ItemDataBuilder;
import io.github.tootertutor.ethyrial.handlers.ItemTextHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public abstract class Item implements Listener, Keyed {
    protected final Plugin plugin;
    protected String displayName;
    protected Material material;
    protected List<String> lore;
    protected String nameColor;
    protected List<String> loreColor;
    protected ItemDataBuilder itemDataBuilder;
    protected Map<String, Recipe> recipes;
    protected ItemStack itemStack;
    protected ItemTextHandler textHandler;
    protected NamespacedKey key;

    // New field to store placeholders
    protected Map<String, String> placeholders = new HashMap<>();

    protected Item(Plugin plugin, NamespacedKey key) {
        this.plugin = plugin;
        this.key = key;
        this.itemDataBuilder = new ItemDataBuilder(plugin);
        this.textHandler = new ItemTextHandler(itemStack);
        setupBasePersistentData();
        updateItemText();
    }

    protected Item(Plugin plugin, ItemStack itemStack) {
        this.plugin = plugin;
        this.itemStack = itemStack;
        ItemMeta meta = itemStack.getItemMeta();
        this.key = new NamespacedKey(plugin, itemStack.getType().name().toLowerCase());
        this.material = itemStack.getType();

        if (meta != null) {
            Component name = meta.displayName();
            this.displayName = name != null ? PlainTextComponentSerializer.plainText().serialize(name) : "Unknown Item";

            List<Component> loreComponents = meta.lore();
            if (loreComponents != null) {
                this.lore = new ArrayList<>();
                for (Component comp : loreComponents) {
                    this.lore.add(PlainTextComponentSerializer.plainText().serialize(comp));
                }
            } else {
                this.lore = new ArrayList<>();
            }

            this.itemDataBuilder = new ItemDataBuilder(plugin);
        } else {
            this.displayName = "Unknown Item";
            this.lore = new ArrayList<>();
            this.itemDataBuilder = null;
        }

        this.nameColor = "#FFF";
        this.loreColor = Collections.emptyList();
        this.recipes = new HashMap<>();
        this.textHandler = new ItemTextHandler(this.itemStack);
        updateItemText();
    }

    private void setupBasePersistentData() {
        // Automatic identification tag
        itemDataBuilder.set(key.getKey(), true);
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    public NamespacedKey getId() {
        return key;
    }

    public String getName() {
        return displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public ItemStack getItemStack() {
        applyMetadata();
        return itemStack;
    }

    /**
     * Creates a new ItemStack with the specified material type and copies the
     * metadata from the current item.
     *
     * @param newType The new Material type for the ItemStack.
     * @return A new ItemStack with the specified material type and copied metadata.
     */
    public ItemStack withType(Material newType) {
        ItemStack newItem = new ItemStack(newType, itemStack.getAmount());
        copyItemMeta(itemStack, newItem);
        return newItem;
    }

    /**
     * Applies the item's metadata (display name, lore, and persistent data) to the
     * ItemStack
     */
    protected void applyMetadata() {
        if (itemStack == null) {
            return;
        }

        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }

        // Replace placeholders in displayName
        String processedDisplayName = replacePlaceholders(displayName);

        // Apply display name
        if (processedDisplayName != null && nameColor != null) {
            meta.displayName(Component.text(processedDisplayName)
                    .color(TextColor.fromHexString(nameColor)));
        }

        // Replace placeholders in lore
        List<Component> loreComponents = new ArrayList<>();
        if (lore != null && !lore.isEmpty()) {
            for (int i = 0; i < lore.size(); i++) {
                String line = replacePlaceholders(lore.get(i));
                String color = (loreColor != null && i < loreColor.size()) ? loreColor.get(i) : "#FFFFFF";
                loreComponents.add(Component.text(line)
                        .color(TextColor.fromHexString(color)));
            }
            meta.lore(loreComponents);
        }

        // Apply persistent data through builder
        itemDataBuilder.applyTo(meta);

        itemStack.setItemMeta(meta);
    }

    /**
     * Replace placeholders in the input string with their corresponding values.
     * If a placeholder has no value, it is removed from the string.
     * @param input the input string possibly containing placeholders
     * @return the string with placeholders replaced or removed
     */
    protected String replacePlaceholders(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String result = input;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue();
            if (value == null) {
                value = "";
            }
            result = result.replace(placeholder, value);
        }
        // Remove any unreplaced placeholders (e.g. {unknown})
        result = result.replaceAll("\\{[^}]+\\}", "");
        return result;
    }

    /**
     * Set the placeholders map and update the item metadata.
     * @param placeholders the map of placeholder keys to values
     */
    public void setPlaceholders(Map<String, String> placeholders) {
        this.placeholders = new HashMap<>(placeholders);
        updateItemText();
    }

    /**
     * Update or add a single placeholder and refresh metadata.
     * @param key the placeholder key
     * @param value the placeholder value
     */
    public void updatePlaceholder(String key, String value) {
        this.placeholders.put(key, value);
        updateItemText();
    }

    public List<String> getLore() {
        return Collections.unmodifiableList(lore);
    }

    public String getNameColor() {
        return nameColor;
    }

    public List<String> getLoreColor() {
        return Collections.unmodifiableList(loreColor);
    }

    public Map<String, Recipe> getRecipes() {
        return Collections.unmodifiableMap(recipes);
    }

    public ItemTextHandler getTextHandler() {
        return textHandler;
    }

    @Deprecated
    public void updateItemText() {
        applyMetadata();
    }

    public void setDisplayName(String name) {
        this.displayName = name;
        textHandler.setDisplayName(name, nameColor);
    }

    public void setDisplayName(String name, String color) {
        this.displayName = name;
        this.nameColor = color;
        textHandler.setDisplayName(name, color);
    }

    public void setLore(List<Component> lore) {
        this.lore = new ArrayList<>(lore.stream().map(Component::toString).collect(Collectors.toList()));
        updateItemText();
    }

    public void setLore(List<String> lore, List<String> colors) {
        this.lore = new ArrayList<>(lore);
        this.loreColor = new ArrayList<>(colors);
        updateItemText();
    }

    public void updateLoreLine(int index, String text) {
        if (index >= 0 && index < lore.size()) {
            lore.set(index, text);
            String color = index < loreColor.size() ? loreColor.get(index) : "#FFFFFF";
            textHandler.updateLoreLine(index,
                    Component.text(text).color(TextColor.fromHexString(color)));
        }
    }

    public void updateLoreLine(int index, String text, String color) {
        if (index >= 0 && index < lore.size()) {
            lore.set(index, text);
            if (index < loreColor.size()) {
                loreColor.set(index, color);
            } else {
                loreColor.add(color);
            }
            textHandler.updateLoreLine(index,
                    Component.text(text).color(TextColor.fromHexString(color)));
        }
    }

    public void addRecipe(String key, Recipe recipe) {
        recipes.put(key, recipe);
        if (plugin.isEnabled()) {
            plugin.getServer().addRecipe(recipe);
        }
    }

    public Recipe getRecipe(String key) {
        return recipes.get(key);
    }

    public void removeRecipe(String key) {
        Recipe recipe = recipes.remove(key);
        if (recipe != null && recipe instanceof ShapedRecipe && plugin.isEnabled()) {
            plugin.getServer().removeRecipe(((ShapedRecipe) recipe).getKey());
        }
    }

    public List<Recipe> listRecipes() {
        return new ArrayList<>(recipes.values());
    }

    public boolean isItem(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return false;
        ItemMeta meta = item.getItemMeta();
        // Directly check if the PersistentDataContainer has the item's key
        return meta.getPersistentDataContainer().has(this.key);
    }

    public void copyItemMeta(ItemStack source, ItemStack target) {
        ItemMeta sourceMeta = source.getItemMeta();
        if (sourceMeta != null) {
            // Copy display name, lore, and PDC
            target.setItemMeta(sourceMeta.clone());
        }
    }

    public ItemStack get() {
        return itemStack.clone();
    }

    @Override
    public String toString() {
        return "Item{id='" + key + "', displayName='" + displayName + "', lore=" + lore +
                ", nameColor='" + nameColor + "', loreColor=" + loreColor + '}';
    }
}
