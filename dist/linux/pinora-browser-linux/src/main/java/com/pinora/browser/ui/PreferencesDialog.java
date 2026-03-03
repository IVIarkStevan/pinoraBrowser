package com.pinora.browser.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import com.pinora.browser.core.CookieManager;
import com.pinora.browser.util.ConfigManager;
import com.pinora.browser.util.SearchEngine;

/**
 * Preferences/Settings dialog
 */
public class PreferencesDialog {
    
    public PreferencesDialog(CookieManager cookieManager) {
        // Constructor accepts cookieManager for potential future use
    }
    
    public void show(Stage owner) {
        Stage preferencesStage = new Stage();
        preferencesStage.setTitle("Preferences - Pinora Browser");
        preferencesStage.setWidth(600);
        preferencesStage.setHeight(500);
        preferencesStage.initOwner(owner);
        
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-font-size: 11;");
        
        // General Settings
        TitledPane generalPane = createGeneralSettings();
        
        // Privacy Settings
        TitledPane privacyPane = createPrivacySettings();
        
        // Cookie Settings
        TitledPane cookiePane = createCookieSettings(preferencesStage);
        
        // Search Settings
        TitledPane searchPane = createSearchSettings();
        
        Accordion accordion = new Accordion();
        accordion.getPanes().addAll(generalPane, privacyPane, cookiePane, searchPane);
        accordion.setExpandedPane(generalPane);
        
        root.getChildren().add(accordion);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center-right;");
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> preferencesStage.close());
        buttonBox.getChildren().add(closeButton);
        
        root.getChildren().add(buttonBox);
        VBox.setVgrow(accordion, javafx.scene.layout.Priority.ALWAYS);
        
