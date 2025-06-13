package io.github.tootertutor.ethyrial.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.tootertutor.ethyrial.data.PlayerData;
import io.github.tootertutor.ethyrial.data.PlayerStats;

public class PlayerDAO {

    private final SQLiteDatabaseManager db;

    public PlayerDAO(SQLiteDatabaseManager db) {
        this.db = db;
    }

    public CompletableFuture<PlayerData> loadPlayerData(UUID uuid) {
        return db.runAsyncQuery(() -> {
            try (Connection conn = db.getConnection()) {
                PlayerStats stats = null;
                Set<String> unlockedSpells = new HashSet<>();

                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT mana, spellPower, bonusHealth FROM player_data WHERE uuid = ?")) {
                    stmt.setString(1, uuid.toString());
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        stats = new PlayerStats(rs.getInt("mana"), rs.getInt("spellPower"), rs.getInt("bonusHealth"));
                    } else {
                        stats = new PlayerStats(100, 10, 20); // default stats
                        savePlayerData(uuid, new PlayerData(uuid, stats, new HashSet<>())).join();
                    }
                }

                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT spell_id FROM unlocked_spells WHERE uuid = ?")) {
                    stmt.setString(1, uuid.toString());
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        unlockedSpells.add(rs.getString("spell_id"));
                    }
                }

                return new PlayerData(uuid, stats, unlockedSpells);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load player data for " + uuid, e);
            }
        });
    }

    public CompletableFuture<Void> savePlayerData(UUID uuid, PlayerData data) {
        return db.runAsyncQuery(() -> {
            try (Connection conn = db.getConnection()) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "REPLACE INTO player_data (uuid, mana, spellPower, bonusHealth) VALUES (?, ?, ?, ?)")) {
                    stmt.setString(1, uuid.toString());
                    stmt.setInt(2, data.getStats().getMana());
                    stmt.setInt(3, data.getStats().getSpellPower());
                    stmt.setInt(4, data.getStats().getBonusHealth());
                    stmt.executeUpdate();
                }

                try (PreparedStatement deleteStmt = conn.prepareStatement(
                        "DELETE FROM unlocked_spells WHERE uuid = ?")) {
                    deleteStmt.setString(1, uuid.toString());
                    deleteStmt.executeUpdate();
                }

                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO unlocked_spells (uuid, spell_id) VALUES (?, ?)")) {
                    for (String spellId : data.getUnlockedSpells()) {
                        insertStmt.setString(1, uuid.toString());
                        insertStmt.setString(2, spellId);
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }

                return null;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save player data for " + uuid, e);
            }
        });
    }
}
