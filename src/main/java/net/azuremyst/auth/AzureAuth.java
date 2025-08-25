package net.azuremyst.auth;

import net.azuremyst.auth.commands.AuthCommand;
import net.azuremyst.auth.config.ConfigManager;
import net.azuremyst.auth.database.DatabaseManager;
import net.azuremyst.auth.listeners.PlayerListener;
import net.azuremyst.auth.managers.AuthManager;
import net.azuremyst.auth.managers.SessionManager;
import net.azuremyst.auth.utils.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * AzureAuth - Плагин авторизации для сервера AzureMyst
 * Обеспечивает безопасную регистрацию и авторизацию игроков
 * 
 * @author AzureMyst Development Team
 * @version 1.0.0
 */
public class AzureAuth extends JavaPlugin {
    
    private static AzureAuth instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private AuthManager authManager;
    private SessionManager sessionManager;
    private MessageUtils messageUtils;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Инициализация компонентов
        initializeComponents();
        
        // Регистрация команд
        registerCommands();
        
        // Регистрация слушателей
        registerListeners();
        
        getLogger().info("§a[AzureAuth] Плагин успешно загружен!");
        getLogger().info("§a[AzureAuth] Версия: " + getDescription().getVersion());
        getLogger().info("§a[AzureAuth] Разработано для сервера AzureMyst");
    }
    
    @Override
    public void onDisable() {
        // Сохранение данных перед отключением
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        
        if (sessionManager != null) {
            sessionManager.saveAllSessions();
        }
        
        getLogger().info("§c[AzureAuth] Плагин отключен!");
    }
    
    /**
     * Инициализация всех компонентов плагина
     */
    private void initializeComponents() {
        try {
            // Менеджер конфигурации
            configManager = new ConfigManager(this);
            configManager.loadConfig();
            
            // Утилиты сообщений
            messageUtils = new MessageUtils(this);
            
            // Менеджер базы данных
            databaseManager = new DatabaseManager(this);
            databaseManager.initialize();
            
            // Менеджер авторизации
            authManager = new AuthManager(this);
            
            // Менеджер сессий
            sessionManager = new SessionManager(this);
            
        } catch (Exception e) {
            getLogger().severe("§c[AzureAuth] Ошибка при инициализации компонентов: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    /**
     * Регистрация команд плагина
     */
    private void registerCommands() {
        AuthCommand authCommand = new AuthCommand(this);
        getCommand("login").setExecutor(authCommand);
        getCommand("register").setExecutor(authCommand);
        getCommand("logout").setExecutor(authCommand);
        getCommand("changepassword").setExecutor(authCommand);
        getCommand("azureauth").setExecutor(authCommand);
    }
    
    /**
     * Регистрация слушателей событий
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }
    
    // Геттеры для доступа к компонентам
    public static AzureAuth getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public AuthManager getAuthManager() {
        return authManager;
    }
    
    public SessionManager getSessionManager() {
        return sessionManager;
    }
    
    public MessageUtils getMessageUtils() {
        return messageUtils;
    }
    
    /**
     * Перезагрузка плагина
     */
    public void reloadPlugin() {
        try {
            configManager.loadConfig();
            messageUtils.reloadMessages();
            getLogger().info("§a[AzureAuth] Плагин успешно перезагружен!");
        } catch (Exception e) {
            getLogger().severe("§c[AzureAuth] Ошибка при перезагрузке: " + e.getMessage());
        }
    }
}
