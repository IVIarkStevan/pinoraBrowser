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
 * Manages bookmarks with persistence to disk
 */
public class BookmarkManager {
    
    private static final Logger logger = LoggerFactory.getLogger(BookmarkManager.class);
    
    private static final String BOOKMARKS_FILE = com.pinora.browser.util.ConfigManager.getBookmarksFile();
    private static final Gson GSON = new Gson();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    private List<Bookmark> bookmarks;
    
    public BookmarkManager() {
        this.bookmarks = new ArrayList<>();
        loadBookmarks();
    }
    
    public void addBookmark(String title, String url) {
        // Validate input
        if (title == null || title.trim().isEmpty()) {
            logger.warn("Cannot add bookmark with empty title");
            return;
        }
        if (url == null || url.trim().isEmpty()) {
            logger.warn("Cannot add bookmark with empty URL");
            return;
        }
        
        // Prevent duplicates
        if (isBookmarked(url)) {
            logger.debug("URL already bookmarked: {}", url);
            return;
        }
        
        Bookmark bookmark = new Bookmark(title, url, LocalDateTime.now());
        bookmarks.add(bookmark);
        
        // Save to disk
        saveBookmarks();
        
        logger.info("Bookmark added: {} -> {}", title, url);
    }
    
    public void removeBookmark(String url) {
        if (!isBookmarked(url)) {
            logger.warn("Bookmark not found: {}", url);
            return;
        }
        
        bookmarks.removeIf(b -> b.url.equals(url));
        
        // Save to disk
        saveBookmarks();
        
        logger.info("Bookmark removed: {}", url);
    }
    
    public List<Bookmark> getBookmarks() {
        return new ArrayList<>(bookmarks);
    }
    
    /**
     * Check if URL is already bookmarked
     */
    public boolean isBookmarked(String url) {
        return bookmarks.stream().anyMatch(b -> b.url.equals(url));
    }
    
    /**
     * Search bookmarks by title or URL
     */
    public List<Bookmark> searchBookmarks(String query) {
        if (query == null || query.isEmpty()) {
            return new ArrayList<>();
        }
        String lowerQuery = query.toLowerCase();
        return bookmarks.stream()
            .filter(b -> b.title.toLowerCase().contains(lowerQuery) || 
                        b.url.toLowerCase().contains(lowerQuery))
            .toList();
    }
    
    /**
     * Clear all bookmarks
     */
    public void clearBookmarks() {
        bookmarks.clear();
        saveBookmarks();
        logger.info("All bookmarks cleared");
    }
    
