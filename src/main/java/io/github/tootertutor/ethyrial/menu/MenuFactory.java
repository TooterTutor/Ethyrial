package io.github.tootertutor.ethyrial.menu;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.data.PlayerData;

/**
 * A factory for creating menu instances based on an identifier.
 * Allows dynamic and centralized menu creation with per-player PlayerData
 * support.
 */
public class MenuFactory {

    private static final Map<String, BiFunction<Player, PlayerData, Menu>> registry = new HashMap<>();

    /**
     * Registers a menu constructor under an identifier.
     *
     * @param id       The unique ID for the menu (e.g., "spell_tree")
     * @param supplier A function that creates the menu given a player and
     *                 PlayerData
     */
    public static void register(String id, BiFunction<Player, PlayerData, Menu> supplier) {
        registry.put(id.toLowerCase(), supplier);
    }

    /**
     * Creates a new menu instance from the given identifier.
     *
     * @param id     the registered menu ID
     * @param player the player for whom the menu is created
     * @param data   the player's PlayerData
     * @return a new Menu instance, or null if the ID is unregistered
     */
    public static Menu create(String id, Player player, PlayerData data) {
        BiFunction<Player, PlayerData, Menu> supplier = registry.get(id.toLowerCase());
        return (supplier != null) ? supplier.apply(player, data) : null;
    }
}
