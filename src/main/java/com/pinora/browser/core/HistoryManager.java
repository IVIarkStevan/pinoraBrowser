package com.pinora.browser.core;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages browsing history with persistence to disk
 */
public class HistoryManager {
    
    private static final Logger logger = LoggerFactory.getLogger(HistoryManager.class);
    
    private static final String HISTORY_FILE = com.pinora.browser.util.ConfigManager.getHistoryFile();
    private static final Gson GSON = new Gson();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    private List<HistoryEntry> history;
    private static final int MAX_HISTORY_ITEMS = 1000;
    
    public HistoryManager() {
        this.history = new ArrayList<>();
        loadHistory();
    }
    
    public void addToHistory(String url) {
        HistoryEntry entry = new HistoryEntry(url, LocalDateTime.now());
        history.addFirst(entry);
        
        // Remove duplicates (keep most recent)
        history.removeIf(e -> e.url.equals(url) && e != entry);
        
        if (history.size() > MAX_HISTORY_ITEMS) {
            history.removeLast();
        }
        
        // Save to disk
        saveHistory();
        
        logger.debug("Added to history: {}", url);
    }
    
    /**
     * Get all history entries
     */
    public List<HistoryEntry> getHistory() {
        return new ArrayList<>(history);
    }
    
    /**
     * Get history for a specific domain
     */
    public List<HistoryEntry> getHistoryForDomain(String domain) {
        return history.stream()
            .filter(e -> e.url.contains(domain))
            .toList();
    }
    
    /**
     * Search history by URL or title
     */
    public List<HistoryEntry> searchHistory(String query) {
        if (query == null || query.isEmpty()) {
            return new ArrayList<>();
        }
        String lowerQuery = query.toLowerCase();
        return history.stream()
            .filter(e -> e.url.toLowerCase().contains(lowerQuery) || 
                        (e.title != null && e.title.toLowerCase().contains(lowerQuery)))
            .toList();
    }
    
    /**
     * Clear all history
     */
    public void clearHistory() {
        history.clear();
        saveHistory();
        logger.info("History cleared");
    }
    
    /**
     * Delete a specific history entry
     */
    public void deleteEntry(String url) {
        history.removeIf(e -> e.url.equals(url));
        saveHistory();
    }
    
    /**
     * Get history count
     */
    public int getHistoryCount() {
        return history.size();
    }
    
    /**
     * Export history to JSON
     */
    public String exportToJSON() {
        JsonArray array = new JsonArray();
        for (HistoryEntry entry : history) {
            JsonObject obj = new JsonObject();
            obj.addProperty("url", entry.url);
            obj.addProperty("timestamp", entry.timestamp.format(FORMATTER));
            if (entry.title != null) {
                obj.addProperty("title", entry.title);
            }
            array.add(obj);
        }
        return GSON.toJson(array);
    }
    
    /**
     * Import history from JSON
     */
    public int importFromJSON(String json) {
        int imported = 0;
        JsonArray array = GSON.fromJson(json, JsonArray.class);
        
        for (JsonElement element : array) {
            JsonObject obj = element.getAsJsonObject();
            String url = obj.get("url").getAsString();
            String timestampStr = obj.get("timestamp").getAsString();
            LocalDateTime timestamp = LocalDateTime.parse(timestampStr, FORMATTER);
            
            HistoryEntry entry = new HistoryEntry(url, timestamp);
            if (obj.has("title")) {
                entry.title = obj.get("title").getAsString();
            }
            
            history.add(entry);
            imported++;
        }
        
        // Trim to max size
        while (history.size() > MAX_HISTORY_ITEMS) {
            history.removeLast();
        }
        
        saveHistory();
        return imported;
    }
    
    /**
     * Save history to disk
     */
    private void saveHistory() {
        try {
            Files.createDirectories(Paths.get(com.pinora.browser.util.ConfigManager.getConfigDirectory()));
            try (FileWriter writer = new FileWriter(HISTORY_FILE)) {
                JsonArray array = new JsonArray();
                for (HistoryEntry entry : history) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("url", entry.url);
                    obj.addProperty("timestamp", entry.timestamp.format(FORMATTER));
                    if (entry.title != null) {
                        obj.addProperty("title", entry.title);
                    }
                    array.add(obj);
                }
                GSON.toJson(array, writer);
            }
            logger.debug("History saved to {}", HISTORY_FILE);
        } catch (Exception e) {
            logger.error("Failed to save history", e);
        }
    }
    
    /**
     * Load history from disk
     */
    private void loadHistory() {
        try {
            File file = new File(HISTORY_FILE);
            if (!file.exists()) {
                logger.debug("History file not found, starting with empty history");
                return;
            }
            
            try (FileReader reader = new FileReader(file)) {
                JsonArray array = GSON.fromJson(reader, JsonArray.class);
                
                if (array != null) {
                    for (JsonElement element : array) {
                        JsonObject obj = element.getAsJsonObject();
                        String url = obj.get("url").getAsString();
                        String timestampStr = obj.get("timestamp").getAsString();
                        LocalDateTime timestamp = LocalDateTime.parse(timestampStr, FORMATTER);
                        
                        HistoryEntry entry = new HistoryEntry(url, timestamp);
                        if (obj.has("title")) {
                            entry.title = obj.get("title").getAsString();
                        }
                        
                        history.add(entry);
                    }
                }
            }
            
            logger.info("Loaded {} history entries from disk", history.size());
        } catch (Exception e) {
            logger.error("Failed to load history", e);
        }
    }
    
    /**
     * History entry class
     */
    public static class HistoryEntry {
        public String url;
        public LocalDateTime timestamp;
        public String title;
        
        public HistoryEntry(String url, LocalDateTime timestamp) {
            this.url = url;
            this.timestamp = timestamp;
        }
        
        public String getUrl() {
            return url;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
    }
}
