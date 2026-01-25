package com.pinora.browser.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration manager for Pinora Browser
 */
public class ConfigManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.pinora";
    private static final String CONFIG_FILE = CONFIG_DIR + "/config.json";
    
    static {
        initConfigDirectory();
    }
    
    private static void initConfigDirectory() {
        try {
            File configDir = new File(CONFIG_DIR);
            if (!configDir.exists()) {
                configDir.mkdirs();
                logger.info("Config directory created: {}", CONFIG_DIR);
            }
        } catch (Exception e) {
            logger.error("Failed to create config directory", e);
        }
    }
    
    public static String getConfigDirectory() {
        return CONFIG_DIR;
    }
    
    public static String getHistoryFile() {
        return CONFIG_DIR + "/history.json";
    }
    
    public static String getBookmarksFile() {
        return CONFIG_DIR + "/bookmarks.json";
    }
    
    public static String getCacheDirectory() {
        return CONFIG_DIR + "/cache";
    }
    
    public static String getDownloadsDirectory() {
        String defaultDownloads = System.getProperty("user.home") + "/Downloads";
        return defaultDownloads;
    }
}
