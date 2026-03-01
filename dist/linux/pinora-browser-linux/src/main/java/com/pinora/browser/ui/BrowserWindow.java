package com.pinora.browser.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.image.Image;
import javafx.application.Platform;
import com.pinora.browser.core.BrowserEngine;
import com.pinora.browser.core.CookieInterceptor;
import com.pinora.browser.util.URLUtil;
import com.pinora.browser.extensions.ExtensionManager;
import com.pinora.browser.extensions.webext.installer.WebExtensionInstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
/**
 * Main Browser Window UI
 */
public class BrowserWindow {
    
    private static final Logger logger = LoggerFactory.getLogger(BrowserWindow.class);
    
    private Stage stage;
    private Scene scene;
    private TabPane tabPane;
    private TextField addressBar;
    private BrowserEngine browserEngine;
    private ExtensionManager extensionManager;
    private CookieInterceptor cookieInterceptor;
    private Button backButton;
    private Button forwardButton;
    private com.pinora.browser.extensions.webext.WebExtensionLoader webExtensionLoader;
    private DownloadManager downloadManager = new DownloadManager();
    private double zoomLevel = 1.0; // Track current zoom level
    private static final double ZOOM_INCREMENT = 0.1; // 10% per zoom step
    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 3.0;
    
    public BrowserWindow() {
        this.browserEngine = new BrowserEngine();
        this.extensionManager = new ExtensionManager();
        this.cookieInterceptor = new CookieInterceptor(browserEngine.getCookieManager());
    }

    public ExtensionManager getExtensionManager() {
        return extensionManager;
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
        this.scene = scene;
        
        // Add keyboard shortcut handler
        scene.setOnKeyPressed(this::handleKeyboardShortcuts);

        // Apply dark mode if set in config
        try {
            if (com.pinora.browser.util.ConfigManager.isDarkModeEnabled()) {
                String css = getClass().getResource("/css/dark.css").toExternalForm();
                scene.getStylesheets().add(css);
            }
        } catch (Exception ignored) {}
        
        primaryStage.setTitle("Pinora Browser");
        primaryStage.setScene(scene);
        primaryStage.show();
        // Load extensions after the UI is visible
        try {
            extensionManager.loadExtensions(this);
            // Load WebExtensions (manifest-based) as well
            try {
                webExtensionLoader = new com.pinora.browser.extensions.webext.WebExtensionLoader();
                webExtensionLoader.loadAll();
            } catch (Exception ex) {
                logger.warn("Failed to load WebExtensions: {}", ex.getMessage());
            }
        } catch (Exception e) {
            logger.warn("Failed to load extensions: {}", e.getMessage());
        }
        
        logger.info("Browser window initialized");
    }
    
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-font-size: 11;");
        
        // File Menu
        Menu fileMenu = new Menu("File");
        MenuItem newTab = new MenuItem("New Tab (Ctrl+T)");
        newTab.setOnAction(e -> addNewTab());
        MenuItem newWindow = new MenuItem("New Window");
        MenuItem closeTab = new MenuItem("Close Tab (Ctrl+W)");
        closeTab.setOnAction(e -> closeCurrentTab());
        MenuItem exit = new MenuItem("Exit (Ctrl+Q)");
        exit.setOnAction(e -> stage.close());
        fileMenu.getItems().addAll(newTab, newWindow, new SeparatorMenuItem(), closeTab, new SeparatorMenuItem(), exit);
        
        // Edit Menu
        Menu editMenu = new Menu("Edit");
        MenuItem preferences = new MenuItem("Preferences");
        preferences.setOnAction(e -> {
            PreferencesDialog prefsDialog = new PreferencesDialog(browserEngine.getCookieManager());
            prefsDialog.show(stage);
        });
        editMenu.getItems().add(preferences);
        
        // View Menu
        Menu viewMenu = new Menu("View");
        MenuItem zoomIn = new MenuItem("Zoom In (Ctrl++)");
        MenuItem zoomOut = new MenuItem("Zoom Out (Ctrl+-)");
        MenuItem resetZoom = new MenuItem("Reset Zoom (Ctrl+0)");
        zoomIn.setOnAction(e -> handleZoomIn());
        zoomOut.setOnAction(e -> handleZoomOut());
        resetZoom.setOnAction(e -> handleResetZoom());
        MenuItem showDownloads = new MenuItem("Downloads");
        showDownloads.setOnAction(e -> openDownloadsTab());
        showDownloads.setAccelerator(KeyCombination.keyCombination("Ctrl+J"));

