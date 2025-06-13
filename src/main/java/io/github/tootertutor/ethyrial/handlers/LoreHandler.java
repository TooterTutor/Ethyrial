package io.github.tootertutor.ethyrial.handlers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

/**
 * Handler class for managing the lore of an ItemStack.
 * Supports setting, updating, placeholder replacement, color gradients, and clearing.
 */
public class LoreHandler {

    private final ItemStack item;

    public LoreHandler(ItemStack item) {
        this.item = item;
    }

    /**
     * Get the current lore components.
     * @return the list of lore components, or null if none set
     */
    public List<Component> getLore() {
        ItemMeta meta = item.getItemMeta();
        return (meta != null) ? meta.lore() : null;
    }

    /**
     * Set the lore from a list of components.
     * @param lore the list of components to set
     */
    public void setLore(List<Component> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.lore(lore);
            item.setItemMeta(meta);
        }
    }

    /**
     * Set the lore from a list of strings.
     * @param lore the list of strings to set
     */
    public void setLoreFromStrings(List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<Component> loreComponents = new ArrayList<>();
            for (String line : lore) {
                loreComponents.add(Component.text(line));
            }
            meta.lore(loreComponents);
            item.setItemMeta(meta);
        }
    }

    /**
     * Update a specific lore line.
     * @param lineIndex the index of the line to update
     * @param newText the new component text
     */
    public void updateLoreLine(int lineIndex, Component newText) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<Component> lore = meta.lore();
            if (lore != null && lineIndex >= 0 && lineIndex < lore.size()) {
                lore.set(lineIndex, newText);
                meta.lore(lore);
                item.setItemMeta(meta);
            }
        }
    }

    /**
     * Add a lore line.
     * @param line the component to add
     */
    public void addLoreLine(Component line) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<Component> lore = meta.lore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add(line);
            meta.lore(lore);
            item.setItemMeta(meta);
        }
    }

    /**
     * Remove a lore line.
     * @param lineIndex the index of the line to remove
     */
    public void removeLoreLine(int lineIndex) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<Component> lore = meta.lore();
            if (lore != null && lineIndex >= 0 && lineIndex < lore.size()) {
                lore.remove(lineIndex);
                meta.lore(lore);
                item.setItemMeta(meta);
            }
        }
    }

    /**
     * Clear all lore.
     */
    public void clearLore() {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.lore(null);
            item.setItemMeta(meta);
        }
    }

    /**
     * Apply a color gradient to all lore lines.
     * @param startColor the start color hex string (e.g. "#FF0000")
     * @param endColor the end color hex string (e.g. "#0000FF")
     */
    public void applyColorGradient(String startColor, String endColor) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<Component> lore = meta.lore();
        if (lore == null || lore.isEmpty()) return;

        TextColor start = TextColor.fromHexString(startColor);
        TextColor end = TextColor.fromHexString(endColor);

        List<Component> newLore = new ArrayList<>();
        for (Component line : lore) {
            String text = line.toString();
            int length = text.length();
            if (length == 0) {
                newLore.add(line);
                continue;
            }
            TextComponent.Builder builder = Component.text();
            for (int i = 0; i < length; i++) {
                double ratio = (double) i / (length - 1);
                int red = (int) (start.red() + ratio * (end.red() - start.red()));
                int green = (int) (start.green() + ratio * (end.green() - start.green()));
                int blue = (int) (start.blue() + ratio * (end.blue() - start.blue()));
                TextColor color = TextColor.color(red, green, blue);
                builder.append(Component.text(text.charAt(i)).color(color));
            }
            newLore.add(builder.build());
        }
        meta.lore(newLore);
        item.setItemMeta(meta);
    }

    /**
     * Check if the item has lore.
     * @return true if lore is set, false otherwise
     */
    public boolean hasLore() {
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasLore();
    }
}
