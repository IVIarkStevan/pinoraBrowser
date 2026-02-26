package com.pinora.browser.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages bookmarks
 */
public class BookmarkManager {
    
    private static final Logger logger = LoggerFactory.getLogger(BookmarkManager.class);
    
    private List<Bookmark> bookmarks;
    
    public BookmarkManager() {
        this.bookmarks = new ArrayList<>();
    }
    
    public void addBookmark(String title, String url) {
        bookmarks.add(new Bookmark(title, url));
        logger.info("Bookmark added: {} -> {}", title, url);
    }
    
    public void removeBookmark(String url) {
        bookmarks.removeIf(b -> b.url.equals(url));
        logger.info("Bookmark removed: {}", url);
    }
    
    public List<Bookmark> getBookmarks() {
        return new ArrayList<>(bookmarks);
    }
    
    public static class Bookmark {
        public String title;
        public String url;
        
        public Bookmark(String title, String url) {
            this.title = title;
            this.url = url;
        }
    }
}