        CheckMenuItem darkMode = new CheckMenuItem("Dark Mode");
        darkMode.setSelected(com.pinora.browser.util.ConfigManager.isDarkModeEnabled());
        darkMode.setOnAction(e -> {
            boolean enabled = darkMode.isSelected();
            try {
                if (enabled) {
                    String css = getClass().getResource("/css/dark.css").toExternalForm();
                    scene.getStylesheets().add(css);
                } else {
                    scene.getStylesheets().removeIf(s -> s.contains("dark.css"));
                }
            } catch (Exception ex) {}
            com.pinora.browser.util.ConfigManager.setDarkModeEnabled(enabled);
        });
        darkMode.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+D"));
        viewMenu.getItems().addAll(zoomIn, zoomOut, new SeparatorMenuItem(), resetZoom);
        viewMenu.getItems().add(new SeparatorMenuItem());
        viewMenu.getItems().add(darkMode);
        viewMenu.getItems().add(new SeparatorMenuItem());
        viewMenu.getItems().add(showDownloads);
        
        // History Menu
        Menu historyMenu = new Menu("History");
        MenuItem clearHistory = new MenuItem("Clear History");
        clearHistory.setOnAction(e -> {
            try {
                browserEngine.getHistoryManager().clearHistory();
                new Alert(Alert.AlertType.INFORMATION, "Browsing history cleared", ButtonType.OK).showAndWait();
            } catch (Exception ex) {
                logger.warn("Error clearing history: {}", ex.getMessage());
                new Alert(Alert.AlertType.ERROR, "Failed to clear history", ButtonType.OK).showAndWait();
            }
        });
        historyMenu.getItems().add(clearHistory);
        
        // Extensions Menu
        Menu extensionsMenu = new Menu("Extensions");
        MenuItem manageExtensions = new MenuItem("Manage Extensions...");
        manageExtensions.setOnAction(e -> {
            try {
                new WebExtensionsManagerDialog(stage, extensionManager, new WebExtensionInstaller()).showAndWait();
            } catch (Exception ex) {
                logger.warn("Failed to open Extensions manager: {}", ex.getMessage());
            }
        });
        extensionsMenu.getItems().add(manageExtensions);

        // Tools Menu
        Menu toolsMenu = new Menu("Tools");
        MenuItem cookieManager = new MenuItem("Cookie Manager...");
        cookieManager.setOnAction(e -> {
            try {
                CookieManagerDialog dialog = new CookieManagerDialog(browserEngine.getCookieManager());
                dialog.show(stage);
            } catch (Exception ex) {
                logger.error("Failed to open Cookie Manager", ex);
            }
        });
        toolsMenu.getItems().add(cookieManager);

