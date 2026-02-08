package com.pinora.browser.extensions.webext.api;

import javafx.scene.web.WebView;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements browser.tabs API for WebExtensions.
 * Allows extensions to create, close, and query browser tabs.
 */
public class TabsAPI {

    private Map<Integer, TabInfo> tabs = new ConcurrentHashMap<>();
    private int nextTabId = 1;

    public TabsAPI() {
    }

    /**
     * Create a new tab.
     */
    public TabInfo create(String url, boolean active) {
        int tabId = nextTabId++;
        TabInfo tab = new TabInfo(tabId, url, active);
        tabs.put(tabId, tab);
        return tab;
    }

    /**
     * Get a tab by ID.
     */
    public TabInfo get(int tabId) {
        return tabs.get(tabId);
    }

    /**
     * Query tabs by properties.
     */
    public List<TabInfo> query(Map<String, Object> queryInfo) {
        List<TabInfo> result = new java.util.ArrayList<>();
        boolean active = queryInfo.containsKey("active") ? (boolean) queryInfo.get("active") : false;
        
        for (TabInfo tab : tabs.values()) {
            if (!queryInfo.containsKey("active") || tab.isActive() == active) {
                result.add(tab);
            }
        }
        return result;
    }

    /**
     * Update a tab (URL, active state, etc).
     */
    public TabInfo update(int tabId, Map<String, Object> updateInfo) {
        TabInfo tab = tabs.get(tabId);
        if (tab != null) {
            if (updateInfo.containsKey("url")) {
                tab.setUrl((String) updateInfo.get("url"));
            }
            if (updateInfo.containsKey("active")) {
                tab.setActive((boolean) updateInfo.get("active"));
            }
        }
        return tab;
    }

    /**
     * Close a tab.
     */
    public void remove(int tabId) {
        tabs.remove(tabId);
    }

    /**
     * Represents a browser tab.
     */
    public static class TabInfo {
        private int id;
        private String url;
        private String title;
        private boolean active;
        private long lastAccessed;

        public TabInfo(int id, String url, boolean active) {
            this.id = id;
            this.url = url;
            this.active = active;
            this.lastAccessed = System.currentTimeMillis();
        }

        public int getId() { return id; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public long getLastAccessed() { return lastAccessed; }
    }
}
