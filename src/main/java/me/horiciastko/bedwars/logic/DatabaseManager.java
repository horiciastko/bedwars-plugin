package me.horiciastko.bedwars.logic;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import me.horiciastko.bedwars.BedWars;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class DatabaseManager {

    public static class StandaloneNPCRecord {
        private final int id;
        private final String type;
        private final String location;

        public StandaloneNPCRecord(int id, String type, String location) {
            this.id = id;
            this.type = type;
            this.location = location;
        }

        public int getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getLocation() {
            return location;
        }
    }

    private final BedWars plugin;
    private HikariDataSource dataSource;
    @Getter
    private String type;

    public DatabaseManager(BedWars plugin) {
        this.plugin = plugin;
        init();
    }

    private void init() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("database");
        if (config == null) {
            plugin.getLogger().severe("Database configuration missing in config.yml!");
            return;
        }

        this.type = config.getString("type", "sqlite").toLowerCase();
        HikariConfig hikariConfig = new HikariConfig();

        if (type.equals("mysql")) {
            String host = config.getString("mysql.host");
            int port = config.getInt("mysql.port");
            String database = config.getString("mysql.database");
            String username = config.getString("mysql.username");
            String password = config.getString("mysql.password");

            hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            hikariConfig.setMaximumPoolSize(config.getInt("mysql.pool.maximum-pool-size", 10));
        } else {
            File dbFile = new File(plugin.getDataFolder(), "database.db");
            hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            hikariConfig.setDriverClassName("org.sqlite.JDBC");
            hikariConfig.setMaximumPoolSize(1);
            hikariConfig.setConnectionTestQuery("SELECT 1");
        }

        try {
            this.dataSource = new HikariDataSource(hikariConfig);
            createTables();
            plugin.getLogger().info("Database connected successfully using " + type);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not connect to database!", e);
        }
    }

    private void createTables() {
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS bw_players (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "name VARCHAR(16) NOT NULL," +
                    "wins INT DEFAULT 0," +
                    "kills INT DEFAULT 0," +
                    "deaths INT DEFAULT 0," +
                    "final_kills INT DEFAULT 0," +
                    "beds_broken INT DEFAULT 0," +
                    "experience INT DEFAULT 0" +
                    ");";
            stmt.execute(sql);

            String arenasSql = "CREATE TABLE IF NOT EXISTS bw_arenas (" +
                    "name VARCHAR(64) PRIMARY KEY," +
                    "world VARCHAR(64)," +
                    "lobby TEXT," +
                    "pos1 TEXT," +
                    "pos2 TEXT," +
                    "auto_setup BOOLEAN DEFAULT 0," +
                    "min_players INT DEFAULT 2," +
                    "max_players INT DEFAULT 8," +
                    "game_mode VARCHAR(32) DEFAULT 'SOLO'," +
                    "group_name VARCHAR(64) DEFAULT 'default'," +
                    "pvp_mode VARCHAR(32) DEFAULT 'LEGACY_1_8'," +
                    "enabled BOOLEAN DEFAULT 1" +
                    ");";
            stmt.execute(arenasSql);

            String teamsSql = "CREATE TABLE IF NOT EXISTS bw_teams (" +
                    "id INTEGER PRIMARY KEY " + (type.equals("sqlite") ? "AUTOINCREMENT" : "AUTO_INCREMENT") + "," +
                    "arena_name VARCHAR(64) NOT NULL," +
                    "team_name VARCHAR(32) NOT NULL," +
                    "color VARCHAR(16)," +
                    "material VARCHAR(32)," +
                    "spawn TEXT," +
                    "bed TEXT," +
                    "shop TEXT," +
                    "upgrade TEXT," +
                    "base_pos1 TEXT," +
                    "base_pos2 TEXT," +
                    "FOREIGN KEY (arena_name) REFERENCES bw_arenas(name) ON DELETE CASCADE" +
                    ");";
            stmt.execute(teamsSql);

            String generatorsSql = "CREATE TABLE IF NOT EXISTS bw_generators (" +
                    "id INTEGER PRIMARY KEY " + (type.equals("sqlite") ? "AUTOINCREMENT" : "AUTO_INCREMENT") + "," +
                    "arena_name VARCHAR(64) NOT NULL," +
                    "team_name VARCHAR(32)," +
                    "type VARCHAR(32) NOT NULL," +
                    "location TEXT NOT NULL," +
                    "FOREIGN KEY (arena_name) REFERENCES bw_arenas(name) ON DELETE CASCADE" +
                    ");";
            stmt.execute(generatorsSql);

            String signsSql = "CREATE TABLE IF NOT EXISTS bw_signs (" +
                    "id INTEGER PRIMARY KEY " + (type.equals("sqlite") ? "AUTOINCREMENT" : "AUTO_INCREMENT") + "," +
                    "arena_name VARCHAR(64) NOT NULL," +
                    "location TEXT NOT NULL," +
                    "FOREIGN KEY (arena_name) REFERENCES bw_arenas(name) ON DELETE CASCADE" +
                    ");";
            stmt.execute(signsSql);

            String quickBuySql = "CREATE TABLE IF NOT EXISTS bw_player_quickbuy (" +
                    "uuid VARCHAR(36) NOT NULL," +
                    "slot INT NOT NULL," +
                    "category VARCHAR(64)," +
                    "item_key VARCHAR(64)," +
                    "PRIMARY KEY (uuid, slot)" +
                    ");";
            stmt.execute(quickBuySql);

            String settingsSql = "CREATE TABLE IF NOT EXISTS bw_settings (" +
                    "setting_key VARCHAR(64) PRIMARY KEY," +
                    "setting_value TEXT" +
                    ");";
            stmt.execute(settingsSql);

                String standaloneNpcsSql = "CREATE TABLE IF NOT EXISTS bw_standalone_npcs (" +
                    "id INTEGER PRIMARY KEY " + (type.equals("sqlite") ? "AUTOINCREMENT" : "AUTO_INCREMENT") + "," +
                    "npc_type VARCHAR(32) NOT NULL," +
                    "location TEXT NOT NULL" +
                    ");";
                stmt.execute(standaloneNpcsSql);

            String[] migrations = {
                    "ALTER TABLE bw_arenas ADD COLUMN pos1 TEXT;",
                    "ALTER TABLE bw_arenas ADD COLUMN pos2 TEXT;",
                    "ALTER TABLE bw_arenas ADD COLUMN lobby TEXT;",
                    "ALTER TABLE bw_arenas ADD COLUMN auto_setup BOOLEAN DEFAULT 0;",
                    "ALTER TABLE bw_teams ADD COLUMN color VARCHAR(16);",
                    "ALTER TABLE bw_teams ADD COLUMN material VARCHAR(32);",
                    "ALTER TABLE bw_teams ADD COLUMN shop TEXT;",
                    "ALTER TABLE bw_teams ADD COLUMN upgrade TEXT;",
                    "ALTER TABLE bw_teams ADD COLUMN base_pos1 TEXT;",
                    "ALTER TABLE bw_teams ADD COLUMN base_pos2 TEXT;",
                    "ALTER TABLE bw_arenas ADD COLUMN min_players INT DEFAULT 2;",
                    "ALTER TABLE bw_arenas ADD COLUMN max_players INT DEFAULT 8;",
                    "ALTER TABLE bw_arenas ADD COLUMN game_mode VARCHAR(32) DEFAULT 'SOLO';",
                    "ALTER TABLE bw_arenas ADD COLUMN waiting_lobby_pos1 TEXT;",
                    "ALTER TABLE bw_arenas ADD COLUMN waiting_lobby_pos2 TEXT;",
                    "ALTER TABLE bw_arenas ADD COLUMN group_name VARCHAR(64) DEFAULT 'default';",
                    "ALTER TABLE bw_arenas ADD COLUMN pvp_mode VARCHAR(32) DEFAULT 'LEGACY_1_8';",
                    "ALTER TABLE bw_arenas ADD COLUMN enabled BOOLEAN DEFAULT 1;",
                    "ALTER TABLE bw_players ADD COLUMN experience INT DEFAULT 0;"
            };

            for (String migration : migrations) {
                try {
                    stmt.execute(migration);
                    plugin.getLogger().info("Applied database migration: " + migration);
                } catch (SQLException ignored) {
                }
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create database tables!", e);
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null)
            throw new SQLException("DataSource is null");
        return dataSource.getConnection();
    }

    public void setSetting(String key, String value) {
        String sql = type.equals("sqlite")
                ? "INSERT OR REPLACE INTO bw_settings (setting_key, setting_value) VALUES (?, ?)"
                : "INSERT INTO bw_settings (setting_key, setting_value) VALUES (?, ?) ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";
        try (Connection conn = getConnection();
                java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save setting " + key, e);
        }
    }

    public String getSetting(String key) {
        String sql = "SELECT setting_value FROM bw_settings WHERE setting_key = ?";
        try (Connection conn = getConnection();
                java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("setting_value");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load setting " + key, e);
        }
        return null;
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public void savePlayerQuickBuy(java.util.UUID uuid, int slot, String category, String itemKey) {
        String sql = type.equals("sqlite")
                ? "INSERT OR REPLACE INTO bw_player_quickbuy (uuid, slot, category, item_key) VALUES (?, ?, ?, ?)"
                : "INSERT INTO bw_player_quickbuy (uuid, slot, category, item_key) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE category = VALUES(category), item_key = VALUES(item_key)";
        try (Connection conn = getConnection();
                java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, slot);
            ps.setString(3, category);
            ps.setString(4, itemKey);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save quickbuy for " + uuid, e);
        }
    }

    public void removePlayerQuickBuy(java.util.UUID uuid, int slot) {
        String sql = "DELETE FROM bw_player_quickbuy WHERE uuid = ? AND slot = ?";
        try (Connection conn = getConnection();
                java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, slot);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not remove quickbuy for " + uuid, e);
        }
    }

    public java.util.Map<Integer, String[]> getPlayerQuickBuy(java.util.UUID uuid) {
        java.util.Map<Integer, String[]> result = new java.util.HashMap<>();
        String sql = "SELECT slot, category, item_key FROM bw_player_quickbuy WHERE uuid = ?";
        try (Connection conn = getConnection();
                java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getInt("slot"), new String[] { rs.getString("category"), rs.getString("item_key") });
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load quickbuy for " + uuid, e);
        }
        return result;
    }

    public me.horiciastko.bedwars.models.PlayerStats getPlayerStats(java.util.UUID uuid) {
        String sql = "SELECT * FROM bw_players WHERE uuid = ?";
        try (Connection conn = getConnection();
                java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new me.horiciastko.bedwars.models.PlayerStats(
                            uuid,
                            rs.getInt("wins"),
                            rs.getInt("kills"),
                            rs.getInt("deaths"),
                            rs.getInt("final_kills"),
                            rs.getInt("beds_broken"),
                            rs.getInt("experience"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load stats for " + uuid, e);
        }
        return new me.horiciastko.bedwars.models.PlayerStats(uuid);
    }

    public void updatePlayerStats(java.util.UUID uuid, String name, me.horiciastko.bedwars.models.PlayerStats stats) {
        String sql = type.equals("sqlite")
                ? "INSERT OR REPLACE INTO bw_players (uuid, name, wins, kills, deaths, final_kills, beds_broken, experience) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                : "INSERT INTO bw_players (uuid, name, wins, kills, deaths, final_kills, beds_broken, experience) VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                        +
                        "ON DUPLICATE KEY UPDATE name=VALUES(name), wins=VALUES(wins), kills=VALUES(kills), deaths=VALUES(deaths), final_kills=VALUES(final_kills), beds_broken=VALUES(beds_broken), experience=VALUES(experience)";

        try (Connection conn = getConnection();
                java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.setInt(3, stats.getWins());
            ps.setInt(4, stats.getKills());
            ps.setInt(5, stats.getDeaths());
            ps.setInt(6, stats.getFinalKills());
            ps.setInt(7, stats.getBedsBroken());
            ps.setInt(8, stats.getExperience());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save stats for " + uuid, e);
        }
    }

    public int saveStandaloneNPC(String type, String location) {
        String sql = "INSERT INTO bw_standalone_npcs (npc_type, location) VALUES (?, ?)";
        try (Connection conn = getConnection();
                java.sql.PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, type);
            ps.setString(2, location);
            int changed = ps.executeUpdate();
            if (changed > 0) {
                try (java.sql.ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save standalone NPC", e);
        }
        return -1;
    }

    public java.util.List<StandaloneNPCRecord> loadStandaloneNPCs() {
        java.util.List<StandaloneNPCRecord> npcs = new java.util.ArrayList<>();
        String sql = "SELECT id, npc_type, location FROM bw_standalone_npcs";

        try (Connection conn = getConnection();
                java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                npcs.add(new StandaloneNPCRecord(
                        rs.getInt("id"),
                        rs.getString("npc_type"),
                        rs.getString("location")));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load standalone NPCs", e);
        }

        return npcs;
    }

    public void deleteStandaloneNPC(int id) {
        String sql = "DELETE FROM bw_standalone_npcs WHERE id = ?";

        try (Connection conn = getConnection();
                java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not delete standalone NPC id=" + id, e);
        }
    }
}
