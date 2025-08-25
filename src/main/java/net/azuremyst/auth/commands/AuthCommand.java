package net.azuremyst.auth.commands;

import net.azuremyst.auth.AzureAuth;
import net.azuremyst.auth.utils.PasswordUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Обработчик команд авторизации
 */
public class AuthCommand implements CommandExecutor, TabCompleter {
    
    private final AzureAuth plugin;
    
    public AuthCommand(AzureAuth plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmdName = command.getName().toLowerCase();
        
        switch (cmdName) {
            case "login":
                return handleLogin(sender, args);
            case "register":
                return handleRegister(sender, args);
            case "logout":
                return handleLogout(sender, args);
            case "changepassword":
                return handleChangePassword(sender, args);
            case "azureauth":
                return handleAdmin(sender, args);
            default:
                return false;
        }
    }
    
    /**
     * Обработка команды входа
     */
    private boolean handleLogin(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (plugin.getAuthManager().isAuthenticated(player)) {
            plugin.getMessageUtils().sendMessage(player, "already-logged-in");
            return true;
        }
        
        if (args.length != 1) {
            plugin.getMessageUtils().sendMessage(player, "login-usage");
            return true;
        }
        
        String password = args[0];
        plugin.getAuthManager().attemptLogin(player, password);
        return true;
    }
    
    /**
     * Обработка команды регистрации
     */
    private boolean handleRegister(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!plugin.getConfigManager().isRegistrationEnabled()) {
            plugin.getMessageUtils().sendMessage(player, "registration-disabled");
            return true;
        }
        
        if (plugin.getDatabaseManager().playerExists(player.getUniqueId())) {
            plugin.getMessageUtils().sendMessage(player, "already-registered");
            return true;
        }
        
        if (args.length == 0) {
            plugin.getMessageUtils().sendMessage(player, "register-usage");
            return true;
        }
        
        String password;
        String confirmPassword = null;
        
        if (args.length == 1) {
            password = args[0];
        } else if (args.length == 2) {
            password = args[0];
            confirmPassword = args[1];
        } else {
            plugin.getMessageUtils().sendMessage(player, "register-usage");
            return true;
        }
        
        // Проверка длины пароля
        if (password.length() < plugin.getConfigManager().getMinPasswordLength()) {
            plugin.getMessageUtils().sendMessage(player, "password-too-short", 
                String.valueOf(plugin.getConfigManager().getMinPasswordLength()));
            return true;
        }
        
        if (password.length() > plugin.getConfigManager().getMaxPasswordLength()) {
            plugin.getMessageUtils().sendMessage(player, "password-too-long",
                String.valueOf(plugin.getConfigManager().getMaxPasswordLength()));
            return true;
        }
        
        // Проверка подтверждения пароля
        if (confirmPassword != null && !password.equals(confirmPassword)) {
            plugin.getMessageUtils().sendMessage(player, "passwords-dont-match");
            return true;
        }
        
        // Проверка безопасности пароля
        if (plugin.getConfigManager().isForceSecurePassword()) {
            PasswordUtils.PasswordStrength strength = PasswordUtils.checkPasswordStrength(password);
            if (!strength.isAcceptable()) {
                player.sendMessage("§cПароль слишком слабый! Сила пароля: " + strength.getDisplayName());
                player.sendMessage(PasswordUtils.getPasswordRequirements());
                return true;
            }
        }
        
