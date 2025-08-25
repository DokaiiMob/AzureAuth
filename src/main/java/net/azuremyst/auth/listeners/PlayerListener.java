package net.azuremyst.auth.listeners;

import net.azuremyst.auth.AzureAuth;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

/**
 * Слушатель событий игроков для авторизации
 */
public class PlayerListener implements Listener {
    
    private final AzureAuth plugin;
    
    public PlayerListener(AzureAuth plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Проверка валидной сессии
        if (plugin.getSessionManager().isValidSession(player)) {
            plugin.getAuthManager().forceLogin(player);
            plugin.getMessageUtils().sendMessage(player, "session-restored");
            return;
        }
        
        // Проверка регистрации
        if (plugin.getDatabaseManager().playerExists(player.getUniqueId())) {
            plugin.getMessageUtils().sendMessage(player, "returning-player");
        } else {
            plugin.getMessageUtils().sendMessage(player, "first-join");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getAuthManager().cleanupPlayer(player.getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getAuthManager().isAuthenticated(player)) {
            if (plugin.getConfigManager().isChatBlocked()) {
                event.setCancelled(true);
                plugin.getMessageUtils().sendMessage(player, "chat-blocked");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getAuthManager().isAuthenticated(player)) {
            if (plugin.getConfigManager().isMovementBlocked()) {
                event.setCancelled(true);
                plugin.getMessageUtils().sendMessage(player, "movement-blocked");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getAuthManager().isAuthenticated(player)) {
            if (plugin.getConfigManager().areCommandsBlocked()) {
                String command = event.getMessage().split(" ")[0].toLowerCase();
                
                // Разрешенные команды
                if (!isAllowedCommand(command)) {
                    event.setCancelled(true);
                    plugin.getMessageUtils().sendMessage(player, "command-blocked");
                }
            }
        }
    }
    
    /**
     * Проверка разрешенных команд
     */
    private boolean isAllowedCommand(String command) {
        String[] allowedCommands = {
            "/login", "/l", "/войти",
            "/register", "/reg", "/регистрация",
            "/help", "/?"
        };
        
        for (String allowed : allowedCommands) {
            if (command.startsWith(allowed)) {
                return true;
            }
        }
        
        return false;
    }
}
