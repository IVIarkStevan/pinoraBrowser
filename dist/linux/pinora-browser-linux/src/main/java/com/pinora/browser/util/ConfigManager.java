package com.pinora.browser.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Configuration manager for Pinora Browser
 */
public class ConfigManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.pinora";
    private static final String CONFIG_FILE = CONFIG_DIR + "/config.json";
    
    static {
        initConfigDirectory();
    }

    private static final Gson GSON = new Gson();
    
    private static void initConfigDirectory() {
        try {
            File configDir = new File(CONFIG_DIR);
            if (!configDir.exists()) {
                configDir.mkdirs();
                logger.info("Config directory created: {}", CONFIG_DIR);
            }
        } catch (Exception e) {
            logger.error("Failed to create config directory", e);
        }
    }
    
    public static String getConfigDirectory() {
        return CONFIG_DIR;
    }
    
    public static String getHistoryFile() {
        return CONFIG_DIR + "/history.json";
    }
    
    public static String getBookmarksFile() {
        return CONFIG_DIR + "/bookmarks.json";
    }
    
    public static String getCacheDirectory() {
        return CONFIG_DIR + "/cache";
    }
    
    public static String getDownloadsDirectory() {
        String defaultDownloads = System.getProperty("user.home") + "/Downloads";
        return defaultDownloads;
    }

    public static boolean isDarkModeEnabled() {
        try {
            File f = new File(CONFIG_FILE);
            if (!f.exists()) return false;
            try (FileReader r = new FileReader(f)) {
                JsonObject obj = GSON.fromJson(r, JsonObject.class);
                if (obj != null && obj.has("darkMode")) {
                    return obj.get("darkMode").getAsBoolean();
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to read config: {}", e.getMessage());
        }
        return false;
    }

    public static void setDarkModeEnabled(boolean enabled) {
        try {
            JsonObject obj = new JsonObject();
            File f = new File(CONFIG_FILE);
            if (f.exists()) {
                try (FileReader r = new FileReader(f)) {
                    JsonObject prev = GSON.fromJson(r, JsonObject.class);
                    if (prev != null) obj = prev;
                }
            }
            obj.addProperty("darkMode", enabled);
            try (FileWriter w = new FileWriter(f)) {
                GSON.toJson(obj, w);
            }
        } catch (Exception e) {
            logger.warn("Failed to write config: {}", e.getMessage());
        }
    }
}
