package io.github.tootertutor.ethyrial.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.NamespacedKey;

import io.github.tootertutor.ethyrial.data.PlayerData;
import io.github.tootertutor.ethyrial.data.PlayerStats;
import io.github.tootertutor.ethyrial.data.SpellSlots;
import io.github.tootertutor.ethyrial.data.WandLoadout;
import io.github.tootertutor.ethyrial.spells.SpellUtils;
import it.unimi.dsi.fastutil.Pair;

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

    public CompletableFuture<SpellSlots> saveWandBindings(UUID uuid, String wandId, boolean isLeftClick,
            NamespacedKey spellKey) {
        return db.runAsyncQuery(() -> {
            try (Connection conn = db.getConnection()) {
                String spell = spellKey != null ? spellKey.toString() : null;

                // Insert the row if it doesn’t exist
                try (PreparedStatement insert = conn.prepareStatement(
                        "INSERT OR IGNORE INTO wand_bindings (uuid, wand_id, left_spell, right_spell) VALUES (?, ?, NULL, NULL)")) {
                    insert.setString(1, uuid.toString());
                    insert.setString(2, wandId);
                    insert.executeUpdate();
                }

                String query = isLeftClick
                        ? "UPDATE wand_bindings SET left_spell = ? WHERE uuid = ? AND wand_id = ?"
                        : "UPDATE wand_bindings SET right_spell = ? WHERE uuid = ? AND wand_id = ?";

                try (PreparedStatement insert = conn.prepareStatement(
                        "INSERT OR IGNORE INTO wand_bindings (uuid, wand_id, left_spell, right_spell) VALUES (?, ?, NULL, NULL)")) {
                    insert.setString(1, uuid.toString());
                    insert.setString(2, wandId);

                    insert.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, spell);
                    stmt.setString(2, uuid.toString());
                    stmt.setString(3, wandId);

                    stmt.executeUpdate();
                }

                return new SpellSlots(uuid, spellKey);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save wand bindings for " + uuid, e);
            }
        });
    }

    public CompletableFuture<Pair<NamespacedKey, NamespacedKey>> loadWandBindings(UUID uuid, String wandId) {
        return db.runAsyncQuery(() -> {
            try (Connection conn = db.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                            "SELECT left_spell, right_spell FROM wand_bindings WHERE uuid = ? AND wand_id = ?")) {

                stmt.setString(1, uuid.toString());
                stmt.setString(2, wandId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String leftSpell = rs.getString("left_spell");
                    String rightSpell = rs.getString("right_spell");

                    NamespacedKey leftKey = null;
                    NamespacedKey rightKey = null;

                    if (leftSpell != null && !leftSpell.isEmpty()) {
                        leftKey = SpellUtils.toKey(leftSpell);
                    }
                    if (rightSpell != null && !rightSpell.isEmpty()) {
                        rightKey = SpellUtils.toKey(rightSpell);
                    }

                    return Pair.of(leftKey, rightKey);

                } else {
                    return Pair.of(null, null);
                }

            } catch (SQLException e) {
                throw new RuntimeException("Failed to load wand bindings for " + uuid, e);
            }
        });
    }

    public CompletableFuture<Void> saveWandLoadout(UUID uuid, String wandId, String loadoutName,
            NamespacedKey leftSpell, NamespacedKey rightSpell) {
        return db.runAsyncQuery(() -> {
            try (Connection conn = db.getConnection()) {
                String left = leftSpell != null ? leftSpell.toString() : null;
                String right = rightSpell != null ? rightSpell.toString() : null;

                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT OR REPLACE INTO wand_loadouts (uuid, wand_id, loadout_name, left_spell, right_spell) " +
                                "VALUES (?, ?, ?, ?, ?)")) {
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, wandId);
                    stmt.setString(3, loadoutName);
                    stmt.setString(4, left);
                    stmt.setString(5, right);
                    stmt.executeUpdate();
                }
                return null;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save wand loadout for " + uuid, e);
            }
        });
    }

    public CompletableFuture<Pair<NamespacedKey, NamespacedKey>> loadWandLoadout(UUID uuid, String wandId,
            String loadoutName) {
        return db.runAsyncQuery(() -> {
            try (Connection conn = db.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                            "SELECT left_spell, right_spell FROM wand_loadouts WHERE uuid = ? AND wand_id = ? AND loadout_name = ?")) {

                stmt.setString(1, uuid.toString());
                stmt.setString(2, wandId);
                stmt.setString(3, loadoutName);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String leftSpell = rs.getString("left_spell");
                    String rightSpell = rs.getString("right_spell");

                    NamespacedKey leftKey = null;
                    NamespacedKey rightKey = null;

                    if (leftSpell != null && !leftSpell.isEmpty()) {
                        leftKey = SpellUtils.toKey(leftSpell);
                    }
                    if (rightSpell != null && !rightSpell.isEmpty()) {
                        rightKey = SpellUtils.toKey(rightSpell);
                    }

                    return Pair.of(leftKey, rightKey);
                }
                return Pair.of(null, null);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load wand loadout for " + uuid, e);
            }
        });
    }

    public CompletableFuture<Void> deleteWandLoadout(UUID uuid, String wandId, String loadoutName) {
        return db.runAsyncQuery(() -> {
            try (Connection conn = db.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                            "DELETE FROM wand_loadouts WHERE uuid = ? AND wand_id = ? AND loadout_name = ?")) {

                stmt.setString(1, uuid.toString());
                stmt.setString(2, wandId);
                stmt.setString(3, loadoutName);
                stmt.executeUpdate();
                return null;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete wand loadout for " + uuid, e);
            }
        });
    }

    public CompletableFuture<List<WandLoadout>> getWandLoadouts(UUID playerId, String wandId) {
        return db.runAsyncQuery(() -> {
            try (Connection conn = db.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                            "SELECT loadout_name, left_spell, right_spell FROM wand_loadouts WHERE uuid = ? AND wand_id = ?")) {

                stmt.setString(1, playerId.toString());
                stmt.setString(2, wandId);
                ResultSet rs = stmt.executeQuery();

                List<WandLoadout> loadouts = new ArrayList<>();

                while (rs.next()) {
                    String loadoutName = rs.getString("loadout_name");
                    String leftSpellStr = rs.getString("left_spell");
                    String rightSpellStr = rs.getString("right_spell");

                    NamespacedKey leftSpell = null;
                    NamespacedKey rightSpell = null;

                    if (leftSpellStr != null && !leftSpellStr.isEmpty()) {
                        leftSpell = SpellUtils.toKey(leftSpellStr);
                    }
                    if (rightSpellStr != null && !rightSpellStr.isEmpty()) {
                        rightSpell = SpellUtils.toKey(rightSpellStr);
                    }

                    loadouts.add(new WandLoadout(playerId, wandId, loadoutName, leftSpell, rightSpell));
                }

                return loadouts;
            } catch (SQLException e) {
                throw new RuntimeException(
                        "Failed to load wand loadouts for player " + playerId + " and wand " + wandId, e);
            }
        });
    }
}
