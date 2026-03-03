package com.pinora.browser.util;

/**
 * Enum for supported search engines
 */
public enum SearchEngine {
    GOOGLE("Google", "https://www.google.com/search?q="),
    BING("Bing", "https://www.bing.com/search?q="),
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q="),
    WIKIPEDIA("Wikipedia", "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch="),
    STARTPAGE("StartPage", "https://www.startpage.com/sp/search?query=");
    
    private final String displayName;
    private final String searchUrlTemplate;
    
    SearchEngine(String displayName, String searchUrlTemplate) {
        this.displayName = displayName;
        this.searchUrlTemplate = searchUrlTemplate;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getSearchUrlTemplate() {
        return searchUrlTemplate;
    }
    
    /**
     * Build complete search URL with encoded query
     */
    public String buildSearchUrl(String query) {
        return searchUrlTemplate + URLUtil.encodeURL(query);
    }
    
    /**
     * Get SearchEngine by display name
     */
    public static SearchEngine fromDisplayName(String displayName) {
        for (SearchEngine engine : values()) {
            if (engine.displayName.equalsIgnoreCase(displayName)) {
                return engine;
            }
        }
        return GOOGLE; // Default to Google
    }
}
