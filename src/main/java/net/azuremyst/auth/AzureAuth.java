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
 * AzureAuth - РџР»Р°РіРёРЅ Р°РІС‚РѕСЂРёР·Р°С†РёРё РґР»СЏ СЃРµСЂРІРµСЂР° AzureMyst
 * РћР±РµСЃРїРµС‡РёРІР°РµС‚ Р±РµР·РѕРїР°СЃРЅСѓСЋ СЂРµРіРёСЃС‚СЂР°С†РёСЋ Рё Р°РІС‚РѕСЂРёР·Р°С†РёСЋ РёРіСЂРѕРєРѕРІ
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
        
        // РРЅРёС†РёР°Р»РёР·Р°С†РёСЏ РєРѕРјРїРѕРЅРµРЅС‚РѕРІ
        initializeComponents();
        
        // Р РµРіРёСЃС‚СЂР°С†РёСЏ РєРѕРјР°РЅРґ
        registerCommands();
        
        // Р РµРіРёСЃС‚СЂР°С†РёСЏ СЃР»СѓС€Р°С‚РµР»РµР№
        registerListeners();
        
        getLogger().info("В§a[AzureAuth] РџР»Р°РіРёРЅ СѓСЃРїРµС€РЅРѕ Р·Р°РіСЂСѓР¶РµРЅ!");
        getLogger().info("В§a[AzureAuth] Р’РµСЂСЃРёСЏ: " + getDescription().getVersion());
        getLogger().info("В§a[AzureAuth] Р Р°Р·СЂР°Р±РѕС‚Р°РЅРѕ РґР»СЏ СЃРµСЂРІРµСЂР° AzureMyst");
    }
    
    @Override
    public void onDisable() {
        // РЎРѕС…СЂР°РЅРµРЅРёРµ РґР°РЅРЅС‹С… РїРµСЂРµРґ РѕС‚РєР»СЋС‡РµРЅРёРµРј
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        
        if (sessionManager != null) {
            sessionManager.saveAllSessions();
        }
        
        getLogger().info("В§c[AzureAuth] РџР»Р°РіРёРЅ РѕС‚РєР»СЋС‡РµРЅ!");
    }
    
    /**
     * РРЅРёС†РёР°Р»РёР·Р°С†РёСЏ РІСЃРµС… РєРѕРјРїРѕРЅРµРЅС‚РѕРІ РїР»Р°РіРёРЅР°
     */
    private void initializeComponents() {
        try {
            // РњРµРЅРµРґР¶РµСЂ РєРѕРЅС„РёРіСѓСЂР°С†РёРё
            configManager = new ConfigManager(this);
            configManager.loadConfig();
            
            // РЈС‚РёР»РёС‚С‹ СЃРѕРѕР±С‰РµРЅРёР№
            messageUtils = new MessageUtils(this);
            
            // РњРµРЅРµРґР¶РµСЂ Р±Р°Р·С‹ РґР°РЅРЅС‹С…
            databaseManager = new DatabaseManager(this);
            databaseManager.initialize();
            
            // РњРµРЅРµРґР¶РµСЂ Р°РІС‚РѕСЂРёР·Р°С†РёРё
            authManager = new AuthManager(this);
            
            // РњРµРЅРµРґР¶РµСЂ СЃРµСЃСЃРёР№
            sessionManager = new SessionManager(this);
            
        } catch (Exception e) {
            getLogger().severe("В§c[AzureAuth] РћС€РёР±РєР° РїСЂРё РёРЅРёС†РёР°Р»РёР·Р°С†РёРё РєРѕРјРїРѕРЅРµРЅС‚РѕРІ: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    /**
     * Р РµРіРёСЃС‚СЂР°С†РёСЏ РєРѕРјР°РЅРґ РїР»Р°РіРёРЅР°
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
     * Р РµРіРёСЃС‚СЂР°С†РёСЏ СЃР»СѓС€Р°С‚РµР»РµР№ СЃРѕР±С‹С‚РёР№
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }
    
    // Р“РµС‚С‚РµСЂС‹ РґР»СЏ РґРѕСЃС‚СѓРїР° Рє РєРѕРјРїРѕРЅРµРЅС‚Р°Рј
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
     * РџРµСЂРµР·Р°РіСЂСѓР·РєР° РїР»Р°РіРёРЅР°
     */
    public void reloadPlugin() {
        try {
            configManager.loadConfig();
            messageUtils.reloadMessages();
            getLogger().info("В§a[AzureAuth] РџР»Р°РіРёРЅ СѓСЃРїРµС€РЅРѕ РїРµСЂРµР·Р°РіСЂСѓР¶РµРЅ!");
        } catch (Exception e) {
            getLogger().severe("В§c[AzureAuth] РћС€РёР±РєР° РїСЂРё РїРµСЂРµР·Р°РіСЂСѓР·РєРµ: " + e.getMessage());
        }
    }
}
