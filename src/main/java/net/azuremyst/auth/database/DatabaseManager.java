package net.azuremyst.auth.database;

import net.azuremyst.auth.AzureAuth;
import net.azuremyst.auth.models.PlayerData;
import net.azuremyst.auth.utils.PasswordUtils;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Менеджер базы данных для плагина AzureAuth
 */
public class DatabaseManager {
    
    private final AzureAuth plugin;
    private Connection connection;
    private String tablePrefix;
    
    public DatabaseManager(AzureAuth plugin) {
        this.plugin = plugin;
        this.tablePrefix = plugin.getConfigManager().getDatabaseTablePrefix();
    }
    
    /**
     * Инициализация подключения к базе данных
     */
    public void initialize() throws SQLException {
        String databaseType = plugin.getConfigManager().getDatabaseType();
        
        if ("MYSQL".equalsIgnoreCase(databaseType)) {
            initializeMysql();
        } else {
            initializeSqlite();
        }
        
        createTables();
        plugin.getLogger().info("§a[AzureAuth] База данных успешно инициализирована!");
    }
    
    /**
     * Инициализация MySQL соединения
     */
    private void initializeMysql() throws SQLException {
        String host = plugin.getConfigManager().getDatabaseHost();
        int port = plugin.getConfigManager().getDatabasePort();
        String database = plugin.getConfigManager().getDatabaseName();
        String username = plugin.getConfigManager().getDatabaseUsername();
        String password = plugin.getConfigManager().getDatabasePassword();
        
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + 
                    "?useSSL=false&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true";
        
        connection = DriverManager.getConnection(url, username, password);
        plugin.getLogger().info("§a[AzureAuth] Подключение к MySQL успешно установлено!");
    }
    
