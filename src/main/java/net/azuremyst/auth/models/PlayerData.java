package net.azuremyst.auth.models;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Модель данных игрока для плагина AzureAuth
 */
public class PlayerData {
    
    private UUID uuid;
    private String username;
    private String email;
    private String ipAddress;
    private Timestamp registrationDate;
    private Timestamp lastLogin;
    private boolean isVerified;
    private int failedAttempts;
    private Timestamp lockedUntil;
    
    public PlayerData() {}
    
    public PlayerData(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }
    
    // Геттеры и сеттеры
    public UUID getUuid() {
        return uuid;
    }
    
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public Timestamp getRegistrationDate() {
        return registrationDate;
    }
    
    public void setRegistrationDate(Timestamp registrationDate) {
        this.registrationDate = registrationDate;
    }
    
    public Timestamp getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public boolean isVerified() {
        return isVerified;
    }
    
    public void setVerified(boolean verified) {
        isVerified = verified;
    }
    
    public int getFailedAttempts() {
        return failedAttempts;
    }
    
    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }
    
    public Timestamp getLockedUntil() {
        return lockedUntil;
    }
    
    public void setLockedUntil(Timestamp lockedUntil) {
        this.lockedUntil = lockedUntil;
    }
    
    /**
     * Проверка блокировки аккаунта
     */
    public boolean isLocked() {
        if (lockedUntil == null) return false;
        return lockedUntil.getTime() > System.currentTimeMillis();
    }
    
    /**
     * Время до разблокировки в секундах
     */
    public long getTimeUntilUnlock() {
        if (!isLocked()) return 0;
        return (lockedUntil.getTime() - System.currentTimeMillis()) / 1000;
    }
    
    @Override
    public String toString() {
        return "PlayerData{" +
                "uuid=" + uuid +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", registrationDate=" + registrationDate +
                ", lastLogin=" + lastLogin +
                ", isVerified=" + isVerified +
                ", failedAttempts=" + failedAttempts +
                ", lockedUntil=" + lockedUntil +
                '}';
    }
}
