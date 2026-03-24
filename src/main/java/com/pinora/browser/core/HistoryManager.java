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
        // Validate input
        if (url == null || url.trim().isEmpty()) {
            logger.warn("Cannot add empty URL to history");
            return;
        }
        
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
     * Get history entries for a specific date
     */
    public List<HistoryEntry> getHistoryForDate(LocalDateTime date) {
        return history.stream()
            .filter(e -> e.timestamp.toLocalDate().equals(date.toLocalDate()))
            .toList();
    }
    
    /**
     * Get history entries from last N days
     */
    public List<HistoryEntry> getHistoryLastNDays(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        return history.stream()
            .filter(e -> e.timestamp.isAfter(cutoff))
            .toList();
    }
    
    /**
     * Get most visited URLs
     */
    public List<String> getMostVisited(int limit) {
        return history.stream()
            .map(e -> e.url)
            .distinct()
            .limit(limit)
            .toList();
    }
    
    /**
     * Export history to HTML file
     */
    public void exportToHTML(String filePath) {
        try {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html>\n");
            html.append("<head>\n");
            html.append("<title>Pinora Browser History</title>\n");
            html.append("<meta charset=\"utf-8\" />\n");
            html.append("<style>\n");
            html.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
            html.append("h1 { color: #333; }\n");
            html.append("ul { list-style-type: none; padding: 0; }\n");
            html.append("li { margin: 10px 0; padding: 8px; border-left: 4px solid #007bff; padding-left: 12px; }\n");
            html.append("a { color: #007bff; text-decoration: none; }\n");
            html.append("a:hover { text-decoration: underline; }\n");
            html.append(".timestamp { color: #666; font-size: 0.9em; margin-left: 10px; }\n");
            html.append("</style>\n");
            html.append("</head>\n");
            html.append("<body>\n");
            html.append("<h1>Pinora Browser Browsing History</h1>\n");
            html.append(String.format("<p>Total entries: %d</p>\n", history.size()));
            html.append("<ul>\n");
            
            for (HistoryEntry entry : history) {
                html.append(String.format("<li><a href=\"%s\">%s</a><span class=\"timestamp\">%s</span></li>\n",
                    entry.url, entry.title != null ? entry.title : entry.url, entry.timestamp));
            }
            
            html.append("</ul>\n");
            html.append("</body>\n");
            html.append("</html>\n");
            
            Files.write(Paths.get(filePath), html.toString().getBytes());
            logger.info("History exported to {}", filePath);
        } catch (Exception e) {
            logger.error("Failed to export history to HTML", e);
        }
    }
    
    /**
     * Get history statistics
     */
    public String getStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Total History Entries: %d\n", history.size()));
        sb.append(String.format("Max History Items: %d\n", MAX_HISTORY_ITEMS));
        
        if (!history.isEmpty()) {
            // Get unique domains
            int uniqueUrls = (int) history.stream()
                .map(e -> e.url)
                .distinct()
                .count();
            sb.append(String.format("Unique URLs: %d\n", uniqueUrls));
            
            // Get most recent and oldest
            LocalDateTime newest = history.get(0).timestamp;
            LocalDateTime oldest = history.get(history.size() - 1).timestamp;
            
            sb.append(String.format("Most Recent: %s\n", newest));
            sb.append(String.format("Oldest: %s\n", oldest));
        }
        
        return sb.toString();
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
