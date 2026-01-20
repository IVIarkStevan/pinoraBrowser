package com.pinora.browser.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages browser cache for lightweight performance
 */
public class CacheManager {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);
    
    private Map<String, CacheEntry> cache;
    private static final int MAX_CACHE_SIZE = 100;
    private static final long CACHE_EXPIRY_TIME = 3600000; // 1 hour in milliseconds
    
    public CacheManager() {
        this.cache = new HashMap<>();
    }
    
    public void put(String key, Object value) {
        if (cache.size() >= MAX_CACHE_SIZE) {
            // Simple FIFO eviction
            cache.remove(cache.keySet().iterator().next());
        }
        cache.put(key, new CacheEntry(value, System.currentTimeMillis()));
        logger.debug("Cache: stored key {}", key);
    }
    
    public Object get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            logger.debug("Cache: hit for key {}", key);
            return entry.value;
        }
        if (entry != null) {
            cache.remove(key);
        }
        return null;
    }
    
    public void clear() {
        cache.clear();
        logger.info("Cache cleared");
    }
    
    private class CacheEntry {
        Object value;
        long timestamp;
        
        CacheEntry(Object value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_TIME;
        }
    }
}
