package com.pinora.browser.util;

import java.net.URI;

/**
 * URL utility functions
 */
public class URLUtil {
    
    /**
     * Format URL - add protocol if missing, handle search queries
     */
    public static String formatURL(String input) {
        input = input.trim();
        
        // If it looks like a search query (no spaces and no obvious domain pattern)
        if (!input.contains(" ") && !input.contains(".")) {
            return "https://www.google.com/search?q=" + encodeURL(input);
        }
        
        // If contains space, treat as search query
        if (input.contains(" ")) {
            return "https://www.google.com/search?q=" + encodeURL(input);
        }
        
        // If already has protocol, return as is
        if (input.startsWith("http://") || input.startsWith("https://")) {
            return input;
        }
        
        // If looks like IP address
        if (input.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+.*")) {
            return "http://" + input;
        }
        
        // Otherwise add https protocol
        return "https://" + input;
    }
    
    /**
     * Simple URL encoding
     */
    public static String encodeURL(String input) {
        try {
            return java.net.URLEncoder.encode(input, "UTF-8");
        } catch (Exception e) {
            return input;
        }
    }
    
    /**
     * Validate if string is a valid URL
     */
    public static boolean isValidURL(String url) {
        try {
            URI.create(url).toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
