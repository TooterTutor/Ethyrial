package io.github.tootertutor.ethyrial.handlers;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;

/**
 * Refactored ItemTextHandler delegating displayName and lore handling to dedicated handlers.
 */
public class ItemTextHandler {

    private final DisplayNameHandler displayNameHandler;
    private final LoreHandler loreHandler;

    public ItemTextHandler(ItemStack item) {
        this.displayNameHandler = new DisplayNameHandler(item);
        this.loreHandler = new LoreHandler(item);
    }

    // DisplayName methods
    public Component getDisplayName() {
        return displayNameHandler.getDisplayName();
    }

    public void setDisplayName(Component displayName) {
        displayNameHandler.setDisplayName(displayName);
    }

    public void setDisplayName(String displayName) {
        displayNameHandler.setDisplayName(displayName);
    }

    public void setDisplayName(String displayName, String hexColor) {
        displayNameHandler.setDisplayName(displayName, hexColor);
    }

    public void updateDisplayName(Component newDisplayName) {
        displayNameHandler.updateDisplayName(newDisplayName);
    }

    public void applyDisplayNameColorGradient(String startColor, String endColor) {
        displayNameHandler.applyColorGradient(startColor, endColor);
    }

    public void clearDisplayName() {
        displayNameHandler.clearDisplayName();
    }

    public boolean hasDisplayName() {
        return displayNameHandler.hasDisplayName();
    }

    // Lore methods
    public List<Component> getLore() {
        return loreHandler.getLore();
    }

    public void setLoreFromStrings(List<String> lore) {
        loreHandler.setLoreFromStrings(lore);
    }

    public void setLore(List<Component> lore) {
        loreHandler.setLore(lore);
    }

    public void updateLoreLine(int lineIndex, Component newText) {
        loreHandler.updateLoreLine(lineIndex, newText);
    }

    public void addLoreLine(Component line) {
        loreHandler.addLoreLine(line);
    }

    public void removeLoreLine(int lineIndex) {
        loreHandler.removeLoreLine(lineIndex);
    }

    public void clearLore() {
        loreHandler.clearLore();
    }

    public void applyLoreColorGradient(String startColor, String endColor) {
        loreHandler.applyColorGradient(startColor, endColor);
    }

    public boolean hasLore() {
        return loreHandler.hasLore();
    }
}
