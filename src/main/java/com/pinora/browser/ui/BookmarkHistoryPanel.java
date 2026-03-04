package com.pinora.browser.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import com.pinora.browser.core.BookmarkManager;
import com.pinora.browser.core.HistoryManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Side panel for viewing and managing bookmarks and history
 */
public class BookmarkHistoryPanel extends VBox {
    
    private BookmarkManager bookmarkManager;
    private HistoryManager historyManager;
    private TabPane tabPane;
    private BrowserWindow browserWindow;
    
    private ListView<BookmarkHistoryItem> bookmarkList;
    private ListView<BookmarkHistoryItem> historyList;
    
    private TextField bookmarkSearchField;
    private TextField historySearchField;
    
    public BookmarkHistoryPanel(BookmarkManager bookmarkManager, HistoryManager historyManager, BrowserWindow browserWindow) {
        this.bookmarkManager = bookmarkManager;
        this.historyManager = historyManager;
        this.browserWindow = browserWindow;
        
        initializeUI();
    }
    
    private void initializeUI() {
        this.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0;");
        this.setPrefWidth(250);
        this.setMinWidth(150);
        this.setMaxWidth(400);
        
        // Tab Pane for Bookmarks and History
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-font-size: 11;");
        
        // Bookmarks Tab
        Tab bookmarksTab = createBookmarksTab();
        
        // History Tab
        Tab historyTab = createHistoryTab();
        
        tabPane.getTabs().addAll(bookmarksTab, historyTab);
        
        this.getChildren().add(tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
    }
    
    private Tab createBookmarksTab() {
        VBox bookmarksContent = new VBox(5);
        bookmarksContent.setPadding(new Insets(5));
        
        // Search bar
        bookmarkSearchField = new TextField();
        bookmarkSearchField.setPromptText("Search bookmarks...");
        bookmarkSearchField.setPrefHeight(30);
        bookmarkSearchField.setStyle("-fx-font-size: 11; -fx-padding: 5;");
        bookmarkSearchField.textProperty().addListener((obs, oldVal, newVal) -> filterBookmarks(newVal));
        
        // Add bookmark button
        Button addBookmarkBtn = new Button("Add Bookmark");
        addBookmarkBtn.setPrefWidth(Double.MAX_VALUE);
        addBookmarkBtn.setStyle("-fx-padding: 5;");
        addBookmarkBtn.setFocusTraversable(false);
        addBookmarkBtn.setOnAction(e -> {
            addCurrentPageAsBookmark();
            e.consume();
        });
        
        // Bookmarks list
        bookmarkList = new ListView<>();
        bookmarkList.setCellFactory(param -> {
            BookmarkHistoryCell cell = new BookmarkHistoryCell();
            cell.setPanel(this);
            return cell;
        });
        
        bookmarksContent.getChildren().addAll(
            bookmarkSearchField,
            addBookmarkBtn,
            bookmarkList
        );
        
        VBox.setVgrow(bookmarkList, Priority.ALWAYS);
        
        Tab tab = new Tab("Bookmarks", bookmarksContent);
        tab.setClosable(false);
        
        refreshBookmarkList();
        
        return tab;
    }
    
    private Tab createHistoryTab() {
        VBox historyContent = new VBox(5);
        historyContent.setPadding(new Insets(5));
        
        // Search bar
        historySearchField = new TextField();
        historySearchField.setPromptText("Search history...");
        historySearchField.setPrefHeight(30);
        historySearchField.setStyle("-fx-font-size: 11; -fx-padding: 5;");
        historySearchField.textProperty().addListener((obs, oldVal, newVal) -> filterHistory(newVal));
        
        // Clear history button
        Button clearHistoryBtn = new Button("Clear History");
        clearHistoryBtn.setPrefWidth(Double.MAX_VALUE);
        clearHistoryBtn.setStyle("-fx-padding: 5;");
        clearHistoryBtn.setFocusTraversable(false);
        clearHistoryBtn.setOnAction(e -> {
            clearHistoryConfirm();
            e.consume();
        });
        
        // History list
        historyList = new ListView<>();
        historyList.setCellFactory(param -> {
            BookmarkHistoryCell cell = new BookmarkHistoryCell();
            cell.setPanel(this);
            return cell;
        });
        
        historyContent.getChildren().addAll(
            historySearchField,
            clearHistoryBtn,
            historyList
        );
        
        VBox.setVgrow(historyList, Priority.ALWAYS);
        
        Tab tab = new Tab("History", historyContent);
        tab.setClosable(false);
        
        refreshHistoryList();
        
        return tab;
    }
    
    private void refreshBookmarkList() {
        bookmarkList.getItems().clear();
        for (BookmarkManager.Bookmark bookmark : bookmarkManager.getBookmarks()) {
            BookmarkHistoryItem item = new BookmarkHistoryItem(
                bookmark.getTitle(),
                bookmark.getUrl(),
                bookmark.getTimestamp()
            );
            bookmarkList.getItems().add(item);
        }
    }
    
    private void refreshHistoryList() {
        historyList.getItems().clear();
        for (HistoryManager.HistoryEntry entry : historyManager.getHistory()) {
            String title = entry.getTitle() != null ? entry.getTitle() : extractDomainFromUrl(entry.getUrl());
            BookmarkHistoryItem item = new BookmarkHistoryItem(
                title,
                entry.getUrl(),
                entry.getTimestamp()
            );
            historyList.getItems().add(item);
        }
    }
    
    private void filterBookmarks(String query) {
        bookmarkList.getItems().clear();
        for (BookmarkManager.Bookmark bookmark : bookmarkManager.searchBookmarks(query)) {
            BookmarkHistoryItem item = new BookmarkHistoryItem(
                bookmark.getTitle(),
                bookmark.getUrl(),
                bookmark.getTimestamp()
            );
            bookmarkList.getItems().add(item);
        }
    }
    
    private void filterHistory(String query) {
        historyList.getItems().clear();
        for (HistoryManager.HistoryEntry entry : historyManager.searchHistory(query)) {
            String title = entry.getTitle() != null ? entry.getTitle() : extractDomainFromUrl(entry.getUrl());
            BookmarkHistoryItem item = new BookmarkHistoryItem(
                title,
                entry.getUrl(),
                entry.getTimestamp()
            );
            historyList.getItems().add(item);
        }
    }
    
    private void addCurrentPageAsBookmark() {
        String url = browserWindow.getCurrentUrl();
        String title = browserWindow.getCurrentTitle();
        
        if (url == null || url.isEmpty()) {
            showAlert("No page loaded", "Please navigate to a page first.");
            return;
        }
        
        // Show dialog to get bookmark title
        TextInputDialog dialog = new TextInputDialog(title != null ? title : url);
        dialog.setTitle("Add Bookmark");
        dialog.setHeaderText("Bookmark this page");
        dialog.setContentText("Bookmark name:");
        
        dialog.showAndWait().ifPresent(bookmarkTitle -> {
            bookmarkManager.addBookmark(bookmarkTitle, url);
            refreshBookmarkList();
            showAlert("Bookmark Added", "'" + bookmarkTitle + "' has been bookmarked.");
        });
    }
    
    private void removeBookmark(BookmarkHistoryItem item) {
        bookmarkManager.removeBookmark(item.url);
        refreshBookmarkList();
    }
    
    private void removeHistoryEntry(BookmarkHistoryItem item) {
        historyManager.deleteEntry(item.url);
        refreshHistoryList();
    }
    
    /**
     * Remove item from either bookmarks or history
     */
    public void removeItem(BookmarkHistoryItem item) {
        // Try to remove from bookmarks first
        if (bookmarkManager.isBookmarked(item.url)) {
            removeBookmark(item);
        } else {
            // Otherwise remove from history
            removeHistoryEntry(item);
        }
    }
    
    private void clearHistoryConfirm() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear History");
        alert.setHeaderText("Clear all browsing history?");
        alert.setContentText("This action cannot be undone.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                historyManager.clearHistory();
                refreshHistoryList();
            }
        });
    }
    
    /**
     * Navigate to the given URL
     */
    public void navigateToUrl(String url) {
        browserWindow.navigateTo(url);
    }
    
    private String extractDomainFromUrl(String url) {
        try {
            java.net.URI u = new java.net.URI(url);
            String host = u.getHost();
            return host != null ? host : url;
        } catch (Exception e) {
            return url;
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void refresh() {
        refreshBookmarkList();
        refreshHistoryList();
    }
    
    /**
     * Select the bookmarks tab (bring it to front)
     */
    public void selectBookmarksTab() {
        if (tabPane != null && tabPane.getTabs().size() > 0) {
            tabPane.getSelectionModel().select(0);
        }
    }
    
    /**
     * Select the history tab (bring it to front)
     */
    public void selectHistoryTab() {
        if (tabPane != null && tabPane.getTabs().size() > 1) {
            tabPane.getSelectionModel().select(1);
        }
    }
    
    /**
     * Data class for bookmark/history items
     */
    public static class BookmarkHistoryItem {
        public String title;
        public String url;
        public LocalDateTime timestamp;
        
        public BookmarkHistoryItem(String title, String url, LocalDateTime timestamp) {
            this.title = title;
            this.url = url;
            this.timestamp = timestamp;
        }
        
        @Override
        public String toString() {
            return title;
        }
    }
    
    /**
     * Custom cell renderer for bookmark/history items
     */
    private static class BookmarkHistoryCell extends ListCell<BookmarkHistoryItem> {
        private BookmarkHistoryPanel panel;
        
        public BookmarkHistoryCell() {
        }
        
        public void setPanel(BookmarkHistoryPanel panel) {
            this.panel = panel;
        }
        
        @Override
        protected void updateItem(BookmarkHistoryItem item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            
            VBox cell = new VBox(2);
            cell.setPadding(new Insets(5));
            cell.setStyle("-fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0; -fx-padding: 5;");
            
            // Title
            Label titleLabel = new Label(item.title);
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11;");
            titleLabel.setWrapText(true);
            
            // URL (truncated)
            String displayUrl = item.url.length() > 40 ? item.url.substring(0, 40) + "..." : item.url;
            Label urlLabel = new Label(displayUrl);
            urlLabel.setStyle("-fx-text-fill: #0066cc; -fx-font-size: 10;");
            urlLabel.setWrapText(true);
            
            // Timestamp
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, HH:mm");
            Label timeLabel = new Label(item.timestamp.format(formatter));
            timeLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 9;");
            
            // Action buttons
            HBox actionBox = new HBox(5);
            actionBox.setPadding(new Insets(3, 0, 0, 0));
            
            Button openBtn = new Button("Open");
            openBtn.setStyle("-fx-font-size: 9; -fx-padding: 2 5;");
            openBtn.setPrefWidth(60);
            openBtn.setFocusTraversable(false);
            openBtn.setOnAction(e -> {
                if (panel != null) {
                    panel.navigateToUrl(item.url);
                }
                e.consume();
            });
            
            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle("-fx-font-size: 9; -fx-padding: 2 5;");
            deleteBtn.setPrefWidth(60);
            deleteBtn.setFocusTraversable(false);
            deleteBtn.setOnAction(e -> {
                if (panel != null) {
                    panel.removeItem(item);
                }
                e.consume();
            });
            
            actionBox.getChildren().addAll(openBtn, deleteBtn);
            
            cell.getChildren().addAll(titleLabel, urlLabel, timeLabel, actionBox);
            
            setGraphic(cell);
            setText(null);
        }
    }
}
