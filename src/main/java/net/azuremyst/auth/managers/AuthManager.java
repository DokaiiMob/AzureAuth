package net.azuremyst.auth.managers;

import net.azuremyst.auth.AzureAuth;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Менеджер авторизации игроков
 */
public class AuthManager {
    
    private final AzureAuth plugin;
    private final Map<UUID, Boolean> authenticatedPlayers;
    private final Map<UUID, String> captchaCodes;
    
    public AuthManager(AzureAuth plugin) {
        this.plugin = plugin;
        this.authenticatedPlayers = new HashMap<>();
        this.captchaCodes = new HashMap<>();
    }
    
    /**
     * Проверка авторизации игрока
     */
    public boolean isAuthenticated(Player player) {
        return authenticatedPlayers.getOrDefault(player.getUniqueId(), false);
    }
    
    /**
     * Попытка входа в систему
     */
    public void attemptLogin(Player player, String password) {
        UUID uuid = player.getUniqueId();
        
        // Проверка существования игрока в БД
        if (!plugin.getDatabaseManager().playerExists(uuid)) {
            plugin.getMessageUtils().sendMessage(player, "registration-required");
            return;
        }
        
        // Проверка пароля
        if (plugin.getDatabaseManager().checkPassword(uuid, password)) {
            // Успешный вход
            authenticatedPlayers.put(uuid, true);
            plugin.getDatabaseManager().updateLastLogin(uuid, 
                player.getAddress().getAddress().getHostAddress());
            plugin.getMessageUtils().sendMessage(player, "login-success");
        } else {
            // Неверный пароль
            plugin.getDatabaseManager().incrementFailedAttempts(uuid);
            int attempts = plugin.getDatabaseManager().getFailedAttempts(uuid);
            int maxAttempts = plugin.getConfigManager().getMaxLoginAttempts();
            
            plugin.getMessageUtils().sendMessage(player, "wrong-password", 
                String.valueOf(maxAttempts - attempts));
        }
    }
    
    /**
     * Регистрация игрока
     */
    public void registerPlayer(Player player, String password) {
        UUID uuid = player.getUniqueId();
        String username = player.getName();
        String ipAddress = player.getAddress().getAddress().getHostAddress();
        
        if (plugin.getDatabaseManager().registerPlayer(uuid, username, password, ipAddress)) {
            authenticatedPlayers.put(uuid, true);
            plugin.getMessageUtils().sendMessage(player, "registration-success");
        } else {
            plugin.getMessageUtils().sendMessage(player, "registration-failed");
        }
    }
    
    /**
     * Выход из системы
     */
    public void logout(Player player) {
        authenticatedPlayers.remove(player.getUniqueId());
        plugin.getDatabaseManager().deactivateAllSessions(player.getUniqueId());
    }
    
    /**
     * Принудительная авторизация (для админов)
     */
    public void forceLogin(Player player) {
        authenticatedPlayers.put(player.getUniqueId(), true);
    }
    
    /**
     * Получить количество авторизованных игроков
     */
    public int getAuthenticatedCount() {
        return authenticatedPlayers.size();
    }
    
    /**
     * Очистка данных при выходе игрока
     */
    public void cleanupPlayer(UUID uuid) {
        authenticatedPlayers.remove(uuid);
        captchaCodes.remove(uuid);
    }
}
