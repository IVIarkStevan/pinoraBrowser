package com.pinora.browser.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages cookies for the browser with support for persistent and session cookies
 */
public class CookieManager {
    
    private static final Logger logger = LoggerFactory.getLogger(CookieManager.class);
    private static final String COOKIES_FILE = com.pinora.browser.util.ConfigManager.getConfigDirectory() + "/cookies.json";
    private static final Gson GSON = new Gson();
    
    private Map<String, CookieData> cookies; // Key: "domain:path:name"
    private Map<String, Boolean> cookiePolicy; // domain -> allow/block
    private boolean blockThirdPartyCookies = true;
    private boolean blockTrackingCookies = true;
    
    public CookieManager() {
        this.cookies = new LinkedHashMap<>();
        this.cookiePolicy = new HashMap<>();
        loadCookies();
        loadCookiePolicy();
    }
    
    /**
     * Add or update a cookie
     */
    public void setCookie(CookieData cookie) {
        if (cookie == null) return;
        
        // Check if domain is allowed
        if (!isDomainAllowed(cookie.getDomain())) {
            logger.debug("Cookie blocked for domain: {}", cookie.getDomain());
            return;
        }
        
        // Check for tracking cookies
        if (blockTrackingCookies && isTrackingCookie(cookie)) {
            logger.debug("Tracking cookie blocked: {}", cookie.getName());
            return;
        }
        
        String key = generateKey(cookie);
        CookieData existing = cookies.get(key);
        
        if (existing != null) {
            existing.setValue(cookie.getValue());
            existing.setLastAccessedTime(System.currentTimeMillis());
            existing.setExpiryTime(cookie.getExpiryTime());
        } else {
            cookies.put(key, cookie);
        }
        
        // Auto-save
        saveCookies();
    }
    
    /**
     * Get a cookie by domain, path, and name
     */
    public CookieData getCookie(String domain, String path, String name) {
        String key = domain + ":" + path + ":" + name;
        CookieData cookie = cookies.get(key);
        
        // Remove if expired
        if (cookie != null && cookie.isExpired()) {
            deleteCookie(domain, path, name);
            return null;
        }
        
        return cookie;
    }
    
    /**
     * Get all cookies for a domain
     */
    public List<CookieData> getCookiesForDomain(String domain) {
        return cookies.values().stream()
            .filter(c -> c.getDomain().equals(domain) || domain.endsWith("." + c.getDomain()))
            .filter(c -> !c.isExpired())
            .collect(Collectors.toList());
    }
    
    /**
     * Get all valid cookies (non-expired)
     */
    public List<CookieData> getAllValidCookies() {
        return cookies.values().stream()
            .filter(c -> !c.isExpired())
            .collect(Collectors.toList());
    }
    
    /**
     * Get all cookies
     */
    public List<CookieData> getAllCookies() {
        return new ArrayList<>(cookies.values());
    }
    
    /**
     * Delete a specific cookie
     */
    public void deleteCookie(String domain, String path, String name) {
        String key = domain + ":" + path + ":" + name;
        cookies.remove(key);
        saveCookies();
    }
    
    /**
     * Delete all cookies for a domain
     */
    public void deleteAllCookiesForDomain(String domain) {
        cookies.entrySet().removeIf(entry -> entry.getValue().getDomain().equals(domain));
        saveCookies();
    }
    
    /**
     * Delete all cookies
     */
    public void deleteAllCookies() {
        cookies.clear();
        saveCookies();
    }
    
    /**
     * Delete all session cookies
     */
    public void deleteSessionCookies() {
        cookies.entrySet().removeIf(entry -> entry.getValue().isSessionOnly());
        saveCookies();
    }
    
    /**
     * Delete expired cookies
     */
    public void deleteExpiredCookies() {
        boolean changed = false;
        List<String> keysToRemove = new ArrayList<>();
        
        for (Map.Entry<String, CookieData> entry : cookies.entrySet()) {
            if (entry.getValue().isExpired()) {
                keysToRemove.add(entry.getKey());
                changed = true;
            }
        }
        
        keysToRemove.forEach(cookies::remove);
        
        if (changed) {
            saveCookies();
        }
    }
    
    /**
     * Set cookie policy for a domain
     */
    public void setDomainPolicy(String domain, boolean allowed) {
        cookiePolicy.put(domain, allowed);
        saveCookiePolicy();
    }
    
