package net.azuremyst.auth.config;

import net.azuremyst.auth.AzureAuth;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Менеджер конфигурации для плагина AzureAuth
 */
public class ConfigManager {
    
    private final AzureAuth plugin;
    private FileConfiguration config;
    
    public ConfigManager(AzureAuth plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Загрузка конфигурации
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    // Настройки базы данных
    public String getDatabaseType() {
        return config.getString("database.type", "SQLITE");
    }
    
    public String getDatabaseHost() {
        return config.getString("database.mysql.host", "localhost");
    }
    
    public int getDatabasePort() {
        return config.getInt("database.mysql.port", 3306);
    }
    
    public String getDatabaseName() {
        return config.getString("database.mysql.database", "azureauth");
    }
    
    public String getDatabaseUsername() {
        return config.getString("database.mysql.username", "root");
    }
    
    public String getDatabasePassword() {
        return config.getString("database.mysql.password", "password");
    }
    
    public String getDatabaseTablePrefix() {
        return config.getString("database.table-prefix", "azureauth_");
    }
    
    // Настройки авторизации
    public boolean isRegistrationEnabled() {
        return config.getBoolean("auth.registration-enabled", true);
    }
    
    public int getMinPasswordLength() {
        return config.getInt("auth.min-password-length", 6);
    }
    
    public int getMaxPasswordLength() {
        return config.getInt("auth.max-password-length", 32);
    }
    
    public int getLoginTimeout() {
        return config.getInt("auth.login-timeout", 60);
    }
    
    public int getMaxLoginAttempts() {
        return config.getInt("auth.max-login-attempts", 3);
    }
    
    public boolean isSessionEnabled() {
        return config.getBoolean("auth.sessions.enabled", true);
    }
    
    public int getSessionDuration() {
        return config.getInt("auth.sessions.duration", 3600);
    }
    
    public boolean isCaptchaEnabled() {
        return config.getBoolean("auth.captcha.enabled", true);
    }
    
    public int getCaptchaAfterAttempts() {
        return config.getInt("auth.captcha.after-attempts", 2);
    }
    
    // Настройки безопасности
    public boolean isIpWhitelistEnabled() {
        return config.getBoolean("security.ip-whitelist.enabled", false);
    }
    
    public boolean isForceSecurePassword() {
        return config.getBoolean("security.force-secure-password", true);
    }
    
    public boolean isEmailVerificationEnabled() {
        return config.getBoolean("security.email-verification.enabled", false);
    }
    
    public int getBruteForceProtectionTime() {
        return config.getInt("security.brute-force-protection-time", 300);
    }
    
    // Настройки сообщений
    public String getLanguage() {
        return config.getString("messages.language", "ru");
    }
    
    public boolean isChatBlocked() {
        return config.getBoolean("restrictions.block-chat", true);
    }
    
    public boolean isMovementBlocked() {
        return config.getBoolean("restrictions.block-movement", true);
    }
    
    public boolean areCommandsBlocked() {
        return config.getBoolean("restrictions.block-commands", true);
    }
    
    public boolean isInventoryBlocked() {
        return config.getBoolean("restrictions.block-inventory", true);
    }
    
    // Настройки уведомлений
    public boolean isBossBarEnabled() {
        return config.getBoolean("notifications.bossbar.enabled", true);
    }
    
    public boolean isActionBarEnabled() {
        return config.getBoolean("notifications.actionbar.enabled", true);
    }
    
    public boolean isTitleEnabled() {
        return config.getBoolean("notifications.title.enabled", true);
    }
    
    public boolean isSoundEnabled() {
        return config.getBoolean("notifications.sound.enabled", true);
    }
    
    // Настройки логирования
    public boolean isLoggingEnabled() {
        return config.getBoolean("logging.enabled", true);
    }
    
    public boolean isLogToFile() {
        return config.getBoolean("logging.log-to-file", true);
    }
    
    public boolean isLogToConsole() {
        return config.getBoolean("logging.log-to-console", true);
    }
    
    public boolean isLogLoginAttempts() {
        return config.getBoolean("logging.log-login-attempts", true);
    }
    
    public boolean isLogRegistrations() {
        return config.getBoolean("logging.log-registrations", true);
    }
}
