package com.pinora.browser.extensions.webext.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements browser.storage API for WebExtensions.
 * Supports local and sync storage namespaces.
 */
public class StorageAPI {

    private StorageArea localArea = new StorageArea();
    private StorageArea syncArea = new StorageArea();
    private Map<String, StorageChangeListener> listeners = new ConcurrentHashMap<>();

    /**
     * Get from local storage.
     */
    public Object getLocal(String key) {
        return localArea.get(key);
    }

    /**
     * Set in local storage.
     */
    public void setLocal(String key, Object value) {
        Object oldValue = localArea.get(key);
        localArea.set(key, value);
        notifyChange(key, oldValue, value);
    }

    /**
     * Remove from local storage.
     */
    public void removeLocal(String key) {
        Object oldValue = localArea.get(key);
        localArea.remove(key);
        notifyChange(key, oldValue, null);
    }

    /**
     * Clear local storage.
     */
    public void clearLocal() {
        localArea.clear();
    }

    /**
     * Get from sync storage.
     */
    public Object getSync(String key) {
        return syncArea.get(key);
    }

    /**
     * Set in sync storage.
     */
    public void setSync(String key, Object value) {
        Object oldValue = syncArea.get(key);
        syncArea.set(key, value);
        notifyChange(key, oldValue, value);
    }

    /**
     * Register a change listener.
     */
    public void onChanged(String listenerId, StorageChangeListener listener) {
        listeners.put(listenerId, listener);
    }

    private void notifyChange(String key, Object oldValue, Object newValue) {
        for (StorageChangeListener listener : listeners.values()) {
            listener.onChange(key, oldValue, newValue);
        }
    }

    /**
     * Simple in-memory storage area.
     */
    public static class StorageArea {
        private Map<String, Object> data = new ConcurrentHashMap<>();

        public Object get(String key) {
            return data.get(key);
        }

        public void set(String key, Object value) {
            data.put(key, value);
        }

        public void remove(String key) {
            data.remove(key);
        }

        public void clear() {
            data.clear();
        }

        public Map<String, Object> getAll() {
            return new HashMap<>(data);
        }
    }

    /**
     * Listener for storage changes.
     */
    public interface StorageChangeListener {
        void onChange(String key, Object oldValue, Object newValue);
    }
}
