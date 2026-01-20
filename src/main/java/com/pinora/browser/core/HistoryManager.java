package com.pinora.browser.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages browsing history
 */
public class HistoryManager {
    
    private static final Logger logger = LoggerFactory.getLogger(HistoryManager.class);
    
    private List<HistoryEntry> history;
    private static final int MAX_HISTORY_ITEMS = 1000;
    
    public HistoryManager() {
        this.history = new ArrayList<>();
    }
    
    public void addToHistory(String url) {
        HistoryEntry entry = new HistoryEntry(url, LocalDateTime.now());
        history.add(0, entry);
        
        if (history.size() > MAX_HISTORY_ITEMS) {
            history.remove(history.size() - 1);
        }
        
        logger.debug("Added to history: {}", url);
    }
    
    public List<HistoryEntry> getHistory() {
        return new ArrayList<>(history);
    }
    
    public void clearHistory() {
        history.clear();
        logger.info("History cleared");
    }
    
    public static class HistoryEntry {
        public String url;
        public LocalDateTime timestamp;
        
        public HistoryEntry(String url, LocalDateTime timestamp) {
            this.url = url;
            this.timestamp = timestamp;
        }
    }
}
