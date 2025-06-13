package io.github.tootertutor.ethyrial.menu;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Utility methods for creating inventory items.
 */
public class MenuUtils {

    /**
     * Creates a simple item with a display name.
     *
     * @param material item material
     * @param name     display name
     * @return the created ItemStack
     */
    public static ItemStack createItem(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name.colorIfAbsent(NamedTextColor.WHITE));
        item.setItemMeta(meta);
        return item;
    }
}
