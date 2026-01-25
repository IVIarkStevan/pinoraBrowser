package com.pinora.browser.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.image.Image;
import com.pinora.browser.core.BrowserEngine;
import com.pinora.browser.util.URLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Browser Window UI
 */
public class BrowserWindow {
    
    private static final Logger logger = LoggerFactory.getLogger(BrowserWindow.class);
    
    private Stage stage;
    private TabPane tabPane;
    private TextField addressBar;
    private BrowserEngine browserEngine;
    
    public BrowserWindow() {
        this.browserEngine = new BrowserEngine();
    }
    
    public void show(Stage primaryStage) {
        this.stage = primaryStage;
        
        // Set window icon
        try {
            Image icon = new Image(getClass().getResourceAsStream("/icons/pinora.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            logger.warn("Could not load icon: {}", e.getMessage());
        }
        
        // Main Layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-font-family: 'Segoe UI', Arial, sans-serif;");
        
        // Top Menu Bar
        root.setTop(createMenuBar());
        
        // Toolbar
        root.setCenter(createMainContent());
        
        // Bottom Status Bar
        root.setBottom(createStatusBar());
        
        Scene scene = new Scene(root, 1200, 800);
        
        primaryStage.setTitle("Pinora Browser");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        logger.info("Browser window initialized");
    }
    
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-font-size: 11;");
        
        // File Menu
        Menu fileMenu = new Menu("File");
        MenuItem newTab = new MenuItem("New Tab");
        newTab.setOnAction(e -> addNewTab());
        MenuItem newWindow = new MenuItem("New Window");
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> System.exit(0));
        fileMenu.getItems().addAll(newTab, newWindow, new SeparatorMenuItem(), exit);
        
        // Edit Menu
        Menu editMenu = new Menu("Edit");
        MenuItem preferences = new MenuItem("Preferences");
        editMenu.getItems().add(preferences);
        
        // View Menu
        Menu viewMenu = new Menu("View");
        MenuItem zoomIn = new MenuItem("Zoom In (Ctrl++)");
        MenuItem zoomOut = new MenuItem("Zoom Out (Ctrl+-)");
        MenuItem resetZoom = new MenuItem("Reset Zoom (Ctrl+0)");
        viewMenu.getItems().addAll(zoomIn, zoomOut, new SeparatorMenuItem(), resetZoom);
        
        // History Menu
        Menu historyMenu = new Menu("History");
        MenuItem clearHistory = new MenuItem("Clear History");
        historyMenu.getItems().add(clearHistory);
        
        // Help Menu
        Menu helpMenu = new Menu("Help");
        MenuItem about = new MenuItem("About Pinora Browser");
        about.setOnAction(e -> showAboutDialog());
        helpMenu.getItems().add(about);
        
        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, historyMenu, helpMenu);
        return menuBar;
    }
    
    private VBox createMainContent() {
        VBox mainContent = new VBox(5);
        mainContent.setPadding(new Insets(5));
        
        // Toolbar
        mainContent.getChildren().add(createToolbar());
        
        // Tab Pane
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        tabPane.setStyle("-fx-font-size: 12;");
        
        // Add default tab
        addNewTab();
        
        mainContent.getChildren().add(tabPane);
        VBox.setVgrow(tabPane, javafx.scene.layout.Priority.ALWAYS);
        
        return mainContent;
    }
    
    private HBox createToolbar() {
        HBox toolbar = new HBox(8);
        toolbar.setPadding(new Insets(5));
        toolbar.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        
        // Back Button
        Button backButton = new Button("←");
        backButton.setPrefWidth(40);
        backButton.setStyle("-fx-font-size: 14;");
        
        // Forward Button
        Button forwardButton = new Button("→");
        forwardButton.setPrefWidth(40);
        forwardButton.setStyle("-fx-font-size: 14;");
        
        // Refresh Button
        Button refreshButton = new Button("⟳");
        refreshButton.setPrefWidth(40);
        refreshButton.setStyle("-fx-font-size: 14;");
        refreshButton.setOnAction(e -> refreshCurrentTab());
        
        // Home Button
        Button homeButton = new Button("⌂");
        homeButton.setPrefWidth(40);
        homeButton.setStyle("-fx-font-size: 14;");
        homeButton.setOnAction(e -> navigateToHome());
        
        // Address Bar
        addressBar = new TextField();
        addressBar.setPromptText("Enter URL or search...");
        addressBar.setPrefHeight(30);
        addressBar.setStyle("-fx-font-size: 12; -fx-padding: 5;");
        addressBar.setOnAction(e -> navigateToAddress());
        
        toolbar.getChildren().addAll(
            backButton, forwardButton, refreshButton, homeButton, addressBar
        );
        
        HBox.setHgrow(addressBar, javafx.scene.layout.Priority.ALWAYS);
        
        return toolbar;
    }
    
    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(5));
        statusBar.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0;");
        
        Label statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-font-size: 11;");
        statusBar.getChildren().add(statusLabel);
        
        return statusBar;
    }
    
    private void addNewTab() {
        Tab tab = new Tab();
        tab.setText("New Tab");
        tab.setClosable(true);
        
        WebView webView = new WebView();
        webView.setStyle("-fx-font-size: 12;");
        
        WebEngine engine = webView.getEngine();
        
        // Update tab title when page finishes loading
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            String title = engine.getTitle();
            String location = engine.getLocation();
            
            // Update tab title with website title or URL
            if (title != null && !title.isEmpty()) {
                String displayTitle = title.length() > 30 ? title.substring(0, 27) + "..." : title;
                tab.setText(displayTitle);
            } else if (location != null && !location.isEmpty()) {
                try {
                    java.net.URL url = new java.net.URL(location);
                    String host = url.getHost().replaceFirst("^www\\.", "");
                    tab.setText(host);
                } catch (Exception e) {
                    tab.setText("Loading...");
                }
            }
        });
        
        // Update address bar when tab is selected
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == tab) {
                String location = engine.getLocation();
                if (location != null && !location.isEmpty()) {
                    addressBar.setText(location);
                }
            }
        });
        
        tab.setContent(webView);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().selectLast();
        
        logger.info("New tab added");
    }
    
    private void navigateToAddress() {
        String address = addressBar.getText().trim();
        if (!address.isEmpty()) {
            String url = URLUtil.formatURL(address);
            addressBar.setText(url);
            
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            if (selectedTab != null) {
                WebView webView = (WebView) selectedTab.getContent();
                WebEngine engine = webView.getEngine();
                
                // Update tab title to loading state
                selectedTab.setText("Loading...");
                
                engine.load(url);
                logger.info("Navigating to: {}", url);
            }
        }
    }
    
    private void navigateToHome() {
        addressBar.setText("https://www.google.com");
        navigateToAddress();
    }
    
    private void refreshCurrentTab() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            WebView webView = (WebView) selectedTab.getContent();
            WebEngine engine = webView.getEngine();
            engine.reload();
            logger.info("Refreshing current tab");
        }
    }
    
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Pinora Browser");
        alert.setHeaderText("Pinora Browser v1.0.0");
        alert.setContentText(
            "A lightweight, minimal yet feature-rich web browser\n" +
            "Built with Java and JavaFX\n" +
            "© 2026 Pinora Browser Team"
        );
        alert.showAndWait();
    }
}
