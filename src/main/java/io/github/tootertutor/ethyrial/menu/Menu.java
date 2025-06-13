package io.github.tootertutor.ethyrial.menu;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;

/**
 * Base class for custom inventory menus.
 * Handles rendering and click event binding.
 */
public abstract class Menu {

    protected final Player player;
    protected final Inventory inventory;
    protected final Map<Integer, Consumer<InventoryClickEvent>> clickHandlers = new HashMap<>();

    public Menu(Player player, int size, Component title) {
        this.player = player;
        this.inventory = Bukkit.createInventory(player, size, title);
    }

    /**
     * Called to render menu contents and bind click events.
     */
    public abstract void render();

    public void open() {
        MenuManager.open(player, this);
    }

    public void close() {
        MenuManager.close(player);
    }

    /**
     * Registers an item and an optional click handler at the given slot.
     *
     * @param slot         inventory slot
     * @param item         item to display
     * @param clickHandler optional click event handler
     */
    public void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> clickHandler) {
        inventory.setItem(slot, item);
        if (clickHandler != null) {
            clickHandlers.put(slot, clickHandler);
        }
    }

    /**
     * Registers an item with no click handler.
     *
     * @param slot slot to place item
     * @param item item to display
     */
    public void setItem(int slot, ItemStack item) {
        setItem(slot, item, null);
    }

    /**
     * Dispatches a click event to the registered handler for the clicked slot.
     * Always cancels the event to prevent item movement.
     *
     * @param event the inventory click event
     */
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        // Only cancel if the clicked inventory is this menu's inventory
        if (event.getClickedInventory() != null && event.getClickedInventory().equals(inventory)) {
            event.setCancelled(true); // Cancel to prevent exploits in menu inventory

            Consumer<InventoryClickEvent> handler = clickHandlers.get(slot);
            if (handler != null) {
                handler.accept(event);
            }
        }
    }

    /**
     * @return the inventory associated with this menu
     */
    public Inventory getInventory() {
        return inventory;
    }

}