    /**
     * Get cookie policy for a domain
     */
    public boolean isDomainAllowed(String domain) {
        // Return explicit policy if set, otherwise default to true
        return cookiePolicy.getOrDefault(domain, true);
    }
    
    /**
     * Get all domain policies
     */
    public Map<String, Boolean> getAllDomainPolicies() {
        return new HashMap<>(cookiePolicy);
    }
    
    /**
     * Set third-party cookie blocking
     */
    public void setBlockThirdPartyCookies(boolean block) {
        this.blockThirdPartyCookies = block;
    }
    
    public boolean isBlockThirdPartyCookies() {
        return blockThirdPartyCookies;
    }
    
    /**
     * Set tracking cookie blocking
     */
    public void setBlockTrackingCookies(boolean block) {
        this.blockTrackingCookies = block;
    }
    
    public boolean isBlockTrackingCookies() {
        return blockTrackingCookies;
    }
    
    /**
     * Export cookies to JSON format
     */
    public String exportCookiesToJSON() {
        JsonArray array = new JsonArray();
        for (CookieData cookie : cookies.values()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", cookie.getName());
            obj.addProperty("value", cookie.getValue());
            obj.addProperty("domain", cookie.getDomain());
            obj.addProperty("path", cookie.getPath());
            obj.addProperty("secure", cookie.isSecure());
            obj.addProperty("httpOnly", cookie.isHttpOnly());
            obj.addProperty("sessionOnly", cookie.isSessionOnly());
            obj.addProperty("expiryTime", cookie.getExpiryTime());
            obj.addProperty("sameSite", cookie.getSameSite());
            array.add(obj);
        }
        return GSON.toJson(array);
    }
    
    /**
     * Import cookies from JSON format
     */
    public int importCookiesFromJSON(String json) throws Exception {
        int importedCount = 0;
        JsonArray array = GSON.fromJson(json, JsonArray.class);
        
        for (JsonElement element : array) {
            JsonObject obj = element.getAsJsonObject();
            CookieData cookie = new CookieData();
            cookie.setName(obj.get("name").getAsString());
            cookie.setValue(obj.get("value").getAsString());
            cookie.setDomain(obj.get("domain").getAsString());
            cookie.setPath(obj.get("path").getAsString());
            cookie.setSecure(obj.get("secure").getAsBoolean());
            cookie.setHttpOnly(obj.get("httpOnly").getAsBoolean());
            cookie.setSessionOnly(obj.get("sessionOnly").getAsBoolean());
            cookie.setExpiryTime(obj.get("expiryTime").getAsLong());
            if (obj.has("sameSite")) {
                cookie.setSameSite(obj.get("sameSite").getAsString());
            }
            
            setCookie(cookie);
            importedCount++;
        }
        
        return importedCount;
    }
    
    /**
     * Get list of unique domains
     */
    public List<String> getAllDomains() {
        return cookies.values().stream()
            .map(CookieData::getDomain)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
    
    /**
     * Get cookie statistics
     */
    public Map<String, Object> getCookieStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCookies", cookies.size());
        stats.put("sessionCookies", cookies.values().stream().filter(CookieData::isSessionOnly).count());
        stats.put("persistentCookies", cookies.values().stream().filter(c -> !c.isSessionOnly()).count());
        stats.put("secureCookies", cookies.values().stream().filter(CookieData::isSecure).count());
        stats.put("httpOnlyCookies", cookies.values().stream().filter(CookieData::isHttpOnly).count());
        stats.put("uniqueDomains", getAllDomains().size());
        return stats;
    }
    
    // Private helper methods
    
    private String generateKey(CookieData cookie) {
        return cookie.getDomain() + ":" + cookie.getPath() + ":" + cookie.getName();
    }
    
