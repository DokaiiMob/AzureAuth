package net.azuremyst.auth.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Утилиты для работы с паролями
 */
public class PasswordUtils {
    
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String SALT_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    
    // Регулярные выражения для проверки безопасности пароля
    private static final Pattern LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern DIGITS = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL_CHARS = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    
    /**
     * Генерация соли для хеширования пароля
     */
    public static String generateSalt() {
        StringBuilder salt = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            salt.append(SALT_CHARS.charAt(RANDOM.nextInt(SALT_CHARS.length())));
        }
        return salt.toString();
    }
    
    /**
     * Хеширование пароля с солью
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedPassword = password + salt;
            byte[] hashedBytes = digest.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка хеширования пароля", e);
        }
    }
    
    /**
     * Проверка безопасности пароля
     */
    public static PasswordStrength checkPasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return PasswordStrength.VERY_WEAK;
        }
        
        int score = 0;
        
        // Длина пароля
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        
        // Содержит строчные буквы
        if (LOWERCASE.matcher(password).matches()) score++;
        
        // Содержит заглавные буквы
        if (UPPERCASE.matcher(password).matches()) score++;
        
        // Содержит цифры
        if (DIGITS.matcher(password).matches()) score++;
        
        // Содержит специальные символы
        if (SPECIAL_CHARS.matcher(password).matches()) score++;
        
        // Не содержит общих паттернов
        if (!containsCommonPatterns(password)) score++;
        
        return PasswordStrength.fromScore(score);
    }
    
    /**
     * Проверка на наличие общих небезопасных паттернов
     */
    private static boolean containsCommonPatterns(String password) {
        String lowerPassword = password.toLowerCase();
        
        // Список общих небезопасных паролей
        String[] commonPasswords = {
            "password", "123456", "qwerty", "admin", "root", "user",
            "minecraft", "123123", "111111", "000000", "password123",
            "azuremyst", "server", "game", "player"
        };
        
        for (String common : commonPasswords) {
            if (lowerPassword.contains(common)) {
                return true;
            }
        }
        
        // Проверка на последовательности
        if (lowerPassword.matches(".*123.*") || 
            lowerPassword.matches(".*abc.*") || 
            lowerPassword.matches(".*qwe.*")) {
            return true;
        }
        
        // Проверка на повторяющиеся символы
        if (password.matches(".*(.)\\1{2,}.*")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Генерация безопасного пароля
     */
    public static String generateSecurePassword(int length) {
        if (length < 8) length = 8;
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        
        // Гарантируем наличие всех типов символов
        password.append(chars.charAt(RANDOM.nextInt(26))); // Заглавная буква
        password.append(chars.charAt(26 + RANDOM.nextInt(26))); // Строчная буква
        password.append(chars.charAt(52 + RANDOM.nextInt(10))); // Цифра
        password.append(chars.charAt(62 + RANDOM.nextInt(8))); // Специальный символ
        
        // Заполняем остальную часть случайными символами
        for (int i = 4; i < length; i++) {
            password.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        
        // Перемешиваем символы
        return shuffleString(password.toString());
    }
    
    /**
     * Перемешивание строки
     */
    private static String shuffleString(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
    
    /**
     * Проверка на простые пароли
     */
    public static boolean isWeakPassword(String password) {
        return checkPasswordStrength(password).ordinal() < PasswordStrength.MEDIUM.ordinal();
    }
    
    /**
     * Получение сообщения о требованиях к паролю
     */
    public static String getPasswordRequirements() {
        return "§6Требования к паролю:\n" +
               "§7• Минимум 6 символов (рекомендуется 8+)\n" +
               "§7• Содержит буквы в разных регистрах\n" +
               "§7• Содержит цифры\n" +
               "§7• Содержит специальные символы (!@#$%^&*)\n" +
               "§7• Не содержит общих слов или последовательностей";
    }
    
    /**
     * Генерация токена сессии
     */
    public static String generateSessionToken() {
        byte[] tokenBytes = new byte[32];
        RANDOM.nextBytes(tokenBytes);
        return Base64.getEncoder().encodeToString(tokenBytes);
    }
    
    /**
     * Генерация кода капчи
     */
    public static String generateCaptcha() {
        StringBuilder captcha = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        
        for (int i = 0; i < 6; i++) {
            captcha.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        
        return captcha.toString();
    }
    
    /**
     * Enum для оценки силы пароля
     */
    public enum PasswordStrength {
        VERY_WEAK("§cОчень слабый", 0),
        WEAK("§eСлабый", 1),
        MEDIUM("§6Средний", 2),
        STRONG("§aСильный", 3),
        VERY_STRONG("§2Очень сильный", 4);
        
        private final String displayName;
        private final int minScore;
        
        PasswordStrength(String displayName, int minScore) {
            this.displayName = displayName;
            this.minScore = minScore;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static PasswordStrength fromScore(int score) {
            if (score >= 6) return VERY_STRONG;
            if (score >= 5) return STRONG;
            if (score >= 3) return MEDIUM;
            if (score >= 2) return WEAK;
            return VERY_WEAK;
        }
        
        public boolean isAcceptable() {
            return this.ordinal() >= MEDIUM.ordinal();
        }
    }
}
