package com.pinora.browser.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;

/**
 * Intercepts HTTP requests/responses to manage cookies
 * Handles sending cookies with requests and extracting Set-Cookie headers from responses
 */
public class CookieInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(CookieInterceptor.class);
    private static final String COOKIE_HEADER = "Cookie";
    private static final String SET_COOKIE_HEADER = "Set-Cookie";
    
    private final CookieManager cookieManager;
    
    public CookieInterceptor(CookieManager cookieManager) {
        this.cookieManager = cookieManager;
    }
    
    /**
     * Add cookies from CookieManager to an HTTP request
     */
    public void addCookiesFromManager(HttpURLConnection connection, String urlString) {
        try {
            URI uri = new URI(urlString);
            String domain = uri.getHost();
            String path = uri.getPath();
            
            if (domain == null || domain.isEmpty()) {
                return;
            }
            
            // Get all applicable cookies for this domain/path
            List<CookieData> applicableCookies = cookieManager.getCookiesForDomain(domain);
            
            if (!applicableCookies.isEmpty()) {
                StringBuilder cookieHeader = new StringBuilder();
                boolean first = true;
                
                for (CookieData cookie : applicableCookies) {
                    // Check path match
                    if (!pathMatches(path, cookie.getPath())) {
                        continue;
                    }
                    
                    // Check secure flag (only send secure cookies over HTTPS)
                    if (cookie.isSecure() && !urlString.startsWith("https")) {
                        logger.debug("Skipping secure cookie {} over HTTP", cookie.getName());
                        continue;
                    }
                    
                    // Skip expired cookies
                    if (cookie.isExpired()) {
                        continue;
                    }
                    
                    if (!first) {
                        cookieHeader.append("; ");
                    }
                    cookieHeader.append(cookie.getName()).append("=").append(cookie.getValue());
                    first = false;
                }
                
                if (cookieHeader.length() > 0) {
                    connection.setRequestProperty(COOKIE_HEADER, cookieHeader.toString());
                    logger.debug("Added {} cookies to request for {}", 
                        cookieHeader.toString().split(";").length, domain);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to add cookies to request: {}", e.getMessage());
        }
    }
    
    /**
     * Extract Set-Cookie headers from HTTP response and store in CookieManager
     */
    public void extractCookiesFromResponse(HttpURLConnection connection, String urlString) {
        try {
            URI uri = new URI(urlString);
            String domain = uri.getHost();
            
            if (domain == null || domain.isEmpty()) {
                return;
            }
            
            // Get all Set-Cookie headers
            for (int i = 0; ; i++) {
                String headerName = connection.getHeaderFieldKey(i);
                if (headerName == null) break;
                
                if (SET_COOKIE_HEADER.equalsIgnoreCase(headerName)) {
                    String setCookieHeader = connection.getHeaderField(i);
                    parseCookieFromSetCookieHeader(setCookieHeader, domain);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to extract cookies from response: {}", e.getMessage());
        }
    }
    
    /**
     * Parse a Set-Cookie header and store the cookie in the manager
     * Format: name=value; Path=/; Domain=example.com; Expires=...; Secure; HttpOnly; SameSite=Strict
     */
    private void parseCookieFromSetCookieHeader(String setCookieHeader, String defaultDomain) {
        if (setCookieHeader == null || setCookieHeader.isEmpty()) {
            return;
        }
        
        try {
            String[] parts = setCookieHeader.split(";");
            if (parts.length == 0) {
                return;
            }
            
            // Parse name=value
            String[] nameValue = parts[0].trim().split("=", 2);
            if (nameValue.length < 2) {
                return;
            }
            
            String name = nameValue[0].trim();
            String value = nameValue[1].trim();
            
            CookieData cookie = new CookieData();
            cookie.setName(name);
            cookie.setValue(value);
            cookie.setDomain(defaultDomain);
            cookie.setPath("/"); // Default path
            cookie.setSessionOnly(true);
            
            // Parse attributes
            for (int i = 1; i < parts.length; i++) {
                String attribute = parts[i].trim();
                
                if (attribute.equalsIgnoreCase("Secure")) {
                    cookie.setSecure(true);
                } else if (attribute.equalsIgnoreCase("HttpOnly")) {
                    cookie.setHttpOnly(true);
                } else if (attribute.startsWith("Path=")) {
                    cookie.setPath(attribute.substring(5).trim());
                } else if (attribute.startsWith("Domain=")) {
                    String domainValue = attribute.substring(7).trim();
                    if (!domainValue.isEmpty()) {
                        cookie.setDomain(domainValue.startsWith(".") ? domainValue : "." + domainValue);
                    }
                } else if (attribute.startsWith("Expires=")) {
                    try {
                        // Parse HTTP date format
                        String expiresStr = attribute.substring(8).trim();
                        long expiryTime = parseHttpDate(expiresStr);
                        if (expiryTime > 0) {
                            cookie.setExpiryTime(expiryTime);
                            cookie.setSessionOnly(false);
                        }
                    } catch (Exception e) {
                        logger.debug("Failed to parse cookie expiry date: {}", e.getMessage());
                    }
                } else if (attribute.startsWith("Max-Age=")) {
                    try {
                        long maxAge = Long.parseLong(attribute.substring(8).trim());
                        cookie.setExpiryTime(System.currentTimeMillis() + (maxAge * 1000));
                        cookie.setSessionOnly(false);
                    } catch (Exception e) {
                        logger.debug("Failed to parse Max-Age: {}", e.getMessage());
                    }
                } else if (attribute.startsWith("SameSite=")) {
                    cookie.setSameSite(attribute.substring(9).trim());
                }
            }
            
            // Store in cookie manager
            cookieManager.setCookie(cookie);
            logger.debug("Stored cookie: {}", name);
            
        } catch (Exception e) {
            logger.warn("Failed to parse Set-Cookie header: {}", e.getMessage());
        }
    }
    
    /**
     * Simple HTTP date parser for cookie expiry dates
     * Supports: "Wed, 09 Jun 2021 10:18:14 GMT" format
     */
    private long parseHttpDate(String dateStr) {
        try {
            // Try parsing RFC 2822 format: "Wed, 09 Jun 2021 10:18:14 GMT"
            java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
            java.util.Date date = formatter.parse(dateStr);
            return date.getTime();
        } catch (Exception e) {
            logger.debug("Failed to parse HTTP date: {}", dateStr);
            return 0;
        }
    }
    
    /**
     * Check if a cookie path matches the request path
     */
    private boolean pathMatches(String requestPath, String cookiePath) {
        if (requestPath == null) {
            requestPath = "/";
        }
        if (cookiePath == null || cookiePath.isEmpty()) {
            cookiePath = "/";
        }
        
        // Exact match
        if (requestPath.equals(cookiePath)) {
            return true;
        }
        
        // Path prefix match (RFC 6265)
        if (requestPath.startsWith(cookiePath)) {
            // Next character after cookie path must be "/" or end of string
            if (cookiePath.endsWith("/")) {
                return true;
            } else if (requestPath.length() > cookiePath.length()) {
                return requestPath.charAt(cookiePath.length()) == '/';
            }
        }
        
        return false;
    }
}