    private boolean isTrackingCookie(CookieData cookie) {
        String name = cookie.getName().toLowerCase();
        String value = cookie.getValue().toLowerCase();
        
        // Common tracking cookie patterns
        String[] trackingPatterns = {
            "_ga", "_gid", "track", "analytics", "_utm", "doubleclick",
            "fbp", "_fbp", "fbsb", "fbcsb", "_ym_", "yandex_gid"
        };
        
        for (String pattern : trackingPatterns) {
            if (name.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    private void saveCookies() {
        try {
            Files.createDirectories(Paths.get(com.pinora.browser.util.ConfigManager.getConfigDirectory()));
            try (FileWriter writer = new FileWriter(COOKIES_FILE)) {
                JsonArray array = new JsonArray();
                for (CookieData cookie : cookies.values()) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("name", cookie.getName());
                    obj.addProperty("value", cookie.getValue());
                    obj.addProperty("domain", cookie.getDomain());
                    obj.addProperty("path", cookie.getPath());
                    obj.addProperty("secure", cookie.isSecure());
                    obj.addProperty("httpOnly", cookie.isHttpOnly());
                    obj.addProperty("sessionOnly", cookie.isSessionOnly());
                    obj.addProperty("expiryTime", cookie.getExpiryTime());
                    obj.addProperty("sameSite", cookie.getSameSite());
                    obj.addProperty("createdTime", cookie.getCreatedTime());
                    obj.addProperty("lastAccessedTime", cookie.getLastAccessedTime());
                    array.add(obj);
                }
                GSON.toJson(array, writer);
            }
            logger.info("Cookies saved to {}", COOKIES_FILE);
        } catch (Exception e) {
            logger.error("Failed to save cookies", e);
        }
    }
    
    private void loadCookies() {
        try {
            File file = new File(COOKIES_FILE);
            if (!file.exists()) {
                logger.debug("Cookies file not found, starting with empty cookie store");
                return;
            }
            
            String content = new String(Files.readAllBytes(Paths.get(COOKIES_FILE)));
            JsonArray array = GSON.fromJson(content, JsonArray.class);
            
            if (array != null) {
                for (JsonElement element : array) {
                    JsonObject obj = element.getAsJsonObject();
                    CookieData cookie = new CookieData();
                    cookie.setName(obj.get("name").getAsString());
                    cookie.setValue(obj.get("value").getAsString());
                    cookie.setDomain(obj.get("domain").getAsString());
                    cookie.setPath(obj.get("path").getAsString());
                    cookie.setSecure(obj.get("secure").getAsBoolean());
                    cookie.setHttpOnly(obj.get("httpOnly").getAsBoolean());
                    cookie.setSessionOnly(obj.get("sessionOnly").getAsBoolean());
                    cookie.setExpiryTime(obj.get("expiryTime").getAsLong());
                    if (obj.has("sameSite")) {
                        cookie.setSameSite(obj.get("sameSite").getAsString());
                    }
                    if (obj.has("createdTime")) {
                        cookie.setCreatedTime(obj.get("createdTime").getAsLong());
                    }
                    if (obj.has("lastAccessedTime")) {
                        cookie.setLastAccessedTime(obj.get("lastAccessedTime").getAsLong());
                    }
                    
                    cookies.put(generateKey(cookie), cookie);
                }
            }
            
            logger.info("Loaded {} cookies from disk", cookies.size());
        } catch (Exception e) {
            logger.error("Failed to load cookies", e);
        }
    }
    
    private void saveCookiePolicy() {
        try {
            File file = new File(com.pinora.browser.util.ConfigManager.getConfigDirectory() + "/cookie_policy.json");
            Files.createDirectories(Paths.get(com.pinora.browser.util.ConfigManager.getConfigDirectory()));
            
            try (FileWriter writer = new FileWriter(file)) {
                JsonObject obj = new JsonObject();
                obj.addProperty("blockThirdPartyCookies", blockThirdPartyCookies);
                obj.addProperty("blockTrackingCookies", blockTrackingCookies);
                
                JsonObject policies = new JsonObject();
                for (Map.Entry<String, Boolean> entry : cookiePolicy.entrySet()) {
                    policies.addProperty(entry.getKey(), entry.getValue());
                }
                obj.add("domainPolicies", policies);
                
                GSON.toJson(obj, writer);
            }
        } catch (Exception e) {
            logger.error("Failed to save cookie policy", e);
        }
    }
    
    private void loadCookiePolicy() {
        try {
            File file = new File(com.pinora.browser.util.ConfigManager.getConfigDirectory() + "/cookie_policy.json");
            if (!file.exists()) {
                return;
            }
            
            String content = new String(Files.readAllBytes(Paths.get(file.getPath())));
            JsonObject obj = GSON.fromJson(content, JsonObject.class);
            
            if (obj != null) {
                this.blockThirdPartyCookies = obj.get("blockThirdPartyCookies").getAsBoolean();
                this.blockTrackingCookies = obj.get("blockTrackingCookies").getAsBoolean();
                
                if (obj.has("domainPolicies")) {
                    JsonObject policies = obj.getAsJsonObject("domainPolicies");
                    for (String key : policies.keySet()) {
                        cookiePolicy.put(key, policies.get(key).getAsBoolean());
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to load cookie policy", e);
        }
    }
}
