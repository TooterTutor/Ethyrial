package io.github.tootertutor.ethyrial.handlers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Handler class for managing player skins and applying player heads to items.
 * Provides functionality to fetch player skins from Mojang's API and apply
 * them to player head items using modern SkullMeta approach.
 */
public class PlayerSkinHandler {

    private static final String MOJANG_API_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";

    /**
     * Fetches a player's skin data and returns the base64-encoded texture value
     * for the player's head.
     * 
     * @param playerUUID The UUID of the player whose skin to fetch
     * @return Base64-encoded string containing the skull texture data, or null if failed
     */
    public static String getPlayerHeadTexture(UUID playerUUID) {
        try {
            return fetchSkinData(playerUUID);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Applies a player's head to an ItemStack using their UUID.
     * Uses the modern SkullMeta.setOwningPlayer() approach.
     * 
     * @param item The ItemStack to apply the head to (must be PLAYER_HEAD)
     * @param playerUUID The UUID of the player whose head to use
     * @return true if successful, false otherwise
     */
    public static boolean applyPlayerHeadToItem(ItemStack item, UUID playerUUID) {
        if (item == null || playerUUID == null) {
            return false;
        }

        if (item.getType() != Material.PLAYER_HEAD) {
            return false;
        }

        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
        if (skullMeta == null) {
            return false;
        }

        // Use the modern approach with OfflinePlayer
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
        skullMeta.setOwningPlayer(offlinePlayer);
        item.setItemMeta(skullMeta);
        
        return true;
    }

    /**
     * Creates a new player head ItemStack with a specific player's skin.
     * 
     * @param playerUUID The UUID of the player whose head to create
     * @return ItemStack containing the player head
     */
    public static ItemStack createPlayerHead(UUID playerUUID) {
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        applyPlayerHeadToItem(playerHead, playerUUID);
        return playerHead;
    }

    /**
     * Creates a new player head ItemStack from a username.
     * 
     * @param username The player's username
     * @return ItemStack containing the player head, or null if player not found
     */
    public static ItemStack createPlayerHeadFromUsername(String username) {
        UUID playerUUID = getUUIDFromUsername(username);
        if (playerUUID != null) {
            return createPlayerHead(playerUUID);
        }
        return null;
    }

    /**
     * Fetches skin data from Mojang's API for a given player UUID.
     * 
     * @param playerUUID The UUID of the player
     * @return Base64-encoded texture value from Mojang
     * @throws IOException If there's an error connecting to the API
     */
    private static String fetchSkinData(UUID playerUUID) throws IOException {
        String urlString = String.format(MOJANG_API_URL, playerUUID.toString().replace("-", ""));
        
        try (InputStreamReader reader = new InputStreamReader(
                java.net.URI.create(urlString).toURL().openStream(), StandardCharsets.UTF_8)) {
            JsonObject profileData = JsonParser.parseReader(reader).getAsJsonObject();
            
            if (profileData.has("properties")) {
                for (var element : profileData.getAsJsonArray("properties")) {
                    JsonObject property = element.getAsJsonObject();
                    if ("textures".equals(property.get("name").getAsString())) {
                        return property.get("value").getAsString();
                    }
                }
            }
        }
        
        return null;
    }

    /**
     * Utility method to get a player's UUID from their username.
     * 
     * @param username The player's username
     * @return The player's UUID, or null if not found
     */
    public static UUID getUUIDFromUsername(String username) {
        try {
            String urlString = "https://api.mojang.com/users/profiles/minecraft/" + username;
            
            try (InputStreamReader reader = new InputStreamReader(
                    java.net.URI.create(urlString).toURL().openStream(), StandardCharsets.UTF_8)) {
                JsonObject profileData = JsonParser.parseReader(reader).getAsJsonObject();
                String uuidString = profileData.get("id").getAsString();
                
                // Add dashes to UUID
                return UUID.fromString(uuidString.replaceFirst(
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})", 
                    "$1-$2-$3-$4-$5"
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the base64 texture value for a player's head.
     * This is the raw texture data that can be used for custom skulls.
     * 
     * @param playerUUID The UUID of the player
     * @return Base64-encoded texture string, or null if failed
     */
    public static String getPlayerHeadBase64(UUID playerUUID) {
        return getPlayerHeadTexture(playerUUID);
    }
}