        Scene scene = new Scene(root);
        preferencesStage.setScene(scene);
        preferencesStage.show();
    }
    
    private static TitledPane createGeneralSettings() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        CheckBox showBookmarks = new CheckBox("Show bookmarks bar on startup");
        showBookmarks.setSelected(ConfigManager.isShowBookmarksBar());
        showBookmarks.setOnAction(e -> ConfigManager.setShowBookmarksBar(showBookmarks.isSelected()));
        
        CheckBox restoreTabs = new CheckBox("Restore tabs from last session");
        restoreTabs.setSelected(ConfigManager.isRestoreTabsFromLastSession());
        restoreTabs.setOnAction(e -> ConfigManager.setRestoreTabsFromLastSession(restoreTabs.isSelected()));
        
        Label homePageLabel = new Label("Home page:");
        TextField homePageField = new TextField(ConfigManager.getHomePage());
        homePageField.setPrefHeight(30);
        homePageField.setOnAction(e -> ConfigManager.setHomePage(homePageField.getText()));
        homePageField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                ConfigManager.setHomePage(homePageField.getText());
            }
        });
        
        content.getChildren().addAll(
            showBookmarks, restoreTabs, homePageLabel, homePageField
        );
        
        TitledPane pane = new TitledPane("General", content);
        pane.setCollapsible(false);
        return pane;
    }
    
    private static TitledPane createPrivacySettings() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        CheckBox doNotTrack = new CheckBox("Send Do Not Track signal");
        doNotTrack.setSelected(ConfigManager.isDoNotTrackEnabled());
        doNotTrack.setOnAction(e -> ConfigManager.setDoNotTrackEnabled(doNotTrack.isSelected()));
        
        CheckBox blockTracking = new CheckBox("Block tracking cookies");
        blockTracking.setSelected(ConfigManager.isBlockTrackingCookies());
        blockTracking.setOnAction(e -> ConfigManager.setBlockTrackingCookies(blockTracking.isSelected()));
        
        CheckBox clearOnExit = new CheckBox("Clear history on exit");
        clearOnExit.setSelected(ConfigManager.isClearHistoryOnExit());
        clearOnExit.setOnAction(e -> ConfigManager.setClearHistoryOnExit(clearOnExit.isSelected()));
        
        Button clearNow = new Button("Clear History Now");
        clearNow.setStyle("-fx-padding: 8 15;");
        
        content.getChildren().addAll(
            doNotTrack, blockTracking, clearOnExit, new Separator(), clearNow
        );
        
        TitledPane pane = new TitledPane("Privacy & Security", content);
        pane.setCollapsible(false);
        return pane;
    }
    
    private static TitledPane createCookieSettings(Stage owner) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        Label cookieHeaderLabel = new Label("Cookie Management:");
        cookieHeaderLabel.setStyle("-fx-font-weight: bold;");
        
        CheckBox acceptCookies = new CheckBox("Accept cookies");
        acceptCookies.setSelected(ConfigManager.isAcceptCookies());
        acceptCookies.setOnAction(e -> ConfigManager.setAcceptCookies(acceptCookies.isSelected()));
        
        CheckBox blockThirdParty = new CheckBox("Block third-party cookies");
        blockThirdParty.setSelected(ConfigManager.isBlockThirdPartyCookies());
        blockThirdParty.setOnAction(e -> ConfigManager.setBlockThirdPartyCookies(blockThirdParty.isSelected()));
        
        CheckBox blockTracking = new CheckBox("Block tracking cookies");
        blockTracking.setSelected(ConfigManager.isBlockTrackingCookies());
        blockTracking.setOnAction(e -> ConfigManager.setBlockTrackingCookies(blockTracking.isSelected()));
        
        CheckBox saveCookies = new CheckBox("Save cookies between sessions");
        saveCookies.setSelected(ConfigManager.isSaveCookiesBetweenSessions());
        saveCookies.setOnAction(e -> ConfigManager.setSaveCookiesBetweenSessions(saveCookies.isSelected()));
        
        Label cookieInfoLabel = new Label("Persistent cookies will be saved to disk and restored on browser restart.\nSession cookies are cleared when you close the browser.");
        cookieInfoLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10;");
        cookieInfoLabel.setWrapText(true);
        
        HBox managerBox = new HBox(10);
        managerBox.setPadding(new Insets(10, 0, 0, 0));
        Label managerLabel = new Label("Full Cookie Management:");
        Button openManager = new Button("Open Cookie Manager");
        openManager.setStyle("-fx-padding: 5 15;");
        openManager.setDisable(true); // Will be enabled by the caller if cookie manager is available
        managerBox.getChildren().addAll(managerLabel, openManager);
        
        content.getChildren().addAll(
            cookieHeaderLabel, new Separator(),
            acceptCookies, blockThirdParty, blockTracking, saveCookies,
            new Separator(), cookieInfoLabel,
            new Separator(), managerBox
        );
        
        TitledPane pane = new TitledPane("Cookies", content);
        pane.setCollapsible(false);
        return pane;
    }
    
    private static TitledPane createSearchSettings() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        Label searchEngineLabel = new Label("Default search engine:");
        ComboBox<String> searchEngineCombo = new ComboBox<>();
        searchEngineCombo.getItems().addAll(
            SearchEngine.GOOGLE.getDisplayName(),
            SearchEngine.DUCKDUCKGO.getDisplayName(),
            SearchEngine.BING.getDisplayName(),
            SearchEngine.WIKIPEDIA.getDisplayName(),
            SearchEngine.STARTPAGE.getDisplayName()
        );
        
        // Set current selection from config
        SearchEngine currentEngine = ConfigManager.getDefaultSearchEngine();
        searchEngineCombo.setValue(currentEngine.getDisplayName());
        
        // Save selection when changed
        searchEngineCombo.setOnAction(e -> {
            String selected = searchEngineCombo.getValue();
            SearchEngine engine = SearchEngine.fromDisplayName(selected);
            ConfigManager.setDefaultSearchEngine(engine);
        });
        
        content.getChildren().addAll(searchEngineLabel, searchEngineCombo);
        
        TitledPane pane = new TitledPane("Search", content);
        pane.setCollapsible(false);
        return pane;
    }
}
