package net.azuremyst.auth.managers;

import net.azuremyst.auth.AzureAuth;
import net.azuremyst.auth.utils.PasswordUtils;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Менеджер сессий игроков
 */
public class SessionManager {
    
    private final AzureAuth plugin;
    private final Map<UUID, String> activeSessions;
    
    public SessionManager(AzureAuth plugin) {
        this.plugin = plugin;
        this.activeSessions = new HashMap<>();
    }
    
    /**
     * Создание новой сессии
     */
    public void createSession(Player player) {
        if (!plugin.getConfigManager().isSessionEnabled()) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        String ipAddress = player.getAddress().getAddress().getHostAddress();
        String sessionToken = PasswordUtils.generateSessionToken();
        
        long duration = plugin.getConfigManager().getSessionDuration() * 1000L;
        long expirationTime = System.currentTimeMillis() + duration;
        
        plugin.getDatabaseManager().saveSession(uuid, ipAddress, sessionToken, expirationTime);
        activeSessions.put(uuid, sessionToken);
    }
    
    /**
     * Проверка валидности сессии
     */
    public boolean isValidSession(Player player) {
        if (!plugin.getConfigManager().isSessionEnabled()) {
            return false;
        }
        
        UUID uuid = player.getUniqueId();
        String ipAddress = player.getAddress().getAddress().getHostAddress();
        String sessionToken = activeSessions.get(uuid);
        
        if (sessionToken == null) {
            return false;
        }
        
        return plugin.getDatabaseManager().isValidSession(uuid, ipAddress, sessionToken);
    }
    
    /**
     * Деактивация сессии
     */
    public void deactivateSession(UUID uuid) {
        activeSessions.remove(uuid);
        plugin.getDatabaseManager().deactivateAllSessions(uuid);
    }
    
    /**
     * Сохранение всех активных сессий
     */
    public void saveAllSessions() {
        // Сессии автоматически сохраняются в базе данных
        activeSessions.clear();
    }
}
