package io.github.tootertutor.ethyrial.builders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import io.github.tootertutor.ethyrial.handlers.ItemTextHandler;

public class ItemBuilder {
    private String displayName;
    private Material material;
    private ItemStack itemStack;
    private List<String> lore = new ArrayList<>();
    private String nameColor = "#FFF";
    private final Map<String, Recipe> recipes;
    private ItemMeta itemMeta;
    private final ItemDataBuilder dataBuilder;

    public ItemBuilder(Plugin plugin) {
        this.dataBuilder = new ItemDataBuilder(plugin);
        this.recipes = new HashMap<>();
    }

    public ItemBuilder setKey(NamespacedKey key) {
        return this;
    }

    public ItemBuilder setDisplayName(String name) {
        this.displayName = name;
        return this;
    }

    public ItemBuilder setMaterial(Material item) {
        this.material = item;
        this.itemStack = new ItemStack(item);
        if (itemStack.hasItemMeta()) {
            this.itemMeta = itemStack.getItemMeta();
        }
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        this.lore = new ArrayList<>(lore);
        return this;
    }

    public ItemBuilder setNameColor(String nameColor) {
        this.nameColor = nameColor;
        return this;
    }

    public ItemBuilder setLoreColor(List<String> loreColor) {
        new ArrayList<>(loreColor);
        return this;
    }

    public ItemBuilder addRecipe(String key, Recipe recipe) {
        this.recipes.put(key, recipe);
        return this;
    }

    public ItemStack buildItemStack() {
        itemStack = new ItemStack(material);
        itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            ItemTextHandler textHandler = new ItemTextHandler(itemStack);
            textHandler.setDisplayName(displayName, nameColor);

            if (lore != null) {
                textHandler.setLoreFromStrings(lore); // This will call the String version
            }

            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }
}
