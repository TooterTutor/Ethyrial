package io.github.tootertutor.ethyrial;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.tootertutor.ethyrial.commands.EthyrialCommand;
import io.github.tootertutor.ethyrial.data.PlayerData;
import io.github.tootertutor.ethyrial.data.PlayerDataManager;
import io.github.tootertutor.ethyrial.database.PlayerDAO;
import io.github.tootertutor.ethyrial.database.SQLiteDatabaseManager;
import io.github.tootertutor.ethyrial.menu.MenuManager;
import io.github.tootertutor.ethyrial.registry.ItemRegister;
import io.github.tootertutor.ethyrial.registry.SpellRegister;

public final class Ethyrial extends JavaPlugin {

    private static Ethyrial instance;
    private SpellRegister spellRegister;
    private ItemRegister itemRegister;
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private PlayerDataManager playerDataManager;
    private PlayerDAO playerDAO;

    public static Ethyrial getInstance() {
        return instance;
    }

    public void onEnable() {
        instance = this;

        Bukkit.getPluginManager().registerEvents(new MenuManager(), this);

        // Load Database
        try {
            SQLiteDatabaseManager databaseManager = new SQLiteDatabaseManager(getDataFolder());
            this.playerDataManager = PlayerDataManager.getInstance();
            this.playerDataManager.setDatabaseManager(databaseManager);
            this.playerDAO = new PlayerDAO(databaseManager);
        } catch (Exception e) {
            getLogger().severe("Failed to initialize the database: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Initializing registries...");

        // Item Registration
        itemRegister = new ItemRegister(this);
        itemRegister.autoRegisterItems();

        getLogger().info("Registered items: " + itemRegister.getItems().size());

        // Spell Registration
        spellRegister = new SpellRegister(this);
        spellRegister.autoRegisterSpells();
        getLogger().info("Registered spells: " + spellRegister.getSpells().size());

        // Load Commands
        getLogger().info("Attempting to register commands...");
        EthyrialCommand ethyrialCommand = new EthyrialCommand(this);
        PluginCommand command = getCommand("ethyrial");

        getLogger().info("Command lookup resoult: " + ((command == null) ? "null" : "found"));
        if (command == null) {
            getLogger().severe("Command 'ethyrial' is not registered in the plugin.yml!");
            return;
        }
        command.setExecutor(ethyrialCommand);
        command.setTabCompleter(ethyrialCommand);

        // Get Online Players
        Bukkit.getOnlinePlayers().forEach(player -> {
            playerDAO.loadPlayerData(player.getUniqueId()).thenAccept(data -> {
                playerDataMap.put(player.getUniqueId(), data);
            }).exceptionally(ex -> {
                getLogger().severe("Failed to load data for " + player.getName() + ": " + ex.getMessage());
                ex.printStackTrace();
                return null;
            });
        });

        getLogger().info("Ethyrial has been enabled.");
    }

    public void onDisable() {
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            try {
                playerDAO.savePlayerData(entry.getKey(), entry.getValue()).join();
            } catch (Exception e) {
                getLogger().severe("Failed to save data for " + entry.getKey() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        getLogger().info("Ethyrial has been disabled.");

    }

    public ItemRegister getItemsRegistered() {
        return itemRegister;
    }

    public SpellRegister getSpellsRegistered() {
        return spellRegister;
    }

    public PlayerData getPlayerData(Player player) {
        return playerDataMap.get(player.getUniqueId());
    }

    public PlayerDAO getPlayerDAO() {
        return playerDAO;
    }

}
