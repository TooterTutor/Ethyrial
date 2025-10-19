package io.github.tootertutor.ethyrial.database;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.bukkit.Bukkit;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class SQLiteDatabaseManager {

    private final HikariDataSource dataSource;

    public SQLiteDatabaseManager(File dataFolder) {
        // Bukkit.getLogger().info("Initializing SQLiteDatabaseManager with data folder:
        // " + dataFolder.getAbsolutePath());
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            // Bukkit.getLogger().severe("Failed to create plugin data folder: " +
            // dataFolder.getAbsolutePath());
            throw new RuntimeException("Failed to create plugin data folder.");
        }

        File dbFile = new File(dataFolder, "ethyrial.db");
        // Bukkit.getLogger().info("SQLite database file path: " +
        // dbFile.getAbsolutePath());
        String jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(10);
        config.setPoolName("EthyrialSQLitePool");

        config.setConnectionInitSql("PRAGMA foreign_keys=ON;");
        this.dataSource = new HikariDataSource(config);

        try (Connection conn = getConnection()) {
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Failed to open SQLite connection.");
            }
            Bukkit.getLogger().info("Successfully opened SQLite connection.");
            initializeDatabaseSchema(conn);
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to initialize SQLite connection:");
            e.printStackTrace();
            throw new RuntimeException("Database initialization failed.", e);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public CompletableFuture<Connection> getConnectionAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getConnection();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get database connection asynchronously", e);
            }
        });
    }

    public <T> CompletableFuture<T> runAsyncQuery(Supplier<T> queryTask) {
        return CompletableFuture.supplyAsync(queryTask);
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private void initializeDatabaseSchema(Connection conn) throws SQLException {
        String createPlayerDataTable = "CREATE TABLE IF NOT EXISTS player_data (" +
                "  uuid TEXT PRIMARY KEY," +
                "  mana INTEGER NOT NULL DEFAULT 0," +
                "  spellPower INTEGER NOT NULL DEFAULT 0," +
                "  bonusHealth INTEGER NOT NULL DEFAULT 0," +
                // New PlayerStats fields:
                "  max_mana INTEGER NOT NULL DEFAULT 100," +
                "  mana_regen INTEGER NOT NULL DEFAULT 0," +
                "  strength INTEGER NOT NULL DEFAULT 0," +
                "  agility INTEGER NOT NULL DEFAULT 0," +
                "  defense INTEGER NOT NULL DEFAULT 0," +
                "  crit_chance INTEGER NOT NULL DEFAULT 0," +
                "  crit_damage INTEGER NOT NULL DEFAULT 0," +
                // Keep these since you already had them:
                "  selected_spell_left TEXT," +
                "  selected_spell_right TEXT" +
                ");";

        String createUnlockedSpellsTable = "CREATE TABLE IF NOT EXISTS unlocked_spells (" +
                "  uuid TEXT," +
                "  spell_id TEXT," +
                "  PRIMARY KEY (uuid, spell_id)" +
                ");";

        String createWandBindingsTable = "CREATE TABLE IF NOT EXISTS wand_bindings (" +
                "  uuid TEXT NOT NULL," +
                "  wand_id TEXT NOT NULL," +
                "  left_spell TEXT," +
                "  right_spell TEXT," +
                "  PRIMARY KEY (uuid, wand_id)" +
                ");";

        String createWandLoadoutsTable = "CREATE TABLE IF NOT EXISTS wand_loadouts (" +
                "  uuid TEXT NOT NULL," +
                "  wand_id TEXT NOT NULL," +
                "  loadout_name TEXT NOT NULL," +
                "  left_spell TEXT," +
                "  right_spell TEXT," +
                "  PRIMARY KEY (uuid, wand_id, loadout_name)" +
                ");";

        String createTeamsTable = "CREATE TABLE IF NOT EXISTS teams (" +
                "  id TEXT PRIMARY KEY," +
                "  name TEXT NOT NULL," +
                "  leader_uuid TEXT NOT NULL," +
                "  friendly_fire INTEGER NOT NULL DEFAULT 0," +
                "  created_at INTEGER NOT NULL" +
                ");";

        String createTeamMembers = "CREATE TABLE IF NOT EXISTS team_members (" +
                "  team_id TEXT NOT NULL," +
                "  player_uuid TEXT NOT NULL," +
                "  role TEXT NOT NULL," +
                "  joined_at INTEGER NOT NULL," +
                "  PRIMARY KEY (team_id, player_uuid)," +
                "  FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE" +
                ");";

        String createTeamInvites = "CREATE TABLE IF NOT EXISTS team_invites (" +
                "  token TEXT PRIMARY KEY," +
                "  team_id TEXT NOT NULL," +
                "  inviter_uuid TEXT NOT NULL," +
                "  invitee_uuid TEXT NOT NULL," +
                "  created_at INTEGER NOT NULL," +
                "  expires_at INTEGER NOT NULL," +
                "  FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE" +
                ");";

        String createTeamIndex = "CREATE INDEX IF NOT EXISTS idx_team_members_player ON team_members(player_uuid);";
        String createTeamInvitee = "CREATE INDEX IF NOT EXISTS idx_team_invites_invitee ON team_invites(invitee_uuid);";

        try (var stmt = conn.createStatement()) {
            // Enable foreign keys for all connections created by Hikari
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute(createPlayerDataTable);
            stmt.execute(createUnlockedSpellsTable);
            stmt.execute(createWandBindingsTable);
            stmt.execute(createWandLoadoutsTable);
            stmt.execute(createTeamsTable);
            stmt.execute(createTeamMembers);
            stmt.execute(createTeamInvites);
            stmt.execute(createTeamIndex);
            stmt.execute(createTeamInvitee);

        }

        // --- Migrate existing installs: add any missing columns on player_data ---
        ensurePlayerDataColumns(conn);
    }

    /** Adds new PlayerStats columns if they don't exist (idempotent). */
    private void ensurePlayerDataColumns(Connection conn) throws SQLException {
        var existing = getExistingColumns(conn, "player_data");

        // Column name -> SQL type/default clause
        var needed = java.util.Map.of(
                "max_mana", "INTEGER NOT NULL DEFAULT 100",
                "mana_regen", "INTEGER NOT NULL DEFAULT 0",
                "strength", "INTEGER NOT NULL DEFAULT 0",
                "agility", "INTEGER NOT NULL DEFAULT 0",
                "defense", "INTEGER NOT NULL DEFAULT 0",
                "crit_chance", "INTEGER NOT NULL DEFAULT 0",
                "crit_damage", "INTEGER NOT NULL DEFAULT 0");

        try (var stmt = conn.createStatement()) {
            for (var entry : needed.entrySet()) {
                String col = entry.getKey();
                if (!existing.contains(col)) {
                    stmt.execute("ALTER TABLE player_data ADD COLUMN " + col + " " + entry.getValue() + ";");
                }
            }
        }
    }

    /** PRAGMA table_info to collect existing columns in a table. */
    private Set<String> getExistingColumns(Connection conn, String table) throws SQLException {
        var cols = new HashSet<String>();
        try (var ps = conn.prepareStatement("PRAGMA table_info('" + table + "')");
                var rs = ps.executeQuery()) {
            while (rs.next()) {
                cols.add(rs.getString("name"));
            }
        }
        return cols;
    }

}
