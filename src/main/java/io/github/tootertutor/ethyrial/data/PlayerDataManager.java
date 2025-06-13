package io.github.tootertutor.ethyrial.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.tootertutor.ethyrial.database.PlayerDAO;
import io.github.tootertutor.ethyrial.database.SQLiteDatabaseManager;

/**
 * Manages loading, caching, saving, and unloading of PlayerData instances.
 * Provides asynchronous access to the database layer through PlayerDAO.
 */
public class PlayerDataManager {

    private static final PlayerDataManager instance = new PlayerDataManager();
    private final Map<UUID, PlayerData> cache = new HashMap<>();
    private SQLiteDatabaseManager databaseManager;

    public PlayerDataManager() {
    }

    public static PlayerDataManager getInstance() {
        return instance;
    }

    public void setDatabaseManager(SQLiteDatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Loads PlayerData from the database asynchronously, or retrieves it from the
     * cache if present.
     *
     * @param uuid UUID of the player
     * @return CompletableFuture with PlayerData
     */
    public CompletableFuture<PlayerData> get(UUID uuid) {
        if (cache.containsKey(uuid)) {
            return CompletableFuture.completedFuture(cache.get(uuid));
        }

        PlayerDAO dao = new PlayerDAO(databaseManager);
        return dao.loadPlayerData(uuid).thenApply(data -> {
            cache.put(uuid, data);
            return data;
        });
    }

    /**
     * Saves PlayerData to the database asynchronously.
     *
     * @param uuid UUID of the player to save
     * @return CompletableFuture for the save operation
     */
    public CompletableFuture<Void> save(UUID uuid) {
        if (!cache.containsKey(uuid)) {
            return CompletableFuture.completedFuture(null);
        }

        PlayerDAO dao = new PlayerDAO(databaseManager);
        return dao.savePlayerData(uuid, cache.get(uuid));
    }

    /**
     * Removes a player from the cache.
     *
     * @param uuid UUID of the player
     */
    public void unload(UUID uuid) {
        cache.remove(uuid);
    }
}