        // Help Menu
        Menu helpMenu = new Menu("Help");
        MenuItem about = new MenuItem("About Pinora Browser");
        about.setOnAction(e -> showAboutDialog());
        helpMenu.getItems().add(about);

        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, historyMenu, extensionsMenu, toolsMenu, helpMenu);
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
        backButton = new Button("←");
        backButton.setPrefWidth(40);
        backButton.setStyle("-fx-font-size: 14;");
        backButton.setDisable(true);
        backButton.setOnAction(e -> navigateBack());
        
        // Forward Button
        forwardButton = new Button("→");
        forwardButton.setPrefWidth(40);
        forwardButton.setStyle("-fx-font-size: 14;");
        forwardButton.setDisable(true);
        forwardButton.setOnAction(e -> navigateForward());
        
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
        
        // Extension Icon Bar
        ExtensionIconBar extensionBar = new ExtensionIconBar(webExtensionLoader);
        extensionBar.setStyle("-fx-padding: 0; -fx-border-width: 0;");
        
        toolbar.getChildren().addAll(
            backButton, forwardButton, refreshButton, homeButton, addressBar, extensionBar
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
    
    /**
     * Initialize WebEngine with proper settings for image/video rendering, audio, and error handling
     */
    private void initializeWebEngine(WebEngine engine) {
        // Create persistent cache directories
        try {
            java.io.File cacheDir = new java.io.File(System.getProperty("user.home") + "/.pinora-browser/cache");
            java.io.File cookieDir = new java.io.File(System.getProperty("user.home") + "/.pinora-browser/cookies");
            java.io.File sessionDir = new java.io.File(System.getProperty("user.home") + "/.pinora-browser/session");
            
            if (!cacheDir.exists()) cacheDir.mkdirs();
            if (!cookieDir.exists()) cookieDir.mkdirs();
            if (!sessionDir.exists()) sessionDir.mkdirs();
            
            logger.info("WebEngine cache directories initialized");
        } catch (Exception e) {
            logger.warn("Failed to create cache directories: {}", e.getMessage());
        }
        
        // Enable JavaScript - required for web functionality
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                try {
                    // Inject AudioContext initialization script for proper audio playback
                    // This ensures audio is not distorted and sample rates are correct
                    String audioInitScript = "if(window.AudioContext){"
                        + "  if(!window._pinora_audio_context){"
                        + "    try{"
                        + "      window._pinora_audio_context=new AudioContext({sampleRate:48000,latencyHint:'interactive'});"
                        + "      console.log('[Pinora] AudioContext initialized at 48kHz');"
                        + "    }catch(e){"
                        + "      try{"
                        + "        window._pinora_audio_context=new AudioContext();"
                        + "        console.log('[Pinora] AudioContext initialized with default sample rate');"
                        + "      }catch(e2){console.warn('[Pinora] AudioContext initialization failed:',e2);}"
                        + "    }"
                        + "  }"
                        + "}";
                    engine.executeScript(audioInitScript);
                } catch (Exception e) {
                    logger.debug("Failed to inject audio initialization: {}", e.getMessage());
                }
            }
        });
        
        // Error handler for script execution
        engine.setOnError(event -> {
            logger.debug("WebEngine error: {}", event.getMessage());
        });
        
        // Handle load worker errors
        engine.getLoadWorker().exceptionProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                logger.debug("Page load exception: {}", newVal.getMessage());
            }
        });
        
        // Monitor page state changes for debugging
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.FAILED) {
                logger.error("Page load failed for: {}", engine.getLocation());
                Throwable exception = engine.getLoadWorker().getException();
                if (exception != null) {
                    logger.error("Load error details: {}", exception.getMessage());
                }
            }
        });
        
        logger.info("WebEngine initialized with cache support and audio configuration");
    }
    
    private void addNewTab() {
        Tab tab = new Tab();
        tab.setText("New Tab");
        tab.setClosable(true);
        
        WebView webView = new WebView();
        webView.setStyle("-fx-font-size: 12;");
        
        WebEngine engine = webView.getEngine();
        
        // Initialize WebEngine with proper settings
        initializeWebEngine(engine);

        // Context menu for link/image download
        webView.setOnMousePressed(me -> {
            if (me.isSecondaryButtonDown()) {
                try {
                    double x = me.getX();
                    double y = me.getY();
                    String script = "(function(){var e=document.elementFromPoint(" + (int)x + "," + (int)y + "); if(!e) return ''; var t=e.tagName.toLowerCase(); if(t==='a') return e.href; if(t==='img') return e.src; return '';})()";
                    Object res = engine.executeScript(script);
                    String url = res == null ? "" : res.toString();
                    if (url != null && !url.isEmpty()) {
                        javafx.application.Platform.runLater(() -> {
                            ContextMenu cm = new ContextMenu();
                            MenuItem downloadLink = new MenuItem("Download");
                            downloadLink.setOnAction(ae -> downloadManager.startDownload(url, stage));
                            MenuItem openNew = new MenuItem("Open in New Tab");
                            openNew.setOnAction(ae -> {
                                Tab t = new Tab("New Tab");
                                WebView wv = new WebView();
                                wv.getEngine().load(url);
                                t.setContent(wv);
                                tabPane.getTabs().add(t);
                                tabPane.getSelectionModel().select(t);
                            });
                            cm.getItems().addAll(downloadLink, openNew);
                            cm.show(webView, me.getScreenX(), me.getScreenY());
                        });
                    }
                } catch (Exception ignored) {
                }
            }
        });
        
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
                    java.net.URL url = URI.create(location).toURL();
                    String host = url.getHost().replaceFirst("^www\\.", "");
                    tab.setText(host);
                } catch (Exception e) {
                    tab.setText("Loading...");
                }
            }
        });
        // After page load succeeded, inject content scripts from webextensions
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            try {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED && webExtensionLoader != null) {
                    String url = engine.getLocation();
                    for (com.pinora.browser.extensions.webext.WebExtensionContext ctx : webExtensionLoader.getAllExtensions()) {
                        com.pinora.browser.extensions.webext.WebExtensionManifest manifest = ctx.getManifest();
                        if (manifest.getContentScripts() != null && !manifest.getContentScripts().isEmpty()) {
                            com.pinora.browser.extensions.webext.ContentScriptInjector.injectScripts(webView, url, manifest.getContentScripts(), ctx);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Error injecting content scripts: {}", e.getMessage());
            }
        });
        // Update navigation buttons when history changes
        try {
            engine.getHistory().currentIndexProperty().addListener((obs, oldIdx, newIdx) -> {
                updateNavigationButtons();
                try {
                    int idx = newIdx.intValue();
                    if (idx >= 0 && idx < engine.getHistory().getEntries().size()) {
                        String urlFromHistory = engine.getHistory().getEntries().get(idx).getUrl();
                        if (urlFromHistory != null && !urlFromHistory.isEmpty()) {
                            addressBar.setText(urlFromHistory);
                        }
                    }
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
        }

        // Intercept navigations to non-HTML resources and offer to download instead
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.RUNNING) {
                String loc = engine.getLocation();
                if (loc == null || loc.isEmpty()) return;
                // run a HEAD request in background to inspect content-type
                new Thread(() -> {
                    try {
                        java.net.URL u = URI.create(loc).toURL();
                        java.net.HttpURLConnection c = (java.net.HttpURLConnection) u.openConnection();
                        c.setRequestMethod("HEAD");
                        c.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                        c.setInstanceFollowRedirects(true);
                        
                        // Add cookies from cookie manager to the request
                        cookieInterceptor.addCookiesFromManager(c, loc);
                        
                        c.connect();
                        
                        // Extract any cookies from the response
                        cookieInterceptor.extractCookiesFromResponse(c, loc);
                        
                        String ct = c.getContentType();
                        if (ct != null && !ct.toLowerCase().startsWith("text/html")) {
                            // cancel navigation and prompt download
                            try { engine.getLoadWorker().cancel(); } catch (Exception ignored) {}
                            Platform.runLater(() -> {
                                Alert a = new Alert(Alert.AlertType.CONFIRMATION, "The resource appears to be a file (" + ct + "). Download instead?", ButtonType.YES, ButtonType.NO);
                                a.setHeaderText("Download file");
                                a.showAndWait().ifPresent(b -> {
                                    if (b == ButtonType.YES) {
                                        downloadManager.startDownload(loc, stage);
                                    }
                                });
                            });
                        }
                    } catch (Exception ignored) {
                    }
                }, "download-inspect").start();
            }
        });
        
        // Update address bar when tab is selected
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == tab) {
                String location = engine.getLocation();
                if (location != null && !location.isEmpty()) {
                    addressBar.setText(location);
                }
                updateNavigationButtons();
            }
        });
        
        tab.setContent(webView);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().selectLast();
        // Clear address bar for new tab
        addressBar.setText("");
        // Ensure navigation buttons reflect the newly selected tab
        updateNavigationButtons();
        
        logger.info("New tab added");
    }

    private void openDownloadsTab() {
        // Check if downloads tab already exists
        for (Tab t : tabPane.getTabs()) {
            if ("Downloads".equals(t.getText())) {
                tabPane.getSelectionModel().select(t);
                return;
            }
        }

        Tab dtab = new Tab("Downloads");
        dtab.setClosable(true);
        dtab.setContent(downloadManager.getView());
        tabPane.getTabs().add(dtab);
        tabPane.getSelectionModel().select(dtab);
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
                // update nav buttons after navigation starts
                updateNavigationButtons();
            }
        }
    }

    private void navigateBack() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null) return;
        WebView webView = (WebView) selectedTab.getContent();
        WebEngine engine = webView.getEngine();
        try {
            javafx.scene.web.WebHistory history = engine.getHistory();
            int idx = history.getCurrentIndex();
            if (idx > 0) {
                history.go(-1);
                javafx.application.Platform.runLater(() -> updateNavigationButtons());
                return;
            }
            // Fallback to JS history if WebHistory didn't move
            engine.executeScript("history.back()");
        } catch (Exception e) {
            logger.warn("Back navigation failed: {}", e.getMessage());
        } finally {
            javafx.application.Platform.runLater(() -> updateNavigationButtons());
        }
    }

    private void navigateForward() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null) return;
        WebView webView = (WebView) selectedTab.getContent();
        WebEngine engine = webView.getEngine();
        try {
            javafx.scene.web.WebHistory history = engine.getHistory();
            int idx = history.getCurrentIndex();
            if (idx < history.getEntries().size() - 1) {
                history.go(1);
                javafx.application.Platform.runLater(() -> updateNavigationButtons());
                return;
            }
            // Fallback to JS history if WebHistory didn't move
            engine.executeScript("history.forward()");
        } catch (Exception e) {
            logger.warn("Forward navigation failed: {}", e.getMessage());
        } finally {
            javafx.application.Platform.runLater(() -> updateNavigationButtons());
        }
    }

    

    private void updateNavigationButtons() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null) {
            if (backButton != null) backButton.setDisable(true);
            if (forwardButton != null) forwardButton.setDisable(true);
            return;
        }
        try {
            WebView webView = (WebView) selectedTab.getContent();
            WebEngine engine = webView.getEngine();
            javafx.scene.web.WebHistory history = engine.getHistory();
            int idx = history.getCurrentIndex();
            int size = history.getEntries().size();
            if (backButton != null) backButton.setDisable(idx <= 0);
            if (forwardButton != null) forwardButton.setDisable(idx >= size - 1);
        } catch (Exception e) {
            if (backButton != null) backButton.setDisable(true);
            if (forwardButton != null) forwardButton.setDisable(true);
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
            """
            A lightweight, minimal yet feature-rich web browser
            Built with Java and JavaFX
            © 2026 Pinora Browser Team"""
        );
        alert.showAndWait();
    }
    
    private void handleKeyboardShortcuts(KeyEvent event) {
        if (event.isControlDown()) {
            if (event.getCode() == KeyCode.T) {
                // Ctrl+T: New Tab
                addNewTab();
                event.consume();
            } else if (event.getCode() == KeyCode.W) {
                // Ctrl+W: Close Tab
                closeCurrentTab();
                event.consume();
            } else if (event.getCode() == KeyCode.J) {
                // Ctrl+J: Open Downloads
                openDownloadsTab();
                event.consume();
            } else if (event.getCode() == KeyCode.Q) {
                // Ctrl+Q: Quit Application
                stage.close();
                event.consume();
            } else if (event.getCode() == KeyCode.R) {
                // Ctrl+R: Reload current tab
                refreshCurrentTab();
                event.consume();
            } else if (event.getCode() == KeyCode.PLUS || event.getCode() == KeyCode.EQUALS) {
                // Ctrl++: Zoom In
                handleZoomIn();
                event.consume();
            } else if (event.getCode() == KeyCode.MINUS) {
                // Ctrl+-: Zoom Out
                handleZoomOut();
                event.consume();
            } else if (event.getCode() == KeyCode.DIGIT0) {
                // Ctrl+0: Reset Zoom
                handleResetZoom();
                event.consume();
            }
        }
    }
    
    private void handleZoomIn() {
        if (zoomLevel < MAX_ZOOM) {
            zoomLevel += ZOOM_INCREMENT;
            applyZoom();
        }
    }
    
    private void handleZoomOut() {
        if (zoomLevel > MIN_ZOOM) {
            zoomLevel -= ZOOM_INCREMENT;
            applyZoom();
        }
    }
    
    private void handleResetZoom() {
        zoomLevel = 1.0;
        applyZoom();
    }
    
    private void applyZoom() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            try {
                WebView webView = (WebView) selectedTab.getContent();
                WebEngine engine = webView.getEngine();
                // Apply zoom using CSS transform on the document body
                engine.executeScript("document.body.style.zoom = '" + (zoomLevel * 100) + "%';");
            } catch (Exception e) {
                logger.debug("Error applying zoom: {}", e.getMessage());
            }
        }
    }

private void closeCurrentTab() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            // Clean up WebEngine resources before closing
            try {
                WebView webView = (WebView) selectedTab.getContent();
                if (webView != null) {
                    WebEngine engine = webView.getEngine();
                    if (engine != null) {
                        // Cancel any pending loads
                        engine.getLoadWorker().cancel();
                        
                        // Load about:blank to release resources
                        engine.load(null);
                        
                        // Clear the page history to free memory
                        try {
                            engine.getHistory().getEntries().clear();
                        } catch (Exception ignored) {}
                        
                        // Set JavaScript disabled to stop any running scripts
                        // This helps release any held resources
                        logger.debug("WebEngine resources cleaned up for tab: {}", selectedTab.getText());
                    }
                }
            } catch (Exception e) {
                logger.debug("Error cleaning up tab: {}", e.getMessage());
            }
            
            tabPane.getTabs().remove(selectedTab);
            logger.info("Tab closed");
            
            // Close application if no tabs left
            if (tabPane.getTabs().isEmpty()) {
                stage.close();
            }
        }
    }
}