        // Регистрация игрока
        plugin.getAuthManager().registerPlayer(player, password);
        return true;
    }
    
    /**
     * Обработка команды выхода
     */
    private boolean handleLogout(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!plugin.getAuthManager().isAuthenticated(player)) {
            plugin.getMessageUtils().sendMessage(player, "not-logged-in");
            return true;
        }
        
        plugin.getAuthManager().logout(player);
        plugin.getMessageUtils().sendMessage(player, "logged-out");
        return true;
    }
    
    /**
     * Обработка команды изменения пароля
     */
    private boolean handleChangePassword(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!plugin.getAuthManager().isAuthenticated(player)) {
            plugin.getMessageUtils().sendMessage(player, "not-logged-in");
            return true;
        }
        
        if (args.length != 2) {
            plugin.getMessageUtils().sendMessage(player, "changepassword-usage");
            return true;
        }
        
        String oldPassword = args[0];
        String newPassword = args[1];
        
        // Проверка старого пароля
        if (!plugin.getDatabaseManager().checkPassword(player.getUniqueId(), oldPassword)) {
            plugin.getMessageUtils().sendMessage(player, "wrong-password");
            plugin.getDatabaseManager().logAction(player.getUniqueId(), player.getName(), 
                "CHANGE_PASSWORD_FAILED", player.getAddress().getAddress().getHostAddress(),
                "Wrong old password");
            return true;
        }
        
        // Проверка нового пароля
        if (newPassword.length() < plugin.getConfigManager().getMinPasswordLength()) {
            plugin.getMessageUtils().sendMessage(player, "password-too-short",
                String.valueOf(plugin.getConfigManager().getMinPasswordLength()));
            return true;
        }
        
        if (newPassword.length() > plugin.getConfigManager().getMaxPasswordLength()) {
            plugin.getMessageUtils().sendMessage(player, "password-too-long",
                String.valueOf(plugin.getConfigManager().getMaxPasswordLength()));
            return true;
        }
        
        // Проверка безопасности нового пароля
        if (plugin.getConfigManager().isForceSecurePassword()) {
            PasswordUtils.PasswordStrength strength = PasswordUtils.checkPasswordStrength(newPassword);
            if (!strength.isAcceptable()) {
                player.sendMessage("§cНовый пароль слишком слабый! Сила пароля: " + strength.getDisplayName());
                player.sendMessage(PasswordUtils.getPasswordRequirements());
                return true;
            }
        }
        
        // Проверка, что новый пароль отличается от старого
        if (oldPassword.equals(newPassword)) {
            plugin.getMessageUtils().sendMessage(player, "same-password");
            return true;
        }
        
        // Изменение пароля
        if (plugin.getDatabaseManager().changePassword(player.getUniqueId(), newPassword)) {
            plugin.getMessageUtils().sendMessage(player, "password-changed");
            plugin.getDatabaseManager().logAction(player.getUniqueId(), player.getName(),
                "CHANGE_PASSWORD", player.getAddress().getAddress().getHostAddress(),
                "Password changed successfully");
            
            // Деактивация всех сессий для безопасности
            plugin.getDatabaseManager().deactivateAllSessions(player.getUniqueId());
        } else {
            plugin.getMessageUtils().sendMessage(player, "password-change-error");
        }
        
        return true;
    }
    
    /**
     * Обработка административных команд
     */
    private boolean handleAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("azureauth.admin")) {
            sender.sendMessage("§cУ вас недостаточно прав для использования этой команды!");
            return true;
        }
        
        if (args.length == 0) {
            showAdminHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                plugin.reloadPlugin();
                sender.sendMessage("§a[AzureAuth] Плагин успешно перезагружен!");
                return true;
                
            case "stats":
                showStats(sender);
                return true;
                
            case "unregister":
                return handleUnregister(sender, args);
                
            case "forcelogin":
                return handleForceLogin(sender, args);
                
            case "resetpassword":
                return handleResetPassword(sender, args);
                
            case "info":
                return handlePlayerInfo(sender, args);
                
            case "cleanup":
                plugin.getDatabaseManager().cleanupExpiredSessions();
                sender.sendMessage("§a[AzureAuth] Очистка устаревших сессий завершена!");
                return true;
                
            default:
                showAdminHelp(sender);
                return true;
        }
    }
    
    /**
     * Показ помощи по административным командам
     */
    private void showAdminHelp(CommandSender sender) {
        sender.sendMessage("§6=== AzureAuth Административные команды ===");
        sender.sendMessage("§e/azureauth reload §7- Перезагрузить плагин");
        sender.sendMessage("§e/azureauth stats §7- Показать статистику");
        sender.sendMessage("§e/azureauth info <игрок> §7- Информация об игроке");
        sender.sendMessage("§e/azureauth unregister <игрок> §7- Удалить регистрацию");
        sender.sendMessage("§e/azureauth forcelogin <игрок> §7- Принудительный вход");
        sender.sendMessage("§e/azureauth resetpassword <игрок> §7- Сбросить пароль");
        sender.sendMessage("§e/azureauth cleanup §7- Очистить устаревшие сессии");
    }
    
    /**
     * Показ статистики
     */
    private void showStats(CommandSender sender) {
        sender.sendMessage("§6=== Статистика AzureAuth ===");
        sender.sendMessage("§7Версия плагина: §f" + plugin.getDescription().getVersion());
        sender.sendMessage("§7База данных: §f" + plugin.getConfigManager().getDatabaseType());
        sender.sendMessage("§7Соединение с БД: " + 
            (plugin.getDatabaseManager().isConnectionValid() ? "§aАктивно" : "§cНеактивно"));
        sender.sendMessage("§7Авторизованных игроков: §f" + plugin.getAuthManager().getAuthenticatedCount());
        sender.sendMessage("§7Регистрация: " + 
            (plugin.getConfigManager().isRegistrationEnabled() ? "§aВключена" : "§cОтключена"));
        sender.sendMessage("§7Сессии: " + 
            (plugin.getConfigManager().isSessionEnabled() ? "§aВключены" : "§cОтключены"));
    }
    
    /**
     * Принудительное удаление регистрации
     */
    private boolean handleUnregister(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("§cИспользование: /azureauth unregister <игрок>");
            return true;
        }
        
        // TODO: Реализовать удаление регистрации
        sender.sendMessage("§aФункция будет добавлена в следующих версиях");
        return true;
    }
    
    /**
     * Принудительный вход в игру
     */
    private boolean handleForceLogin(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("§cИспользование: /azureauth forcelogin <игрок>");
            return true;
        }
        
        // TODO: Реализовать принудительный вход
        sender.sendMessage("§aФункция будет добавлена в следующих версиях");
        return true;
    }
    
    /**
     * Сброс пароля игрока
     */
    private boolean handleResetPassword(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("§cИспользование: /azureauth resetpassword <игрок>");
            return true;
        }
        
        // TODO: Реализовать сброс пароля
        sender.sendMessage("§aФункция будет добавлена в следующих версиях");
        return true;
    }
    
    /**
     * Информация об игроке
     */
    private boolean handlePlayerInfo(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("§cИспользование: /azureauth info <игрок>");
            return true;
        }
        
        // TODO: Реализовать показ информации об игроке
        sender.sendMessage("§aФункция будет добавлена в следующих версиях");
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (command.getName().equalsIgnoreCase("azureauth")) {
            if (args.length == 1) {
                if (sender.hasPermission("azureauth.admin")) {
                    completions.addAll(Arrays.asList("reload", "stats", "info", "unregister", 
                        "forcelogin", "resetpassword", "cleanup"));
                }
            }
        }
        
        return completions;
    }
}