    /**
     * Save bookmarks to disk in JSON format
     */
    private void saveBookmarks() {
        try {
            Files.createDirectories(Paths.get(com.pinora.browser.util.ConfigManager.getConfigDirectory()));
            try (FileWriter writer = new FileWriter(BOOKMARKS_FILE)) {
                JsonArray array = new JsonArray();
                for (Bookmark bookmark : bookmarks) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("title", bookmark.title);
                    obj.addProperty("url", bookmark.url);
                    obj.addProperty("timestamp", bookmark.timestamp.format(FORMATTER));
                    array.add(obj);
                }
                GSON.toJson(array, writer);
            }
            logger.debug("Bookmarks saved to {}", BOOKMARKS_FILE);
        } catch (Exception e) {
            logger.error("Failed to save bookmarks", e);
        }
    }
    
    /**
     * Load bookmarks from disk
     */
    private void loadBookmarks() {
        try {
            File file = new File(BOOKMARKS_FILE);
            if (!file.exists()) {
                logger.debug("Bookmarks file not found, starting with empty bookmarks");
                return;
            }
            
            try (FileReader reader = new FileReader(file)) {
                JsonArray array = GSON.fromJson(reader, JsonArray.class);
                
                if (array != null) {
                    for (JsonElement element : array) {
                        JsonObject obj = element.getAsJsonObject();
                        String title = obj.get("title").getAsString();
                        String url = obj.get("url").getAsString();
                        
                        LocalDateTime timestamp = LocalDateTime.now();
                        if (obj.has("timestamp")) {
                            try {
                                String timestampStr = obj.get("timestamp").getAsString();
                                timestamp = LocalDateTime.parse(timestampStr, FORMATTER);
                            } catch (Exception e) {
                                logger.debug("Failed to parse timestamp: {}", e.getMessage());
                            }
                        }
                        
                        Bookmark bookmark = new Bookmark(title, url, timestamp);
                        bookmarks.add(bookmark);
                    }
                }
            }
            
            logger.info("Loaded {} bookmarks from disk", bookmarks.size());
        } catch (Exception e) {
            logger.error("Failed to load bookmarks", e);
        }
    }
    
    /**
     * Export bookmarks to HTML file
     */
    public void exportToHTML(String filePath) {
        try {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html>\n");
            html.append("<head>\n");
            html.append("<title>Pinora Browser Bookmarks</title>\n");
            html.append("<meta charset=\"utf-8\" />\n");
            html.append("</head>\n");
            html.append("<body>\n");
            html.append("<h1>Pinora Browser Bookmarks</h1>\n");
            html.append("<ul>\n");
            
            for (Bookmark bookmark : bookmarks) {
                html.append(String.format("<li><a href=\"%s\">%s</a> (Added: %s)</li>\n",
                    bookmark.url, bookmark.title, bookmark.timestamp));
            }
            
            html.append("</ul>\n");
            html.append("</body>\n");
            html.append("</html>\n");
            
            Files.write(Paths.get(filePath), html.toString().getBytes());
            logger.info("Bookmarks exported to {}", filePath);
        } catch (Exception e) {
            logger.error("Failed to export bookmarks to HTML", e);
        }
    }
    
    /**
     * Export bookmarks to JSON
     */
    public String exportToJSON() {
        try {
            JsonArray array = new JsonArray();
            for (Bookmark bookmark : bookmarks) {
                JsonObject obj = new JsonObject();
                obj.addProperty("title", bookmark.title);
                obj.addProperty("url", bookmark.url);
                obj.addProperty("timestamp", bookmark.timestamp.format(FORMATTER));
                array.add(obj);
            }
            return GSON.toJson(array);
        } catch (Exception e) {
            logger.error("Failed to export bookmarks to JSON", e);
            return "[]";
        }
    }
    
    /**
     * Import bookmarks from JSON
     */
    public void importFromJSON(String json) {
        try {
            JsonArray array = GSON.fromJson(json, JsonArray.class);
            if (array != null) {
                for (JsonElement element : array) {
                    JsonObject obj = element.getAsJsonObject();
                    String title = obj.get("title").getAsString();
                    String url = obj.get("url").getAsString();
                    
                    // Don't add duplicates
                    if (!isBookmarked(url)) {
                        LocalDateTime timestamp = LocalDateTime.now();
                        if (obj.has("timestamp")) {
                            try {
                                timestamp = LocalDateTime.parse(
                                    obj.get("timestamp").getAsString(), FORMATTER);
                            } catch (Exception ignored) {}
                        }
                        bookmarks.add(new Bookmark(title, url, timestamp));
                    }
                }
                saveBookmarks();
                logger.info("Bookmarks imported successfully");
            }
        } catch (Exception e) {
            logger.error("Failed to import bookmarks from JSON", e);
        }
    }
    
    /**
     * Get bookmark count
     */
    public int getBookmarkCount() {
        return bookmarks.size();
    }
    
    /**
     * Get persistence statistics
     */
    public String getStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Total Bookmarks: %d\n", bookmarks.size()));
        
        if (!bookmarks.isEmpty()) {
            LocalDateTime oldest = bookmarks.get(0).timestamp;
            LocalDateTime newest = bookmarks.get(0).timestamp;
            
            for (Bookmark b : bookmarks) {
                if (b.timestamp.isBefore(oldest)) oldest = b.timestamp;
                if (b.timestamp.isAfter(newest)) newest = b.timestamp;
            }
            
            sb.append(String.format("Oldest Bookmark: %s\n", oldest));
            sb.append(String.format("Newest Bookmark: %s\n", newest));
        }
        
        return sb.toString();
    }
    
    /**
     * Bookmark entry class
     */
    public static class Bookmark {
        public String title;
        public String url;
        public LocalDateTime timestamp;
        
        public Bookmark(String title, String url, LocalDateTime timestamp) {
            this.title = title;
            this.url = url;
            this.timestamp = timestamp;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getUrl() {
            return url;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
}
