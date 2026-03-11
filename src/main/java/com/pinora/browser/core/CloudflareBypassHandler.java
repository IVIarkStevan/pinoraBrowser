package com.pinora.browser.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles Cloudflare bypass by managing challenge tokens and cookies
 * Implements proper User-Agent and header spoofing for Cloudflare compatibility
 */
public class CloudflareBypassHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CloudflareBypassHandler.class);
    
    // Real Chrome User-Agent that Cloudflare recognizes
    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    
    private final CookieManager cookieManager;
    private final Map<String, Long> challengeTokenCache = new HashMap<>();
    
    public CloudflareBypassHandler(CookieManager cookieManager) {
        this.cookieManager = cookieManager;
    }
    
    /**
     * Enhanced HTTP connection setup for Cloudflare compatibility
     */
    public void configureConnection(HttpURLConnection connection, String url) {
        try {
            // Set proper User-Agent
            connection.setRequestProperty("User-Agent", USER_AGENT);
            
            // Set standard browser headers that Cloudflare expects
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            connection.setRequestProperty("DNT", "1");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
            connection.setRequestProperty("Sec-Fetch-Dest", "document");
            connection.setRequestProperty("Sec-Fetch-Mode", "navigate");
            connection.setRequestProperty("Sec-Fetch-Site", "none");
            connection.setRequestProperty("Sec-Fetch-User", "?1");
            
            // Add cookies
            URI uri = new URI(url);
            String domain = uri.getHost();
            java.util.List<CookieData> cookies = cookieManager.getCookiesForDomain(domain);
            
            if (!cookies.isEmpty()) {
                StringBuilder cookieHeader = new StringBuilder();
                for (int i = 0; i < cookies.size(); i++) {
                    if (i > 0) cookieHeader.append("; ");
                    CookieData cookie = cookies.get(i);
                    cookieHeader.append(cookie.getName()).append("=").append(cookie.getValue());
                }
                connection.setRequestProperty("Cookie", cookieHeader.toString());
                logger.debug("Added {} cookies to Cloudflare request", cookies.size());
            }
            
            // Set timeout
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            
        } catch (Exception e) {
            logger.warn("Error configuring Cloudflare connection: {}", e.getMessage());
        }
    }
    
    /**
     * Check if response indicates Cloudflare challenge
     */
    public boolean isCloudflareChallenge(HttpURLConnection connection) {
        try {
            int responseCode = connection.getResponseCode();
            String server = connection.getHeaderField("Server");
            String cacheControl = connection.getHeaderField("Cache-Control");
            
            // Cloudflare typically returns 403 for challenges or modifies content
            if (responseCode == 403 || (server != null && server.contains("cloudflare"))) {
                logger.debug("Detected Cloudflare challenge for {}", connection.getURL());
                return true;
            }
            
            // Check if response contains Cloudflare challenge JavaScript
            if (responseCode == 200) {
                // Read a small portion to check for challenge
                String contentType = connection.getContentType();
                if (contentType != null && contentType.contains("text/html")) {
                    logger.debug("Cloudflare HTML response detected, will be handled by WebEngine JavaScript");
                }
            }
        } catch (IOException e) {
            logger.debug("Error checking Cloudflare challenge: {}", e.getMessage());
        }
        return false;
    }
    
    /**
     * Extract and cache Cloudflare tokens from response
     */
    public void extractCloudflareTokens(HttpURLConnection connection, String url) {
        try {
            // Check for Cloudflare challenge token in Set-Cookie headers
            for (int i = 0; ; i++) {
                String headerName = connection.getHeaderFieldKey(i);
                if (headerName == null) break;
                
                if ("Set-Cookie".equalsIgnoreCase(headerName)) {
                    String cookie = connection.getHeaderField(i);
                    if (cookie != null && (cookie.contains("cf_clearance") || cookie.contains("__cfruid"))) {
                        logger.info("Detected Cloudflare security cookie");
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Error extracting Cloudflare tokens: {}", e.getMessage());
        }
    }
    
    /**
     * Clear cached challenge tokens for a domain
     */
    public void clearTokens(String domain) {
        challengeTokenCache.remove(domain);
    }
    
    /**
     * Log Cloudflare handling attempts for debugging
     */
    public void logCloudflareAttempt(String url, int retryCount) {
        logger.info("Cloudflare challenge attempt {} for {}", retryCount, url);
    }
}
