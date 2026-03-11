package com.pinora.browser.ui;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Developer Console panel for displaying JavaScript console output
 */
public class DeveloperConsole extends VBox {
    
    private static final Logger logger = LoggerFactory.getLogger(DeveloperConsole.class);
    
    private ListView<ConsoleMessage> messageList;
    private TextField filterField;
    private ComboBox<String> levelFilter;
    private List<ConsoleMessage> allMessages = new ArrayList<>();
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    
    public DeveloperConsole() {
        initializeUI();
    }
    
    private void initializeUI() {
        this.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 10;");
        this.setPadding(new Insets(5));
        this.setSpacing(5);
        
        // Toolbar
        HBox toolbar = createToolbar();
        this.getChildren().add(toolbar);
        
        // Message list
        messageList = new ListView<>();
        messageList.setPrefHeight(200);
        messageList.setCellFactory(param -> new ConsoleMessageCell());
        messageList.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: #e0e0e0;");
        
        this.getChildren().add(messageList);
        VBox.setVgrow(messageList, Priority.ALWAYS);
    }
    
    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(5));
        toolbar.setStyle("-fx-border-color: #333; -fx-border-width: 0 0 1 0;");
        
        // Filter text field
        filterField = new TextField();
        filterField.setPromptText("Filter messages...");
        filterField.setPrefWidth(150);
        filterField.textProperty().addListener((obs, oldVal, newVal) -> refreshFilter());
        
        // Level filter
        levelFilter = new ComboBox<>();
        levelFilter.getItems().addAll("All", "Log", "Info", "Warn", "Error", "Debug");
        levelFilter.setValue("All");
        levelFilter.setPrefWidth(100);
        levelFilter.setOnAction(e -> refreshFilter());
        
        // Clear button
        Button clearButton = new Button("Clear");
        clearButton.setStyle("-fx-padding: 5 15;");
        clearButton.setOnAction(e -> clearConsole());
        
        // Copy button
        Button copyButton = new Button("Copy");
        copyButton.setStyle("-fx-padding: 5 15;");
        copyButton.setOnAction(e -> copyToClipboard());
        
        toolbar.getChildren().addAll(
            new Label("Level:"), levelFilter,
            new Label("Filter:"), filterField,
            clearButton, copyButton
        );
        
        return toolbar;
    }
    
    private void refreshFilter() {
        String filterText = filterField.getText().toLowerCase();
        String selectedLevel = levelFilter.getValue();
        
        List<ConsoleMessage> filtered = allMessages.stream()
            .filter(msg -> {
                boolean levelMatch = "All".equals(selectedLevel) || msg.level.equals(selectedLevel);
                boolean textMatch = msg.message.toLowerCase().contains(filterText);
                return levelMatch && textMatch;
            })
            .toList();
        
        messageList.getItems().clear();
        messageList.getItems().addAll(filtered);
        
        // Scroll to bottom
        if (!filtered.isEmpty()) {
            messageList.scrollTo(filtered.size() - 1);
        }
    }
    
    private void clearConsole() {
        allMessages.clear();
        messageList.getItems().clear();
    }
    
    private void copyToClipboard() {
        StringBuilder sb = new StringBuilder();
        for (ConsoleMessage msg : messageList.getItems()) {
            sb.append(String.format("[%s] %s: %s%n", msg.timestamp, msg.level, msg.message));
        }
        
        if (sb.length() > 0) {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(sb.toString());
            clipboard.setContent(content);
        }
    }
    
    /**
     * Add a message to the console
     * 
     * @param level The log level (log, info, warn, error, debug)
     * @param message The message to display
     */
    public void addMessage(String level, String message) {
        Platform.runLater(() -> {
            ConsoleMessage msg = new ConsoleMessage(level, message, LocalTime.now().format(timeFormatter));
            allMessages.add(msg);
            
            // Keep max 1000 messages to avoid memory issues
            if (allMessages.size() > 1000) {
                allMessages.remove(0);
            }
            
            refreshFilter();
        });
    }
    
    /**
     * Represents a single console message
     */
    public static class ConsoleMessage {
        public String level;
        public String message;
        public String timestamp;
        
        public ConsoleMessage(String level, String message, String timestamp) {
            this.level = level;
            this.message = message;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Custom cell renderer for console messages with syntax highlighting
     */
    private static class ConsoleMessageCell extends ListCell<ConsoleMessage> {
        private final Label label = new Label();
        
        @Override
        protected void updateItem(ConsoleMessage item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                String text = String.format("[%s] %s: %s", item.timestamp, item.level, item.message);
                label.setText(text);
                label.setWrapText(true);
                
                // Color code by level
                switch (item.level.toLowerCase()) {
                    case "error" -> label.setTextFill(Color.web("#ff6b6b"));
                    case "warn" -> label.setTextFill(Color.web("#ffd93d"));
                    case "info" -> label.setTextFill(Color.web("#6bcf7f"));
                    case "debug" -> label.setTextFill(Color.web("#888888"));
                    default -> label.setTextFill(Color.web("#e0e0e0"));
                }
                
                setGraphic(label);
            }
        }
    }
}
