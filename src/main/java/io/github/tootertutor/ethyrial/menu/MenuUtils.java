package io.github.tootertutor.ethyrial.menu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.builders.ItemBuilder;
import io.github.tootertutor.ethyrial.handlers.PlayerSkinHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Utility methods for creating inventory items.
 */
public class MenuUtils {

    private static final Set<Material> borderMaterials = new HashSet<>();
    // private static final String DEFAULT_NAME_COLOR = "#AAAAAA"; // light gray

    public static ItemStack createGlassPane(DyeColor color, Component name, Ethyrial plugin) {
        Material glassMaterial = Material.valueOf(color.name() + "_STAINED_GLASS_PANE");
        borderMaterials.add(glassMaterial);

        ItemBuilder builder = new ItemBuilder(plugin)
                .setMaterial(glassMaterial)
                .name(safeName(name));

        return builder.buildItemStack();
    }

    public static Component safeName(Component input) {
        if (input == null)
            return Component.text(" ");
        // Optional: could also check for empty content if you want
        return input;
    }

    public enum BorderStyle {
        TOP, BOTTOM, CORNERS, FULL
    }

    public static void applyBorder(Inventory inventory, DyeColor color, Ethyrial plugin,
            BorderStyle... styles) {
        ItemStack pane = createGlassPane(color, null, plugin);
        int size = inventory.getSize();

        for (BorderStyle style : styles) {
            switch (style) {
                case TOP -> {
                    for (int i = 0; i < 9; i++) {
                        inventory.setItem(i, pane);
                    }
                }
                case BOTTOM -> {
                    for (int i = size - 9; i < size; i++) {
                        inventory.setItem(i, pane);
                    }
                }
                case CORNERS -> {
                    inventory.setItem(0, pane);
                    inventory.setItem(8, pane);
                    inventory.setItem(size - 9, pane);
                    inventory.setItem(size - 1, pane);
                }
                case FULL -> {
                    for (int i = 0; i < size; i++) {
                        if (i < 9 || i >= size - 9 || i % 9 == 0 || i % 9 == 8) {
                            inventory.setItem(i, pane);
                        }
                    }
                }
            }
        }
    }

    public static void setPaginationControls(PagedMenu<?> menu, int page, int maxPage, DyeColor themeColor,
            Menu backTarget) {
        Ethyrial plugin = Ethyrial.getInstance();

        if (page > 0) {
            ItemStack prev = createGlassPane(DyeColor.GREEN, Component.text("◀ Prev", NamedTextColor.GRAY), plugin);
            menu.setItem(47, prev, e -> menu.previousPage());
        }

        if (page + 1 < maxPage) {
            ItemStack next = createGlassPane(DyeColor.GREEN, Component.text("Next ▶", NamedTextColor.GRAY), plugin);
            menu.setItem(51, next, e -> menu.nextPage());
        }

        if (backTarget != null) {
            ItemStack back = createGlassPane(DyeColor.RED, Component.text("↩ Back", NamedTextColor.RED), plugin);
            menu.setItem(49, back, e -> MenuManager.open(menu.player, backTarget));
        } else {
            ItemStack info = createGlassPane(DyeColor.CYAN, Component.text("Page " + (page + 1) + " of " + maxPage),
                    plugin);
            menu.setItem(49, info);
        }
    }

    public static boolean isBorderPane(ItemStack item) {
        return item != null && borderMaterials.contains(item.getType());
    }

    public static void handleBorderClick(InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isBorderPane(clicked)) {
            event.setCancelled(true);
        }
    }

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

    /**
     * Creates a simple item with a display name and applies player skin if it's a player head.
     *
     * @param material item material
     * @param name     display name
     * @param playerUUID UUID of the player whose head to use (if material is PLAYER_HEAD)
     * @return the created ItemStack
     */
    public static ItemStack createItem(Material material, Component name, UUID playerUUID) {
        ItemStack item = new ItemStack(material);
        
        if (material == Material.PLAYER_HEAD && playerUUID != null) {
            PlayerSkinHandler.applyPlayerHeadToItem(item, playerUUID);
        }
        
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name.colorIfAbsent(NamedTextColor.WHITE));
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates a simple item with a display name and applies player skin if it's a player head.
     *
     * @param material item material
     * @param name     display name
     * @param playerUUID UUID of the player whose head to use (if material is PLAYER_HEAD)
     * @param lore     item lore
     * @return the created ItemStack
     */
    public static ItemStack createItem(Material material, Component name, UUID playerUUID, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        
        if (material == Material.PLAYER_HEAD && playerUUID != null) {
            PlayerSkinHandler.applyPlayerHeadToItem(item, playerUUID);
        }
        
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name.colorIfAbsent(NamedTextColor.WHITE));
        if (lore != null && !lore.isEmpty()) {
            meta.lore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static void setBackButton(Menu menu, Menu targetMenu) {
        ItemStack backItem = createItem(Material.ARROW, Component.text()
                .content("Back")
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false)
                .build());

        menu.setItem(49, backItem, event -> MenuManager.open(menu.player, targetMenu));
    }

    public static List<Integer> getGridSlots(int rows, int columns, int startSlot) {
        List<Integer> slots = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int slot = startSlot + row * 9 + col;
                if (slot % 9 == 0 || slot % 9 == 8)
                    continue; // skip side borders
                slots.add(slot);
            }
        }
        return slots;
    }

}
