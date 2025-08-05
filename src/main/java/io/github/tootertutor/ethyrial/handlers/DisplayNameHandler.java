package io.github.tootertutor.ethyrial.handlers;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

/**
 * Handler class for managing the display name of an ItemStack.
 * Supports setting, updating, placeholder replacement, color gradients, and
 * clearing.
 */
public class DisplayNameHandler {

    private final ItemStack item;

    public DisplayNameHandler(ItemStack item) {
        this.item = item;
    }

    /**
     * Get the current display name component.
     * 
     * @return the display name component, or null if none set
     */
    public Component getDisplayName() {
        ItemMeta meta = item.getItemMeta();
        return (meta != null) ? meta.displayName() : null;
    }

    /**
     * Set the display name component.
     * 
     * @param displayName the component to set
     */
    public void setDisplayName(Component displayName) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(displayName);
            item.setItemMeta(meta);
        }
    }

    /**
     * Set the display name from a plain string.
     * 
     * @param displayName the string to set
     */
    public void setDisplayName(String displayName) {
        setDisplayName(Component.text(displayName));
    }

    /**
     * Set the display name from a plain string with a hex color.
     * 
     * @param displayName the string to set
     * @param hexColor    the hex color string (e.g. "#FF0000")
     */
    public void setDisplayName(String displayName, String hexColor) {
        setDisplayName(
                Component.text(displayName != null ? displayName : " ").color(TextColor.fromHexString(hexColor)));
    }

    /**
     * Update the display name component.
     * 
     * @param newDisplayName the new component to set
     */
    public void updateDisplayName(Component newDisplayName) {
        setDisplayName(newDisplayName);
    }

    /**
     * Apply a color gradient to the display name.
     * 
     * @param startColor the start color hex string (e.g. "#FF0000")
     * @param endColor   the end color hex string (e.g. "#0000FF")
     */
    public void applyColorGradient(String startColor, String endColor) {
        Component displayName = getDisplayName();
        if (displayName == null)
            return;

        String text = displayName.toString();
        int length = text.length();
        if (length == 0)
            return;

        TextComponent.Builder builder = Component.text();
        TextColor start = TextColor.fromHexString(startColor);
        TextColor end = TextColor.fromHexString(endColor);

        for (int i = 0; i < length; i++) {
            double ratio = (double) i / (length - 1);
            int red = (int) (start.red() + ratio * (end.red() - start.red()));
            int green = (int) (start.green() + ratio * (end.green() - start.green()));
            int blue = (int) (start.blue() + ratio * (end.blue() - start.blue()));
            TextColor color = TextColor.color(red, green, blue);
            builder.append(Component.text(text.charAt(i)).color(color));
        }

        setDisplayName(builder.build());
    }

    /**
     * Clear the display name.
     */
    public void clearDisplayName() {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(null);
            item.setItemMeta(meta);
        }
    }

    /**
     * Check if the item has a display name.
     * 
     * @return true if display name is set, false otherwise
     */
    public boolean hasDisplayName() {
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName();
    }
}
