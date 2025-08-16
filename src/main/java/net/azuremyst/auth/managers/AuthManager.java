package net.azuremyst.auth.managers;

import net.azuremyst.auth.AzureAuth;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * РњРµРЅРµРґР¶РµСЂ Р°РІС‚РѕСЂРёР·Р°С†РёРё РёРіСЂРѕРєРѕРІ
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
     * РџСЂРѕРІРµСЂРєР° Р°РІС‚РѕСЂРёР·Р°С†РёРё РёРіСЂРѕРєР°
     */
    public boolean isAuthenticated(Player player) {
        return authenticatedPlayers.getOrDefault(player.getUniqueId(), false);
    }
    
    /**
     * РџРѕРїС‹С‚РєР° РІС…РѕРґР° РІ СЃРёСЃС‚РµРјСѓ
     */
    public void attemptLogin(Player player, String password) {
        UUID uuid = player.getUniqueId();
        
        // РџСЂРѕРІРµСЂРєР° СЃСѓС‰РµСЃС‚РІРѕРІР°РЅРёСЏ РёРіСЂРѕРєР° РІ Р‘Р”
        if (!plugin.getDatabaseManager().playerExists(uuid)) {
            plugin.getMessageUtils().sendMessage(player, "registration-required");
            return;
        }
        
        // РџСЂРѕРІРµСЂРєР° РїР°СЂРѕР»СЏ
        if (plugin.getDatabaseManager().checkPassword(uuid, password)) {
            // РЈСЃРїРµС€РЅС‹Р№ РІС…РѕРґ
            authenticatedPlayers.put(uuid, true);
            plugin.getDatabaseManager().updateLastLogin(uuid, 
                player.getAddress().getAddress().getHostAddress());
            plugin.getMessageUtils().sendMessage(player, "login-success");
        } else {
            // РќРµРІРµСЂРЅС‹Р№ РїР°СЂРѕР»СЊ
            plugin.getDatabaseManager().incrementFailedAttempts(uuid);
            int attempts = plugin.getDatabaseManager().getFailedAttempts(uuid);
            int maxAttempts = plugin.getConfigManager().getMaxLoginAttempts();
            
            plugin.getMessageUtils().sendMessage(player, "wrong-password", 
                String.valueOf(maxAttempts - attempts));
        }
    }
    
    /**
     * Р РµРіРёСЃС‚СЂР°С†РёСЏ РёРіСЂРѕРєР°
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
     * Р’С‹С…РѕРґ РёР· СЃРёСЃС‚РµРјС‹
     */
    public void logout(Player player) {
        authenticatedPlayers.remove(player.getUniqueId());
        plugin.getDatabaseManager().deactivateAllSessions(player.getUniqueId());
    }
    
    /**
     * РџСЂРёРЅСѓРґРёС‚РµР»СЊРЅР°СЏ Р°РІС‚РѕСЂРёР·Р°С†РёСЏ (РґР»СЏ Р°РґРјРёРЅРѕРІ)
     */
    public void forceLogin(Player player) {
        authenticatedPlayers.put(player.getUniqueId(), true);
    }
    
    /**
     * РџРѕР»СѓС‡РёС‚СЊ РєРѕР»РёС‡РµСЃС‚РІРѕ Р°РІС‚РѕСЂРёР·РѕРІР°РЅРЅС‹С… РёРіСЂРѕРєРѕРІ
     */
    public int getAuthenticatedCount() {
        return authenticatedPlayers.size();
    }
    
    /**
     * РћС‡РёСЃС‚РєР° РґР°РЅРЅС‹С… РїСЂРё РІС‹С…РѕРґРµ РёРіСЂРѕРєР°
     */
    public void cleanupPlayer(UUID uuid) {
        authenticatedPlayers.remove(uuid);
        captchaCodes.remove(uuid);
    }
}
