package net.azuremyst.auth.utils;

import net.azuremyst.auth.AzureAuth;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Утилиты для работы с сообщениями
 */
public class MessageUtils {
    
    private final AzureAuth plugin;
    private FileConfiguration messages;
    private final Map<String, String> messageCache;
    
    public MessageUtils(AzureAuth plugin) {
        this.plugin = plugin;
        this.messageCache = new HashMap<>();
        loadMessages();
    }
    
    /**
     * Загрузка сообщений
     */
    public void loadMessages() {
        String language = plugin.getConfigManager().getLanguage();
        File messagesFile = new File(plugin.getDataFolder(), "messages_" + language + ".yml");
        
        if (!messagesFile.exists()) {
            plugin.saveResource("messages_" + language + ".yml", false);
        }
        
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        
        // Загрузка по умолчанию из ресурсов
        InputStream defConfigStream = plugin.getResource("messages_" + language + ".yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            messages.setDefaults(defConfig);
        }
        
        cacheMessages();
    }
    
    /**
     * Кэширование сообщений для производительности
     */
    private void cacheMessages() {
        messageCache.clear();
        for (String key : messages.getKeys(false)) {
            String value = messages.getString(key);
            if (value != null) {
                messageCache.put(key, colorize(value));
            }
        }
    }
    
    /**
     * Отправка сообщения игроку
     */
    public void sendMessage(Player player, String key) {
        sendMessage(player, key, (String[]) null);
    }
    
    /**
     * Отправка сообщения игроку с параметрами
     */
    public void sendMessage(Player player, String key, String... params) {
        String message = getMessage(key, params);
        if (message != null && !message.isEmpty()) {
            String prefix = plugin.getConfig().getString("messages.prefix", "");
            if (plugin.getConfig().getBoolean("messages.show-prefix", true) && !prefix.isEmpty()) {
                message = colorize(prefix) + " " + message;
            }
            player.sendMessage(message);
        }
    }
    
    /**
     * Получение сообщения по ключу
     */
    public String getMessage(String key, String... params) {
        String message = messageCache.get(key);
        
        if (message == null) {
            message = messages.getString(key);
            if (message != null) {
                message = colorize(message);
                messageCache.put(key, message);
            }
        }
        
        if (message == null) {
            return "§cСообщение не найдено: " + key;
        }
        
        // Замена параметров
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                message = message.replace("{" + i + "}", params[i]);
            }
        }
        
        return message;
    }
    
    /**
     * Преобразование цветовых кодов
     */
    public String colorize(String message) {
        if (message == null) return null;
        return message.replace('&', '§');
    }
    
    /**
     * Перезагрузка сообщений
     */
    public void reloadMessages() {
        loadMessages();
    }
    
    /**
     * Отправка Title сообщения
     */
    public void sendTitle(Player player, String titleKey, String subtitleKey) {
        if (!plugin.getConfigManager().isTitleEnabled()) {
            return;
        }
        
        String title = getMessage(titleKey);
        String subtitle = getMessage(subtitleKey);
        
        player.sendTitle(title, subtitle, 10, 70, 20);
    }
    
    /**
     * Отправка Action Bar сообщения
     */
    public void sendActionBar(Player player, String key, String... params) {
        if (!plugin.getConfigManager().isActionBarEnabled()) {
            return;
        }
        
        String message = getMessage(key, params);
        // Используем spigot API для action bar
        try {
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
        } catch (Exception e) {
            // Fallback - отправляем как обычное сообщение
            player.sendMessage(message);
        }
    }
}
