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

    /**
     * Get the default search engine
     */
    public static SearchEngine getDefaultSearchEngine() {
        try {
            File f = new File(CONFIG_FILE);
            if (!f.exists()) return SearchEngine.GOOGLE;
            try (FileReader r = new FileReader(f)) {
                JsonObject obj = GSON.fromJson(r, JsonObject.class);
                if (obj != null && obj.has("defaultSearchEngine")) {
                    String engineName = obj.get("defaultSearchEngine").getAsString();
                    return SearchEngine.fromDisplayName(engineName);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to read default search engine: {}", e.getMessage());
        }
        return SearchEngine.GOOGLE;
    }

    /**
     * Set the default search engine
     */
    public static void setDefaultSearchEngine(SearchEngine engine) {
        try {
            JsonObject obj = new JsonObject();
            File f = new File(CONFIG_FILE);
            if (f.exists()) {
                try (FileReader r = new FileReader(f)) {
                    JsonObject prev = GSON.fromJson(r, JsonObject.class);
                    if (prev != null) obj = prev;
                }
            }
            obj.addProperty("defaultSearchEngine", engine.getDisplayName());
            try (FileWriter w = new FileWriter(f)) {
                GSON.toJson(obj, w);
            }
            logger.info("Default search engine set to: {}", engine.getDisplayName());
        } catch (Exception e) {
            logger.warn("Failed to write default search engine: {}", e.getMessage());
        }
    }

    // ========== General Settings ==========

    /**
     * Get the home page URL
     */
    public static String getHomePage() {
        try {
            File f = new File(CONFIG_FILE);
            if (!f.exists()) return "https://www.google.com";
            try (FileReader r = new FileReader(f)) {
                JsonObject obj = GSON.fromJson(r, JsonObject.class);
                if (obj != null && obj.has("homePage")) {
                    return obj.get("homePage").getAsString();
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to read home page: {}", e.getMessage());
        }
        return "https://www.google.com";
    }

    /**
     * Set the home page URL
     */
    public static void setHomePage(String url) {
        try {
            JsonObject obj = new JsonObject();
            File f = new File(CONFIG_FILE);
            if (f.exists()) {
                try (FileReader r = new FileReader(f)) {
                    JsonObject prev = GSON.fromJson(r, JsonObject.class);
                    if (prev != null) obj = prev;
                }
            }
            obj.addProperty("homePage", url);
            try (FileWriter w = new FileWriter(f)) {
                GSON.toJson(obj, w);
            }
            logger.info("Home page set to: {}", url);
        } catch (Exception e) {
            logger.warn("Failed to write home page: {}", e.getMessage());
        }
    }

    /**
     * Get show bookmarks bar preference
     */
    public static boolean isShowBookmarksBar() {
        try {
            File f = new File(CONFIG_FILE);
            if (!f.exists()) return true;
            try (FileReader r = new FileReader(f)) {
                JsonObject obj = GSON.fromJson(r, JsonObject.class);
                if (obj != null && obj.has("showBookmarksBar")) {
                    return obj.get("showBookmarksBar").getAsBoolean();
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to read show bookmarks bar preference: {}", e.getMessage());
        }
        return true;
    }

    /**
     * Set show bookmarks bar preference
     */
    public static void setShowBookmarksBar(boolean show) {
        try {
            JsonObject obj = new JsonObject();
            File f = new File(CONFIG_FILE);
            if (f.exists()) {
                try (FileReader r = new FileReader(f)) {
                    JsonObject prev = GSON.fromJson(r, JsonObject.class);
                    if (prev != null) obj = prev;
                }
            }
            obj.addProperty("showBookmarksBar", show);
            try (FileWriter w = new FileWriter(f)) {
                GSON.toJson(obj, w);
            }
            logger.info("Show bookmarks bar: {}", show);
        } catch (Exception e) {
            logger.warn("Failed to write show bookmarks bar preference: {}", e.getMessage());
        }
    }

    /**
     * Get restore tabs from last session preference
     */
    public static boolean isRestoreTabsFromLastSession() {
        try {
            File f = new File(CONFIG_FILE);
            if (!f.exists()) return true;
            try (FileReader r = new FileReader(f)) {
                JsonObject obj = GSON.fromJson(r, JsonObject.class);
                if (obj != null && obj.has("restoreTabsFromLastSession")) {
                    return obj.get("restoreTabsFromLastSession").getAsBoolean();
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to read restore tabs preference: {}", e.getMessage());
        }
        return true;
    }

    /**
     * Set restore tabs from last session preference
     */
    public static void setRestoreTabsFromLastSession(boolean restore) {
        try {
            JsonObject obj = new JsonObject();
            File f = new File(CONFIG_FILE);
            if (f.exists()) {
                try (FileReader r = new FileReader(f)) {
                    JsonObject prev = GSON.fromJson(r, JsonObject.class);
                    if (prev != null) obj = prev;
                }
            }
            obj.addProperty("restoreTabsFromLastSession", restore);
            try (FileWriter w = new FileWriter(f)) {
                GSON.toJson(obj, w);
            }
            logger.info("Restore tabs from last session: {}", restore);
        } catch (Exception e) {
            logger.warn("Failed to write restore tabs preference: {}", e.getMessage());
        }
    }

    // ========== Privacy Settings ==========

    /**
     * Get Do Not Track preference
     */
    public static boolean isDoNotTrackEnabled() {
        try {
            File f = new File(CONFIG_FILE);
            if (!f.exists()) return true;
            try (FileReader r = new FileReader(f)) {
                JsonObject obj = GSON.fromJson(r, JsonObject.class);
                if (obj != null && obj.has("doNotTrack")) {
                    return obj.get("doNotTrack").getAsBoolean();
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to read Do Not Track preference: {}", e.getMessage());
        }
        return true;
    }

    /**
     * Set Do Not Track preference
     */
    public static void setDoNotTrackEnabled(boolean enabled) {
        try {
            JsonObject obj = new JsonObject();
            File f = new File(CONFIG_FILE);
            if (f.exists()) {
                try (FileReader r = new FileReader(f)) {
                    JsonObject prev = GSON.fromJson(r, JsonObject.class);
                    if (prev != null) obj = prev;
                }
            }
            obj.addProperty("doNotTrack", enabled);
            try (FileWriter w = new FileWriter(f)) {
                GSON.toJson(obj, w);
            }
            logger.info("Do Not Track enabled: {}", enabled);
        } catch (Exception e) {
            logger.warn("Failed to write Do Not Track preference: {}", e.getMessage());
        }
    }

    /**
     * Get block tracking cookies preference
     */
    public static boolean isBlockTrackingCookies() {
        try {
            File f = new File(CONFIG_FILE);
            if (!f.exists()) return true;
            try (FileReader r = new FileReader(f)) {
                JsonObject obj = GSON.fromJson(r, JsonObject.class);
                if (obj != null && obj.has("blockTrackingCookies")) {
                    return obj.get("blockTrackingCookies").getAsBoolean();
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to read block tracking cookies preference: {}", e.getMessage());
        }
        return true;
    }

    /**
     * Set block tracking cookies preference
     */
    public static void setBlockTrackingCookies(boolean block) {
        try {
            JsonObject obj = new JsonObject();
            File f = new File(CONFIG_FILE);
            if (f.exists()) {
                try (FileReader r = new FileReader(f)) {
                    JsonObject prev = GSON.fromJson(r, JsonObject.class);
                    if (prev != null) obj = prev;
                }
            }
            obj.addProperty("blockTrackingCookies", block);
            try (FileWriter w = new FileWriter(f)) {
                GSON.toJson(obj, w);
            }
            logger.info("Block tracking cookies: {}", block);
        } catch (Exception e) {
            logger.warn("Failed to write block tracking cookies preference: {}", e.getMessage());
        }
    }

    /**
     * Get clear history on exit preference
     */
    public static boolean isClearHistoryOnExit() {
        try {
            File f = new File(CONFIG_FILE);
            if (!f.exists()) return false;
            try (FileReader r = new FileReader(f)) {
                JsonObject obj = GSON.fromJson(r, JsonObject.class);
                if (obj != null && obj.has("clearHistoryOnExit")) {
                    return obj.get("clearHistoryOnExit").getAsBoolean();
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to read clear history on exit preference: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Set clear history on exit preference
     */
    public static void setClearHistoryOnExit(boolean clear) {
        try {
            JsonObject obj = new JsonObject();
            File f = new File(CONFIG_FILE);
            if (f.exists()) {
                try (FileReader r = new FileReader(f)) {
                    JsonObject prev = GSON.fromJson(r, JsonObject.class);
                    if (prev != null) obj = prev;
                }
            }
            obj.addProperty("clearHistoryOnExit", clear);
            try (FileWriter w = new FileWriter(f)) {
                GSON.toJson(obj, w);
            }
            logger.info("Clear history on exit: {}", clear);
        } catch (Exception e) {
            logger.warn("Failed to write clear history on exit preference: {}", e.getMessage());
        }
    }

    // ========== Cookie Settings ==========

    /**
     * Get accept cookies preference
     */
    public static boolean isAcceptCookies() {
        try {
            File f = new File(CONFIG_FILE);
            if (!f.exists()) return true;
            try (FileReader r = new FileReader(f)) {
                JsonObject obj = GSON.fromJson(r, JsonObject.class);
                if (obj != null && obj.has("acceptCookies")) {
                    return obj.get("acceptCookies").getAsBoolean();
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to read accept cookies preference: {}", e.getMessage());
        }
        return true;
    }

    /**
     * Set accept cookies preference
     */
    public static void setAcceptCookies(boolean accept) {
        try {
            JsonObject obj = new JsonObject();
            File f = new File(CONFIG_FILE);
            if (f.exists()) {
                try (FileReader r = new FileReader(f)) {
                    JsonObject prev = GSON.fromJson(r, JsonObject.class);
                    if (prev != null) obj = prev;
                }
            }
            obj.addProperty("acceptCookies", accept);
            try (FileWriter w = new FileWriter(f)) {
                GSON.toJson(obj, w);
            }
            logger.info("Accept cookies: {}", accept);
        } catch (Exception e) {
            logger.warn("Failed to write accept cookies preference: {}", e.getMessage());
        }
    }

    /**
     * Get block third-party cookies preference
     */
    public static boolean isBlockThirdPartyCookies() {
        try {
            File f = new File(CONFIG_FILE);
            if (!f.exists()) return true;
            try (FileReader r = new FileReader(f)) {
                JsonObject obj = GSON.fromJson(r, JsonObject.class);
                if (obj != null && obj.has("blockThirdPartyCookies")) {
                    return obj.get("blockThirdPartyCookies").getAsBoolean();
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to read block third-party cookies preference: {}", e.getMessage());
        }
        return true;
    }

    /**
     * Set block third-party cookies preference
     */
    public static void setBlockThirdPartyCookies(boolean block) {
        try {
            JsonObject obj = new JsonObject();
            File f = new File(CONFIG_FILE);
            if (f.exists()) {
                try (FileReader r = new FileReader(f)) {
                    JsonObject prev = GSON.fromJson(r, JsonObject.class);
                    if (prev != null) obj = prev;
                }
            }
            obj.addProperty("blockThirdPartyCookies", block);
            try (FileWriter w = new FileWriter(f)) {
                GSON.toJson(obj, w);
            }
            logger.info("Block third-party cookies: {}", block);
        } catch (Exception e) {
            logger.warn("Failed to write block third-party cookies preference: {}", e.getMessage());
        }
    }

    /**
     * Get save cookies between sessions preference
     */
    public static boolean isSaveCookiesBetweenSessions() {
        try {
            File f = new File(CONFIG_FILE);
            if (!f.exists()) return true;
            try (FileReader r = new FileReader(f)) {
                JsonObject obj = GSON.fromJson(r, JsonObject.class);
                if (obj != null && obj.has("saveCookiesBetweenSessions")) {
                    return obj.get("saveCookiesBetweenSessions").getAsBoolean();
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to read save cookies between sessions preference: {}", e.getMessage());
        }
        return true;
    }

    /**
     * Set save cookies between sessions preference
     */
    public static void setSaveCookiesBetweenSessions(boolean save) {
        try {
            JsonObject obj = new JsonObject();
            File f = new File(CONFIG_FILE);
            if (f.exists()) {
                try (FileReader r = new FileReader(f)) {
                    JsonObject prev = GSON.fromJson(r, JsonObject.class);
                    if (prev != null) obj = prev;
                }
            }
            obj.addProperty("saveCookiesBetweenSessions", save);
            try (FileWriter w = new FileWriter(f)) {
                GSON.toJson(obj, w);
            }
            logger.info("Save cookies between sessions: {}", save);
        } catch (Exception e) {
            logger.warn("Failed to write save cookies between sessions preference: {}", e.getMessage());
        }
    }
}
