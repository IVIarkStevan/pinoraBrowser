package com.pinora.browser.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages browser session persistence - saving and restoring open tabs
 */
public class SessionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    
    private static final String SESSION_FILE = ConfigManager.getConfigDirectory() + "/session.json";
    private static final Gson GSON = new Gson();
    
    /**
     * Represents a single tab in the session
     */
    public static class TabSession {
        private String url;
        private String title;
        
        public TabSession(String url, String title) {
            this.url = url != null ? url : "";
            this.title = title != null ? title : "";
        }
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url != null ? url : "";
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title != null ? title : "";
        }
    }
    
    /**
     * Save the current session (list of open tabs) to disk
     * 
     * @param tabs List of TabSession objects representing open tabs
     */
    public static void saveSession(List<TabSession> tabs) {
        if (tabs == null || tabs.isEmpty()) {
            logger.debug("No tabs to save, skipping session save");
            return;
        }
        
        try {
            JsonObject sessionObj = new JsonObject();
            JsonArray tabsArray = new JsonArray();
            
            for (TabSession tab : tabs) {
                // Only save if URL is not empty/null
                if (tab.getUrl() != null && !tab.getUrl().isEmpty()) {
                    JsonObject tabObj = new JsonObject();
                    tabObj.addProperty("url", tab.getUrl());
                    tabObj.addProperty("title", tab.getTitle() != null ? tab.getTitle() : "");
                    tabsArray.add(tabObj);
                }
            }
            
            sessionObj.add("tabs", tabsArray);
            sessionObj.addProperty("timestamp", System.currentTimeMillis());
            
            // Write to file
            try (FileWriter writer = new FileWriter(SESSION_FILE)) {
                GSON.toJson(sessionObj, writer);
            }
            
            logger.info("Session saved with {} tabs", tabs.size());
        } catch (Exception e) {
            logger.warn("Failed to save session: {}", e.getMessage());
        }
    }
    
    /**
     * Load the saved session from disk
     * 
     * @return List of TabSession objects, or empty list if no session exists
     */
    public static List<TabSession> loadSession() {
        List<TabSession> tabs = new ArrayList<>();
        
        try {
            File sessionFile = new File(SESSION_FILE);
            if (!sessionFile.exists()) {
                logger.debug("Session file does not exist");
                return tabs;
            }
            
            try (FileReader reader = new FileReader(sessionFile)) {
                JsonObject sessionObj = GSON.fromJson(reader, JsonObject.class);
                
                if (sessionObj != null && sessionObj.has("tabs")) {
                    JsonArray tabsArray = sessionObj.getAsJsonArray("tabs");
                    
                    for (int i = 0; i < tabsArray.size(); i++) {
                        JsonObject tabObj = tabsArray.get(i).getAsJsonObject();
                        String url = tabObj.has("url") ? tabObj.get("url").getAsString() : "";
                        String title = tabObj.has("title") ? tabObj.get("title").getAsString() : "";
                        
                        if (!url.isEmpty()) {
                            tabs.add(new TabSession(url, title));
                        }
                    }
                    
                    logger.info("Session loaded with {} tabs", tabs.size());
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to load session: {}", e.getMessage());
        }
        
        return tabs;
    }
    
    /**
     * Clear the saved session from disk
     */
    public static void clearSession() {
        try {
            File sessionFile = new File(SESSION_FILE);
            if (sessionFile.exists() && sessionFile.delete()) {
                logger.info("Session cleared");
            }
        } catch (Exception e) {
            logger.warn("Failed to clear session: {}", e.getMessage());
        }
    }
    
    /**
     * Check if restore tabs preference is enabled
     * 
     * @return true if user has enabled tab restoration in preferences
     */
    public static boolean isRestoreEnabled() {
        return ConfigManager.isRestoreTabsFromLastSession();
    }
}
