package com.pinora.browser.extensions.webext.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Implements browser.messaging API for WebExtensions.
 * Allows extensions and content scripts to send/receive messages.
 */
public class MessagingAPI {

    private Map<String, Consumer<Message>> listeners = new ConcurrentHashMap<>();
    private Map<String, Consumer<Object>> responseListeners = new ConcurrentHashMap<>();

    /**
     * Send a message to another part of the extension.
     */
    public void sendMessage(Map<String, Object> message, Consumer<Object> callback) {
        String messageId = java.util.UUID.randomUUID().toString();
        if (callback != null) {
            responseListeners.put(messageId, callback);
        }

        Message msg = new Message(messageId, message);
        for (Consumer<Message> listener : listeners.values()) {
            try {
                listener.accept(msg);
            } catch (Exception e) {
                // Listener error
            }
        }
    }

    /**
     * Register a listener for incoming messages.
     */
    public void onMessage(String listenerId, Consumer<Message> handler) {
        listeners.put(listenerId, handler);
    }

    /**
     * Remove a message listener.
     */
    public void offMessage(String listenerId) {
        listeners.remove(listenerId);
    }

    /**
     * Send a response to a message.
     */
    public void sendResponse(String messageId, Object response) {
        Consumer<Object> callback = responseListeners.remove(messageId);
        if (callback != null) {
            callback.accept(response);
        }
    }

    /**
     * Represents a message passed between extension parts.
     */
    public static class Message {
        private String id;
        private Map<String, Object> data;
        private long timestamp;

        public Message(String id, Map<String, Object> data) {
            this.id = id;
            this.data = new HashMap<>(data);
            this.timestamp = System.currentTimeMillis();
        }

        public String getId() { return id; }
        public Map<String, Object> getData() { return data; }
        public long getTimestamp() { return timestamp; }

        public Object get(String key) {
            return data.get(key);
        }

        public String getString(String key) {
            Object val = data.get(key);
            return val != null ? val.toString() : null;
        }

        public boolean getBoolean(String key) {
            Object val = data.get(key);
            return val instanceof Boolean ? (Boolean) val : false;
        }
    }
}
