package com.pinora.browser.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core Browser Engine handling navigation and web requests
 */
public class BrowserEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(BrowserEngine.class);
    
    private HistoryManager historyManager;
    private BookmarkManager bookmarkManager;
    private CacheManager cacheManager;
    private CookieManager cookieManager;
    
    public BrowserEngine() {
        this.historyManager = new HistoryManager();
        this.bookmarkManager = new BookmarkManager();
        this.cacheManager = new CacheManager();
        this.cookieManager = new CookieManager();
        logger.info("Browser Engine initialized");
    }
    
    public void navigate(String url) {
        logger.info("Navigating to: {}", url);
        historyManager.addToHistory(url);
    }
    
    public HistoryManager getHistoryManager() {
        return historyManager;
    }
    
    public BookmarkManager getBookmarkManager() {
        return bookmarkManager;
    }
    
    public CacheManager getCacheManager() {
        return cacheManager;
    }
    
    public CookieManager getCookieManager() {
        return cookieManager;
    }
}
