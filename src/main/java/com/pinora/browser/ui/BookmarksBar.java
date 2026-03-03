package com.pinora.browser.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import com.pinora.browser.core.BookmarkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * A horizontal bookmarks bar that displays quick access bookmarks
 * Supports drag-and-drop reordering and click navigation
 */
public class BookmarksBar extends HBox {
    
    private static final Logger logger = LoggerFactory.getLogger(BookmarksBar.class);
    
    private BookmarkManager bookmarkManager;
    private BrowserWindow browserWindow;
    
    public BookmarksBar(BookmarkManager bookmarkManager, BrowserWindow browserWindow) {
        super(5);
        this.bookmarkManager = bookmarkManager;
        this.browserWindow = browserWindow;
        
        this.setPadding(new Insets(5, 5, 5, 5));
        this.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; -fx-background-color: #fafafa;");
        
        loadBookmarks();
    }
    
    /**
     * Load and display all bookmarks as buttons
     */
    private void loadBookmarks() {
        this.getChildren().clear();
        
        List<BookmarkManager.Bookmark> bookmarks = bookmarkManager.getBookmarks();
        
        if (bookmarks.isEmpty()) {
            Label emptyLabel = new Label("No bookmarks - Right-click a page to add one");
            emptyLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11;");
            this.getChildren().add(emptyLabel);
            return;
        }
        
        // Add a label for the bookmarks bar
        Label label = new Label("Bookmarks:");
        label.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");
        this.getChildren().add(label);
        
        // Add each bookmark as a draggable button
        for (int i = 0; i < bookmarks.size(); i++) {
            BookmarkManager.Bookmark bookmark = bookmarks.get(i);
            Button bookmarkButton = createBookmarkButton(bookmark, i, bookmarks.size());
            this.getChildren().add(bookmarkButton);
        }
        
        // Add spacer to push buttons to the left
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        this.getChildren().add(spacer);
    }
    
    /**
     * Create a draggable button for a single bookmark
     */
    private Button createBookmarkButton(BookmarkManager.Bookmark bookmark, int index, int total) {
        Button button = new Button();
        
        // Display title with ellipsis if too long
        String displayTitle = bookmark.getTitle();
        if (displayTitle.length() > 20) {
            displayTitle = displayTitle.substring(0, 17) + "...";
        }
        
        button.setText(displayTitle);
        button.setStyle("-fx-font-size: 11; -fx-padding: 3 8;");
        button.setTooltip(new Tooltip(bookmark.getTitle() + "\n" + bookmark.getUrl()));
        
        // Navigate to bookmark on click
        button.setOnAction(event -> {
            browserWindow.navigateTo(bookmark.getUrl());
            logger.info("Bookmark clicked: {}", bookmark.getTitle());
        });
        
        // Setup drag-and-drop for reordering
        setupDragAndDrop(button, bookmark, index, total);
        
        return button;
    }
    
    /**
     * Setup drag-and-drop for bookmark reordering
     */
    private void setupDragAndDrop(Button button, BookmarkManager.Bookmark bookmark, int fromIndex, int total) {
        // Make button draggable
        button.setOnDragDetected(event -> {
            Dragboard db = button.startDragAndDrop(TransferMode.MOVE);
            
            ClipboardContent content = new ClipboardContent();
            content.putString(bookmark.getUrl());
            db.setContent(content);
            
            event.consume();
        });
        
        // Handle drop on other bookmarks
        button.setOnDragOver(event -> {
            if (event.getGestureSource() != button && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        
        // Handle drop completion - swap bookmarks
        button.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            
            if (db.hasString()) {
                String droppedUrl = db.getString();
                
                // Find the dropped bookmark and current bookmark
                List<BookmarkManager.Bookmark> allBookmarks = bookmarkManager.getBookmarks();
                int droppedIndex = -1;
                
                for (int i = 0; i < allBookmarks.size(); i++) {
                    if (allBookmarks.get(i).getUrl().equals(droppedUrl)) {
                        droppedIndex = i;
                        break;
                    }
                }
                
                if (droppedIndex >= 0 && droppedIndex != fromIndex) {
                    // Swap the bookmarks in the list
                    BookmarkManager.Bookmark temp = allBookmarks.get(droppedIndex);
                    allBookmarks.set(droppedIndex, allBookmarks.get(fromIndex));
                    allBookmarks.set(fromIndex, temp);
                    
                    // Save and refresh
                    saveBookmarkOrder(allBookmarks);
                    loadBookmarks();
                    
                    success = true;
                    logger.info("Bookmarks reordered: {} <-> {}", 
                               allBookmarks.get(fromIndex).getTitle(),
                               allBookmarks.get(droppedIndex).getTitle());
                }
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
    }
    
    /**
     * Save the new bookmark order to disk
     * Since BookmarkManager doesn't have a direct method to set bookmark order,
     * we'll clear and re-add them in the new order
     */
    private void saveBookmarkOrder(List<BookmarkManager.Bookmark> orderedBookmarks) {
        try {
            // Clear all bookmarks
            bookmarkManager.clearBookmarks();
            
            // Re-add in new order
            for (BookmarkManager.Bookmark bookmark : orderedBookmarks) {
                bookmarkManager.addBookmark(bookmark.getTitle(), bookmark.getUrl());
            }
            
            logger.debug("Bookmark order saved");
        } catch (Exception e) {
            logger.error("Failed to save bookmark order: {}", e.getMessage());
        }
    }
    
    /**
     * Refresh the bookmarks bar display
     * Call this when bookmarks are added or removed
     */
    public void refresh() {
        loadBookmarks();
    }
}
