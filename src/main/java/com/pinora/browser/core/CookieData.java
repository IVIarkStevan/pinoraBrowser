package com.pinora.browser.core;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single cookie with all its properties
 */
public class CookieData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String value;
    private String domain;
    private String path;
    private boolean secure;
    private boolean httpOnly;
    private boolean sessionOnly;
    private long expiryTime; // milliseconds since epoch, 0 for session cookies
    private String sameSite; // "Strict", "Lax", "None", or null
    private long createdTime;
    private long lastAccessedTime;
    
    public CookieData() {
        this.createdTime = System.currentTimeMillis();
        this.lastAccessedTime = System.currentTimeMillis();
        this.sameSite = "Lax"; // Default
    }
    
    public CookieData(String name, String value, String domain, String path) {
        this();
        this.name = name;
        this.value = value;
        this.domain = domain;
        this.path = path;
        this.sessionOnly = true;
    }
    
    // Getters
    public String getName() { return name; }
    public String getValue() { return value; }
    public String getDomain() { return domain; }
    public String getPath() { return path; }
    public boolean isSecure() { return secure; }
    public boolean isHttpOnly() { return httpOnly; }
    public boolean isSessionOnly() { return sessionOnly; }
    public long getExpiryTime() { return expiryTime; }
    public String getSameSite() { return sameSite; }
    public long getCreatedTime() { return createdTime; }
    public long getLastAccessedTime() { return lastAccessedTime; }
    
    // Setters
    public void setName(String name) { this.name = name; }
    public void setValue(String value) { this.value = value; }
    public void setDomain(String domain) { this.domain = domain; }
    public void setPath(String path) { this.path = path; }
    public void setSecure(boolean secure) { this.secure = secure; }
    public void setHttpOnly(boolean httpOnly) { this.httpOnly = httpOnly; }
    public void setSessionOnly(boolean sessionOnly) { this.sessionOnly = sessionOnly; }
    public void setExpiryTime(long expiryTime) { this.expiryTime = expiryTime; }
    public void setSameSite(String sameSite) { this.sameSite = sameSite; }
    public void setLastAccessedTime(long lastAccessedTime) { this.lastAccessedTime = lastAccessedTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }
    
    public boolean isExpired() {
        if (sessionOnly || expiryTime == 0) return false;
        return System.currentTimeMillis() > expiryTime;
    }
    
    public String getExpiryTimeString() {
        if (sessionOnly || expiryTime == 0) return "Session";
        return new java.util.Date(expiryTime).toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CookieData that = (CookieData) o;
        return name.equals(that.name) && domain.equals(that.domain) && path.equals(that.path);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, domain, path);
    }
    
    @Override
    public String toString() {
        return name + "=" + value + "; Domain=" + domain + "; Path=" + path;
    }
}
