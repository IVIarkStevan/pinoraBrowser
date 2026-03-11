package com.pinora.browser.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages search history and provides search suggestions/autocomplete
 */
public class SearchSuggestionsManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SearchSuggestionsManager.class);
    
    private static final String SEARCH_HISTORY_FILE = ConfigManager.getConfigDirectory() + "/search_history.json";
    private static final Gson GSON = new Gson();
    private static final int MAX_SUGGESTIONS = 10;
    private static final int MAX_HISTORY_ITEMS = 500;
    
    private List<SearchHistoryEntry> searchHistory;
    
    public SearchSuggestionsManager() {
        this.searchHistory = new ArrayList<>();
        loadSearchHistory();
    }
    
    /**
     * Represents a search history entry
     */
    public static class SearchHistoryEntry {
        private String query;
        private long timestamp;
        private int frequency; // How many times this query was searched
        
        public SearchHistoryEntry(String query) {
            this.query = query;
            this.timestamp = System.currentTimeMillis();
            this.frequency = 1;
        }
        
        public String getQuery() {
            return query;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public int getFrequency() {
            return frequency;
        }
        
        public void incrementFrequency() {
            this.frequency++;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Add a search query to history
     * 
     * @param query The search query to add
     */
    public void addSearchToHistory(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }
        
        String trimmedQuery = query.trim();
        
        // Check if query already exists
        Optional<SearchHistoryEntry> existing = searchHistory.stream()
            .filter(e -> e.query.equalsIgnoreCase(trimmedQuery))
            .findFirst();
        
        if (existing.isPresent()) {
            // Update frequency and timestamp
            existing.get().incrementFrequency();
        } else {
            // Add new entry
            searchHistory.addFirst(new SearchHistoryEntry(trimmedQuery));
            
            // Remove oldest if exceeds max
            if (searchHistory.size() > MAX_HISTORY_ITEMS) {
                searchHistory.removeLast();
            }
        }
        
        // Save to disk
        saveSearchHistory();
        logger.debug("Added search to history: {}", trimmedQuery);
    }
    
    /**
     * Get search suggestions for a given prefix
     * 
     * @param prefix The search query prefix to get suggestions for
     * @return List of suggestions sorted by frequency and recency
     */
    public List<String> getSuggestions(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            // Return top recent searches
            return searchHistory.stream()
                .limit(MAX_SUGGESTIONS)
                .map(e -> e.query)
                .collect(Collectors.toList());
        }
        
        String lowerPrefix = prefix.toLowerCase().trim();
        
        return searchHistory.stream()
            .filter(e -> e.query.toLowerCase().startsWith(lowerPrefix))
            .sorted((a, b) -> {
                // Sort by frequency first (descending), then by timestamp (descending)
                int freqCompare = Integer.compare(b.frequency, a.frequency);
                if (freqCompare != 0) {
                    return freqCompare;
                }
                return Long.compare(b.timestamp, a.timestamp);
            })
            .limit(MAX_SUGGESTIONS)
            .map(e -> e.query)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all search history entries
     */
    public List<SearchHistoryEntry> getSearchHistory() {
        return new ArrayList<>(searchHistory);
    }
    
    /**
     * Clear all search history
     */
    public void clearSearchHistory() {
        searchHistory.clear();
        saveSearchHistory();
        logger.info("Search history cleared");
    }
    
    /**
     * Delete a specific search from history
     * 
     * @param query The query to delete
     */
    public void deleteSearch(String query) {
        searchHistory.removeIf(e -> e.query.equalsIgnoreCase(query));
        saveSearchHistory();
    }
    
    /**
     * Save search history to disk
     */
    private void saveSearchHistory() {
        try {
            JsonObject historyObj = new JsonObject();
            JsonArray entriesArray = new JsonArray();
            
            for (SearchHistoryEntry entry : searchHistory) {
                JsonObject entryObj = new JsonObject();
                entryObj.addProperty("query", entry.query);
                entryObj.addProperty("timestamp", entry.timestamp);
                entryObj.addProperty("frequency", entry.frequency);
                entriesArray.add(entryObj);
            }
            
            historyObj.add("entries", entriesArray);
            
            // Write to file
            try (FileWriter writer = new FileWriter(SEARCH_HISTORY_FILE)) {
                GSON.toJson(historyObj, writer);
            }
            
            logger.debug("Search history saved");
        } catch (Exception e) {
            logger.warn("Failed to save search history: {}", e.getMessage());
        }
    }
    
    /**
     * Load search history from disk
     */
    private void loadSearchHistory() {
        try {
            File historyFile = new File(SEARCH_HISTORY_FILE);
            if (!historyFile.exists()) {
                logger.debug("Search history file does not exist");
                return;
            }
            
            try (FileReader reader = new FileReader(historyFile)) {
                JsonObject historyObj = GSON.fromJson(reader, JsonObject.class);
                
                if (historyObj != null && historyObj.has("entries")) {
                    JsonArray entriesArray = historyObj.getAsJsonArray("entries");
                    
                    for (int i = 0; i < entriesArray.size(); i++) {
                        JsonObject entryObj = entriesArray.get(i).getAsJsonObject();
                        String query = entryObj.has("query") ? entryObj.get("query").getAsString() : "";
                        long timestamp = entryObj.has("timestamp") ? entryObj.get("timestamp").getAsLong() : System.currentTimeMillis();
                        int frequency = entryObj.has("frequency") ? entryObj.get("frequency").getAsInt() : 1;
                        
                        if (!query.isEmpty()) {
                            SearchHistoryEntry entry = new SearchHistoryEntry(query);
                            entry.timestamp = timestamp;
                            entry.frequency = frequency;
                            searchHistory.add(entry);
                        }
                    }
                    
                    logger.info("Search history loaded with {} entries", searchHistory.size());
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to load search history: {}", e.getMessage());
        }
    }
}
