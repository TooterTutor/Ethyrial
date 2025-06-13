package io.github.tootertutor.ethyrial.menu;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class MenuManager implements Listener {
    private static final Map<UUID, Menu> activeMenus = new ConcurrentHashMap<>();

    // #region Menu API
    /**
     * Opens the menu to the player and renders the contents.
     * 
     * @param player the player to open the menu for
     * @param menu   the menu class to open
     */
    public static void open(Player player, Menu menu) {
        // Close any existing menu (and call InventoryCloseEvent manually if needed)
        close(player);

        activeMenus.put(player.getUniqueId(), menu);
        menu.render();
        player.openInventory(menu.getInventory());
    }

    /**
     * closes the menu for the targeted player.
     * 
     * @param player the player to close the menu for
     */
    public static void close(Player player) {
        activeMenus.remove(player.getUniqueId());
        player.closeInventory();
    }

    /**
     * Returns the menu the player is on.
     * 
     * @param player the player to check
     * @return the menu the player is on, or null if not on a menu
     */
    public static Menu get(Player player) {
        return activeMenus.get(player.getUniqueId());
    }
    // #endregion

    // #region InventoryClick Event
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        Menu menu = activeMenus.get(player.getUniqueId());
        if (menu == null) {
            return;
        }
        // Cancel clicks in both the menu inventory and the player's inventory to
        // prevent exploits

        if (event.getClickedInventory() == null)
            return;

        if (event.getClickedInventory() != null && event.getClickedInventory().equals(menu.getInventory())) {
            menu.handleClick(event);
        }

        if (event.getClickedInventory().equals(player.getInventory()) ||
                event.getClickedInventory().equals(menu.getInventory())) {
            event.setCancelled(true);
        }
    }
    // #endregion

    // #region InventoryClose Event
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player))
            return;

        activeMenus.remove(player.getUniqueId());
    }
    // #endregion
}
