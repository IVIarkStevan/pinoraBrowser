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
        Bookmark bookmark = new Bookmark(title, url, LocalDateTime.now());
        bookmarks.add(bookmark);
        
        // Save to disk
        saveBookmarks();
        
        logger.info("Bookmark added: {} -> {}", title, url);
    }
    
    public void removeBookmark(String url) {
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
