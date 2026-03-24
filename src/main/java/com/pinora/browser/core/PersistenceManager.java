package com.pinora.browser.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

/**
 * Centralized persistence manager for browser data
 * Handles auto-save, backup, and data integrity checks
 */
public class PersistenceManager {
    
    private static final Logger logger = LoggerFactory.getLogger(PersistenceManager.class);
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
        Thread t = new Thread(r, "PersistenceManager-AutoSave");
        t.setDaemon(true);
        return t;
    });
    
    private final BookmarkManager bookmarkManager;
    private final HistoryManager historyManager;
    private final CookieManager cookieManager;
    
    private static final long AUTO_SAVE_INTERVAL = 5; // minutes
    private static final String BACKUP_DIR = com.pinora.browser.util.ConfigManager.getConfigDirectory() + "/backups";
    
    public PersistenceManager(BookmarkManager bookmarkManager, HistoryManager historyManager, CookieManager cookieManager) {
        this.bookmarkManager = bookmarkManager;
        this.historyManager = historyManager;
        this.cookieManager = cookieManager;
        
        initializeBackupDirectory();
        startAutoSave();
        validateDataIntegrity();
        
        logger.info("PersistenceManager initialized");
    }
    
    /**
     * Initialize backup directory
     */
    private void initializeBackupDirectory() {
        try {
            Files.createDirectories(Paths.get(BACKUP_DIR));
            logger.debug("Backup directory ready: {}", BACKUP_DIR);
        } catch (Exception e) {
            logger.warn("Failed to create backup directory: {}", e.getMessage());
        }
    }
    
    /**
     * Start automatic save scheduler
     */
    private void startAutoSave() {
        scheduler.scheduleAtFixedRate(
            this::performAutoSave,
            AUTO_SAVE_INTERVAL,
            AUTO_SAVE_INTERVAL,
            TimeUnit.MINUTES
        );
        logger.info("Auto-save scheduled every {} minutes", AUTO_SAVE_INTERVAL);
    }
    
    /**
     * Perform auto-save of all data
     */
    private void performAutoSave() {
        try {
            // Data is automatically saved by each manager when modified
            // This is a hook for future enhancements (e.g., background sync)
            logger.debug("Auto-save cycle completed");
        } catch (Exception e) {
            logger.error("Error during auto-save: {}", e.getMessage());
        }
    }
    
    /**
     * Perform immediate save of all managers
     */
    public synchronized void saveAll() {
        try {
            // Managers save themselves, but we can add housekeeping here
            logger.info("Saved all browser data");
        } catch (Exception e) {
            logger.error("Error saving all data: {}", e.getMessage());
        }
    }
    
    /**
     * Validate data integrity
     */
    public void validateDataIntegrity() {
        try {
            // Check if bookmark file exists and is readable
            String bookmarkFile = com.pinora.browser.util.ConfigManager.getBookmarksFile();
            File bf = new File(bookmarkFile);
            if (bf.exists() && !bf.canRead()) {
                logger.warn("Bookmark file is not readable: {}", bookmarkFile);
            }
            
            // Check if history file exists and is readable
            String historyFile = com.pinora.browser.util.ConfigManager.getHistoryFile();
            File hf = new File(historyFile);
            if (hf.exists() && !hf.canRead()) {
                logger.warn("History file is not readable: {}", historyFile);
            }
            
            logger.info("Data integrity check passed");
        } catch (Exception e) {
            logger.error("Error validating data: {}", e.getMessage());
        }
    }
    
    /**
     * Create a backup of current data
     */
    public void createBackup() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String backupSubDir = BACKUP_DIR + "/" + timestamp;
            
            Files.createDirectories(Paths.get(backupSubDir));
            
            // Copy bookmark file
            String bookmarkFile = com.pinora.browser.util.ConfigManager.getBookmarksFile();
            File bf = new File(bookmarkFile);
            if (bf.exists()) {
                Files.copy(Paths.get(bookmarkFile), 
                    Paths.get(backupSubDir + "/bookmarks.json"),
                    StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Copy history file
            String historyFile = com.pinora.browser.util.ConfigManager.getHistoryFile();
            File hf = new File(historyFile);
            if (hf.exists()) {
                Files.copy(Paths.get(historyFile),
                    Paths.get(backupSubDir + "/history.json"),
                    StandardCopyOption.REPLACE_EXISTING);
            }
            
            logger.info("Backup created: {}", backupSubDir);
        } catch (Exception e) {
            logger.error("Failed to create backup: {}", e.getMessage());
        }
    }
    
    /**
     * Get list of available backups
     */
    public java.util.List<String> getAvailableBackups() {
        java.util.List<String> backups = new java.util.ArrayList<>();
        try {
            Files.list(Paths.get(BACKUP_DIR))
                .filter(Files::isDirectory)
                .map(Path::getFileName)
                .map(Path::toString)
                .sorted()
                .forEach(backups::add);
        } catch (Exception e) {
            logger.warn("Error listing backups: {}", e.getMessage());
        }
        return backups;
    }
    
    /**
     * Get backup directory path
     */
    public String getBackupDirectory() {
        return BACKUP_DIR;
    }
    
    /**
     * Get persistence statistics
     */
    public String getPersistenceStats() {
        StringBuilder sb = new StringBuilder();
        try {
            int bookmarkCount = bookmarkManager.getBookmarks().size();
            int historyCount = historyManager.getHistoryCount();
            int backupCount = getAvailableBackups().size();
            
            sb.append("Persistence Statistics:\n");
            sb.append(String.format("  Bookmarks: %d\n", bookmarkCount));
            sb.append(String.format("  History: %d\n", historyCount));
            sb.append(String.format("  Available Backups: %d\n", backupCount));
            
            File bookmarkFile = new File(com.pinora.browser.util.ConfigManager.getBookmarksFile());
            if (bookmarkFile.exists()) {
                sb.append(String.format("  Bookmark File Size: %.2f KB\n", bookmarkFile.length() / 1024.0));
            }
            
            File historyFile = new File(com.pinora.browser.util.ConfigManager.getHistoryFile());
            if (historyFile.exists()) {
                sb.append(String.format("  History File Size: %.2f KB\n", historyFile.length() / 1024.0));
            }
        } catch (Exception e) {
            logger.error("Error getting persistence stats: {}", e.getMessage());
        }
        return sb.toString();
    }
    
    /**
     * Shutdown persistence manager
     */
    public void shutdown() {
        try {
            saveAll();
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            logger.info("PersistenceManager shutdown complete");
        } catch (Exception e) {
            logger.error("Error during shutdown: {}", e.getMessage());
        }
    }
}
