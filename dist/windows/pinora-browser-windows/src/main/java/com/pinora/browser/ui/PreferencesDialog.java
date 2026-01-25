package com.pinora.browser.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Preferences/Settings dialog
 */
public class PreferencesDialog {
    
    public static void show(Stage owner) {
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
        
        // Search Settings
        TitledPane searchPane = createSearchSettings();
        
        Accordion accordion = new Accordion();
        accordion.getPanes().addAll(generalPane, privacyPane, searchPane);
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
        showBookmarks.setSelected(true);
        
        CheckBox restoreTabs = new CheckBox("Restore tabs from last session");
        restoreTabs.setSelected(true);
        
        Label homePageLabel = new Label("Home page:");
        TextField homePageField = new TextField("https://www.google.com");
        homePageField.setPrefHeight(30);
        
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
        doNotTrack.setSelected(true);
        
        CheckBox blockTracking = new CheckBox("Block tracking cookies");
        blockTracking.setSelected(true);
        
        CheckBox clearOnExit = new CheckBox("Clear history on exit");
        clearOnExit.setSelected(false);
        
        Button clearNow = new Button("Clear History Now");
        clearNow.setStyle("-fx-padding: 8 15;");
        
        content.getChildren().addAll(
            doNotTrack, blockTracking, clearOnExit, new Separator(), clearNow
        );
        
        TitledPane pane = new TitledPane("Privacy & Security", content);
        pane.setCollapsible(false);
        return pane;
    }
    
    private static TitledPane createSearchSettings() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        Label searchEngineLabel = new Label("Default search engine:");
        ComboBox<String> searchEngineCombo = new ComboBox<>();
        searchEngineCombo.getItems().addAll(
            "Google",
            "DuckDuckGo",
            "Bing",
            "Wikipedia",
            "StartPage"
        );
        searchEngineCombo.setValue("Google");
        
        content.getChildren().addAll(searchEngineLabel, searchEngineCombo);
        
        TitledPane pane = new TitledPane("Search", content);
        pane.setCollapsible(false);
        return pane;
    }
}
