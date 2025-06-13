package io.github.tootertutor.ethyrial.database;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.bukkit.Bukkit;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class SQLiteDatabaseManager {

    private final HikariDataSource dataSource;

    public SQLiteDatabaseManager(File dataFolder) {
        // Bukkit.getLogger().info("Initializing SQLiteDatabaseManager with data folder: " + dataFolder.getAbsolutePath());
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            // Bukkit.getLogger().severe("Failed to create plugin data folder: " + dataFolder.getAbsolutePath());
            throw new RuntimeException("Failed to create plugin data folder.");
        }

        File dbFile = new File(dataFolder, "ethyrial.db");
        // Bukkit.getLogger().info("SQLite database file path: " + dbFile.getAbsolutePath());
        String jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(10);
        config.setPoolName("EthyrialSQLitePool");

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
                "uuid TEXT PRIMARY KEY," +
                "mana INTEGER," +
                "spellPower INTEGER," +
                "bonusHealth INTEGER" +
                ");";

        String createUnlockedSpellsTable = "CREATE TABLE IF NOT EXISTS unlocked_spells (" +
                "uuid TEXT," +
                "spell_id TEXT," +
                "PRIMARY KEY (uuid, spell_id)" +
                ");";

        try (var stmt = conn.createStatement()) {
            stmt.execute(createPlayerDataTable);
            stmt.execute(createUnlockedSpellsTable);
        }
    }
}