    /**
     * Инициализация SQLite соединения
     */
    private void initializeSqlite() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        String url = "jdbc:sqlite:" + dataFolder.getAbsolutePath() + "/database.db";
        connection = DriverManager.getConnection(url);
        plugin.getLogger().info("§a[AzureAuth] Подключение к SQLite успешно установлено!");
    }
    
    /**
     * Создание таблиц в базе данных
     */
    private void createTables() throws SQLException {
        // Таблица пользователей
        String createUsersTable = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "users (" +
                "id INTEGER PRIMARY KEY " + (isMySQL() ? "AUTO_INCREMENT" : "AUTOINCREMENT") + "," +
                "uuid VARCHAR(36) UNIQUE NOT NULL," +
                "username VARCHAR(16) NOT NULL," +
                "password_hash VARCHAR(128) NOT NULL," +
                "salt VARCHAR(32) NOT NULL," +
                "email VARCHAR(100)," +
                "ip_address VARCHAR(45)," +
                "registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "last_login TIMESTAMP," +
                "is_verified BOOLEAN DEFAULT FALSE," +
                "failed_attempts INTEGER DEFAULT 0," +
                "locked_until TIMESTAMP NULL" +
                ")";
        
        // Таблица сессий
        String createSessionsTable = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "sessions (" +
                "id INTEGER PRIMARY KEY " + (isMySQL() ? "AUTO_INCREMENT" : "AUTOINCREMENT") + "," +
                "uuid VARCHAR(36) NOT NULL," +
                "ip_address VARCHAR(45) NOT NULL," +
                "session_token VARCHAR(64) UNIQUE NOT NULL," +
                "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "expires_date TIMESTAMP NOT NULL," +
                "is_active BOOLEAN DEFAULT TRUE" +
                ")";
        
        // Таблица логов
        String createLogsTable = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "logs (" +
                "id INTEGER PRIMARY KEY " + (isMySQL() ? "AUTO_INCREMENT" : "AUTOINCREMENT") + "," +
                "uuid VARCHAR(36)," +
                "username VARCHAR(16)," +
                "action VARCHAR(50) NOT NULL," +
                "ip_address VARCHAR(45)," +
                "details TEXT," +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createUsersTable);
            stmt.executeUpdate(createSessionsTable);
            stmt.executeUpdate(createLogsTable);
        }
    }
    
    /**
     * Регистрация нового игрока
     */
    public boolean registerPlayer(UUID uuid, String username, String password, String ipAddress) {
        String sql = "INSERT INTO " + tablePrefix + "users (uuid, username, password_hash, salt, ip_address) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String salt = PasswordUtils.generateSalt();
            String hashedPassword = PasswordUtils.hashPassword(password, salt);
            
            stmt.setString(1, uuid.toString());
            stmt.setString(2, username);
            stmt.setString(3, hashedPassword);
            stmt.setString(4, salt);
            stmt.setString(5, ipAddress);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                logAction(uuid, username, "REGISTER", ipAddress, "Successful registration");
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("§c[AzureAuth] Ошибка при регистрации игрока: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Проверка пароля игрока
     */
    public boolean checkPassword(UUID uuid, String password) {
        String sql = "SELECT password_hash, salt FROM " + tablePrefix + "users WHERE uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String salt = rs.getString("salt");
                String hashedPassword = PasswordUtils.hashPassword(password, salt);
                
                return storedHash.equals(hashedPassword);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("§c[AzureAuth] Ошибка при проверке пароля: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Проверка существования игрока
     */
    public boolean playerExists(UUID uuid) {
        String sql = "SELECT 1 FROM " + tablePrefix + "users WHERE uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            plugin.getLogger().severe("§c[AzureAuth] Ошибка при проверке существования игрока: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Обновление времени последнего входа
     */
    public void updateLastLogin(UUID uuid, String ipAddress) {
        String sql = "UPDATE " + tablePrefix + "users SET last_login = CURRENT_TIMESTAMP, ip_address = ?, failed_attempts = 0 WHERE uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ipAddress);
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("§c[AzureAuth] Ошибка при обновлении времени входа: " + e.getMessage());
        }
    }
    
    /**
     * Увеличение счетчика неудачных попыток
     */
    public void incrementFailedAttempts(UUID uuid) {
        String sql = "UPDATE " + tablePrefix + "users SET failed_attempts = failed_attempts + 1 WHERE uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("§c[AzureAuth] Ошибка при обновлении неудачных попыток: " + e.getMessage());
        }
    }
    
    /**
     * Получение количества неудачных попыток
     */
    public int getFailedAttempts(UUID uuid) {
        String sql = "SELECT failed_attempts FROM " + tablePrefix + "users WHERE uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("failed_attempts");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("§c[AzureAuth] Ошибка при получении неудачных попыток: " + e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * Изменение пароля
     */
    public boolean changePassword(UUID uuid, String newPassword) {
        String sql = "UPDATE " + tablePrefix + "users SET password_hash = ?, salt = ? WHERE uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String salt = PasswordUtils.generateSalt();
            String hashedPassword = PasswordUtils.hashPassword(newPassword, salt);
            
            stmt.setString(1, hashedPassword);
            stmt.setString(2, salt);
            stmt.setString(3, uuid.toString());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("§c[AzureAuth] Ошибка при изменении пароля: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Сохранение сессии
     */
    public void saveSession(UUID uuid, String ipAddress, String sessionToken, long expirationTime) {
        String sql = "INSERT INTO " + tablePrefix + "sessions (uuid, ip_address, session_token, expires_date) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, ipAddress);
            stmt.setString(3, sessionToken);
            stmt.setTimestamp(4, new Timestamp(expirationTime));
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("§c[AzureAuth] Ошибка при сохранении сессии: " + e.getMessage());
        }
    }
    
    /**
     * Проверка валидности сессии
     */
    public boolean isValidSession(UUID uuid, String ipAddress, String sessionToken) {
        String sql = "SELECT 1 FROM " + tablePrefix + "sessions WHERE uuid = ? AND ip_address = ? AND session_token = ? AND expires_date > CURRENT_TIMESTAMP AND is_active = TRUE";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, ipAddress);
            stmt.setString(3, sessionToken);
            
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            plugin.getLogger().severe("§c[AzureAuth] Ошибка при проверке сессии: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Деактивация всех сессий игрока
     */
    public void deactivateAllSessions(UUID uuid) {
        String sql = "UPDATE " + tablePrefix + "sessions SET is_active = FALSE WHERE uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("§c[AzureAuth] Ошибка при деактивации сессий: " + e.getMessage());
        }
    }
    
    /**
     * Логирование действий
     */
    public void logAction(UUID uuid, String username, String action, String ipAddress, String details) {
        if (!plugin.getConfigManager().isLoggingEnabled()) {
            return;
        }
        
        String sql = "INSERT INTO " + tablePrefix + "logs (uuid, username, action, ip_address, details) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid != null ? uuid.toString() : null);
            stmt.setString(2, username);
            stmt.setString(3, action);
            stmt.setString(4, ipAddress);
            stmt.setString(5, details);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("§c[AzureAuth] Ошибка при логировании: " + e.getMessage());
        }
    }
    
    /**
     * Получение данных игрока
     */
    public PlayerData getPlayerData(UUID uuid) {
        String sql = "SELECT * FROM " + tablePrefix + "users WHERE uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                PlayerData data = new PlayerData();
                data.setUuid(uuid);
                data.setUsername(rs.getString("username"));
                data.setEmail(rs.getString("email"));
                data.setIpAddress(rs.getString("ip_address"));
                data.setRegistrationDate(rs.getTimestamp("registration_date"));
                data.setLastLogin(rs.getTimestamp("last_login"));
                data.setVerified(rs.getBoolean("is_verified"));
                data.setFailedAttempts(rs.getInt("failed_attempts"));
                data.setLockedUntil(rs.getTimestamp("locked_until"));
                
                return data;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("§c[AzureAuth] Ошибка при получении данных игрока: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Очистка старых сессий
     */
    public void cleanupExpiredSessions() {
        String sql = "DELETE FROM " + tablePrefix + "sessions WHERE expires_date < CURRENT_TIMESTAMP";
        
        try (Statement stmt = connection.createStatement()) {
            int deleted = stmt.executeUpdate(sql);
            if (deleted > 0) {
                plugin.getLogger().info("§a[AzureAuth] Очищено " + deleted + " устаревших сессий");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("§c[AzureAuth] Ошибка при очистке сессий: " + e.getMessage());
        }
    }
    
    /**
     * Проверка типа базы данных
     */
    private boolean isMySQL() {
        return "MYSQL".equalsIgnoreCase(plugin.getConfigManager().getDatabaseType());
    }
    
    /**
     * Закрытие соединения с базой данных
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("§a[AzureAuth] Соединение с базой данных закрыто");
            } catch (SQLException e) {
                plugin.getLogger().severe("§c[AzureAuth] Ошибка при закрытии соединения: " + e.getMessage());
            }
        }
    }
    
    /**
     * Проверка активности соединения
     */
    public boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Переподключение к базе данных
     */
    public void reconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            initialize();
        } catch (SQLException e) {
            plugin.getLogger().severe("§c[AzureAuth] Ошибка при переподключении к БД: " + e.getMessage());
        }
    }
}
