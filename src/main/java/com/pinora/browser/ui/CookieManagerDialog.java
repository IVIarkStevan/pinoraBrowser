package com.pinora.browser.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.pinora.browser.core.CookieData;
import com.pinora.browser.core.CookieManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Cookie Manager Dialog for viewing, editing, and managing cookies
 */
public class CookieManagerDialog {
    
    private static final Logger logger = LoggerFactory.getLogger(CookieManagerDialog.class);
    
    private CookieManager cookieManager;
    private Stage stage;
    private TableView<CookieData> cookieTable;
    private ComboBox<String> domainFilter;
    private TextArea detailsArea;
    private Label statsLabel;
    private CheckBox blockTrackingCheckBox;
    private CheckBox blockThirdPartyCheckBox;
    
    public CookieManagerDialog(CookieManager cookieManager) {
        this.cookieManager = cookieManager;
    }
    
    public void show(Stage owner) {
        stage = new Stage();
        stage.setTitle("Cookie Manager - Pinora Browser");
        stage.setWidth(1000);
        stage.setHeight(700);
        stage.initOwner(owner);
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-font-size: 11;");
        
        // Top: Toolbar and filters
        root.setTop(createToolbar());
        
        // Center: Main content with table and details
        root.setCenter(createMainContent());
        
        // Bottom: Buttons
        root.setBottom(createButtonBar());
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        
        // Refresh the data
        refreshCookieTable();
    }
    
    private VBox createToolbar() {
        VBox toolbar = new VBox(8);
        toolbar.setPadding(new Insets(5, 0, 5, 0));
        
        // First row: Title and stats
        HBox titleBox = new HBox(10);
        Label titleLabel = new Label("Manage Cookies");
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        statsLabel = new Label();
        statsLabel.setStyle("-fx-text-fill: #666;");
        titleBox.getChildren().addAll(titleLabel, new Separator(javafx.geometry.Orientation.VERTICAL), statsLabel);
        toolbar.getChildren().add(titleBox);
        
        // Second row: Filters and actions
        HBox filterBox = new HBox(10);
        filterBox.setPadding(new Insets(5));
        filterBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 3; -fx-background-color: #f5f5f5;");
        
        Label filterLabel = new Label("Filter by domain:");
        domainFilter = new ComboBox<>();
        domainFilter.setPrefWidth(200);
        domainFilter.setPromptText("All domains");
        domainFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterCookies());
        
        filterBox.getChildren().addAll(filterLabel, domainFilter);
        HBox.setHgrow(domainFilter, Priority.SOMETIMES);
        toolbar.getChildren().add(filterBox);
        
        // Third row: Cookie policies
        HBox policiesBox = new HBox(15);
        policiesBox.setPadding(new Insets(5));
        policiesBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 3; -fx-background-color: #f5f5f5;");
        
        Label policiesLabel = new Label("Cookie Policies:");
        blockTrackingCheckBox = new CheckBox("Block tracking cookies");
        blockTrackingCheckBox.setSelected(cookieManager.isBlockTrackingCookies());
        blockTrackingCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            cookieManager.setBlockTrackingCookies(newVal));
        
        blockThirdPartyCheckBox = new CheckBox("Block third-party cookies");
        blockThirdPartyCheckBox.setSelected(cookieManager.isBlockThirdPartyCookies());
        blockThirdPartyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            cookieManager.setBlockThirdPartyCookies(newVal));
        
        policiesBox.getChildren().addAll(policiesLabel, blockTrackingCheckBox, blockThirdPartyCheckBox);
        toolbar.getChildren().add(policiesBox);
        
        return toolbar;
    }
    
    private BorderPane createMainContent() {
        BorderPane center = new BorderPane();
        center.setPadding(new Insets(5));
        
        // Left: Cookie table
        VBox tableBox = new VBox(5);
        Label tableLabel = new Label("Cookies:");
        tableLabel.setStyle("-fx-font-weight: bold;");
        
        cookieTable = new TableView<>();
        cookieTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        TableColumn<CookieData, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        nameCol.setPrefWidth(150);
        
        TableColumn<CookieData, String> domainCol = new TableColumn<>("Domain");
        domainCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDomain()));
        domainCol.setPrefWidth(200);
        
        TableColumn<CookieData, String> valueCol = new TableColumn<>("Value");
        valueCol.setCellValueFactory(cellData -> {
            String value = cellData.getValue().getValue();
            if (value.length() > 50) {
                return new javafx.beans.property.SimpleStringProperty(value.substring(0, 50) + "...");
            }
            return new javafx.beans.property.SimpleStringProperty(value);
        });
        valueCol.setPrefWidth(200);
        
        TableColumn<CookieData, String> expiryCol = new TableColumn<>("Expiry");
        expiryCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getExpiryTimeString()));
        expiryCol.setPrefWidth(150);
        
        cookieTable.getColumns().add(nameCol);
        cookieTable.getColumns().add(domainCol);
        cookieTable.getColumns().add(valueCol);
        cookieTable.getColumns().add(expiryCol);
        cookieTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displayCookieDetails(newVal);
            }
        });
        
        tableBox.getChildren().addAll(tableLabel, cookieTable);
        VBox.setVgrow(cookieTable, Priority.ALWAYS);
        
        // Right: Details panel
        VBox detailsBox = createDetailsPanel();
        
        // Split pane
        SplitPane splitPane = new SplitPane(tableBox, detailsBox);
        splitPane.setDividerPosition(0, 0.6);
        BorderPane.setMargin(splitPane, new Insets(0, 0, 10, 0));
        
        center.setCenter(splitPane);
        
        return center;
    }
    
    private VBox createDetailsPanel() {
        VBox detailsBox = new VBox(5);
        detailsBox.setPadding(new Insets(5));
        
        Label detailsLabel = new Label("Details:");
        detailsLabel.setStyle("-fx-font-weight: bold;");
        
        detailsArea = new TextArea();
        detailsArea.setWrapText(true);
        detailsArea.setStyle("-fx-control-inner-background: #f9f9f9; -fx-font-family: 'Monaco', 'Courier New', monospace; -fx-font-size: 10;");
        detailsArea.setEditable(false);
        
        detailsBox.getChildren().addAll(detailsLabel, detailsArea);
        VBox.setVgrow(detailsArea, Priority.ALWAYS);
        
        return detailsBox;
    }
    
    private HBox createButtonBar() {
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        buttonBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0; -fx-padding: 10;");
        
        Button deleteButton = new Button("Delete Selected");
        deleteButton.setStyle("-fx-padding: 8 15;");
        deleteButton.setOnAction(e -> deleteSelectedCookie());
        
        Button deleteAllButton = new Button("Delete All");
        deleteAllButton.setStyle("-fx-padding: 8 15;");
        deleteAllButton.setOnAction(e -> deleteAllCookies());
        
        Button deleteSessionButton = new Button("Delete Session Cookies");
        deleteSessionButton.setStyle("-fx-padding: 8 15;");
        deleteSessionButton.setOnAction(e -> deleteSessionCookies());
        
        Button exportButton = new Button("Export");
        exportButton.setStyle("-fx-padding: 8 15;");
        exportButton.setOnAction(e -> exportCookies());
        
        Button importButton = new Button("Import");
        importButton.setStyle("-fx-padding: 8 15;");
        importButton.setOnAction(e -> importCookies());
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-padding: 8 15;");
        refreshButton.setOnAction(e -> refreshCookieTable());
        
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-padding: 8 15;");
        closeButton.setOnAction(e -> stage.close());
        
        buttonBox.getChildren().addAll(
            deleteButton, deleteAllButton, deleteSessionButton, 
            new Separator(javafx.geometry.Orientation.VERTICAL),
            exportButton, importButton,
            new Separator(javafx.geometry.Orientation.VERTICAL),
            refreshButton, closeButton
        );
        
        return buttonBox;
    }
    
    private void displayCookieDetails(CookieData cookie) {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(cookie.getName()).append("\n\n");
        sb.append("Value: ").append(cookie.getValue()).append("\n\n");
        sb.append("Domain: ").append(cookie.getDomain()).append("\n");
        sb.append("Path: ").append(cookie.getPath()).append("\n\n");
        sb.append("Secure: ").append(cookie.isSecure()).append("\n");
        sb.append("HTTP Only: ").append(cookie.isHttpOnly()).append("\n");
        sb.append("Session Only: ").append(cookie.isSessionOnly()).append("\n");
        sb.append("SameSite: ").append(cookie.getSameSite()).append("\n\n");
        sb.append("Expiry: ").append(cookie.getExpiryTimeString()).append("\n");
        sb.append("Created: ").append(new java.util.Date(cookie.getCreatedTime())).append("\n");
        sb.append("Last Accessed: ").append(new java.util.Date(cookie.getLastAccessedTime())).append("\n\n");
        sb.append("Expired: ").append(cookie.isExpired()).append("\n");
        
        detailsArea.setText(sb.toString());
    }
    
    private void filterCookies() {
        refreshCookieTable();
    }
    
    private void refreshCookieTable() {
        Platform.runLater(() -> {
            ObservableList<CookieData> items = FXCollections.observableArrayList();
            
            String selectedDomain = domainFilter.getValue();
            
            if (selectedDomain == null || selectedDomain.isEmpty()) {
                items.addAll(cookieManager.getAllValidCookies());
            } else {
                items.addAll(cookieManager.getCookiesForDomain(selectedDomain));
            }
            
            cookieTable.setItems(items);
            
            // Update domain filter options
            List<String> domains = cookieManager.getAllDomains();
            ObservableList<String> domainItems = FXCollections.observableArrayList();
            domainItems.add(""); // Empty option for "All domains"
            domainItems.addAll(domains);
            String current = domainFilter.getValue();
            domainFilter.setItems(domainItems);
            domainFilter.setValue(current);
            
            // Update statistics
            updateStatistics();
        });
    }
    
    private void updateStatistics() {
        Map<String, Object> stats = cookieManager.getCookieStatistics();
        String statsText = String.format(
            "Total: %d | Session: %d | Persistent: %d | Secure: %d | Domains: %d",
            stats.get("totalCookies"),
            stats.get("sessionCookies"),
            stats.get("persistentCookies"),
            stats.get("secureCookies"),
            stats.get("uniqueDomains")
        );
        statsLabel.setText(statsText);
    }
    
    private void deleteSelectedCookie() {
        CookieData selected = cookieTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText("Delete Cookie?");
            confirm.setContentText("Are you sure you want to delete this cookie?\n" + selected.getName());
            
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                cookieManager.deleteCookie(selected.getDomain(), selected.getPath(), selected.getName());
                refreshCookieTable();
            }
        }
    }
    
    private void deleteAllCookies() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete All");
        confirm.setHeaderText("Delete All Cookies?");
        confirm.setContentText("This will permanently delete all cookies. This action cannot be undone.");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            cookieManager.deleteAllCookies();
            refreshCookieTable();
            detailsArea.clear();
        }
    }
    
    private void deleteSessionCookies() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete All Session Cookies?");
        confirm.setContentText("This will delete all session cookies (non-persistent cookies).");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            cookieManager.deleteSessionCookies();
            refreshCookieTable();
            detailsArea.clear();
        }
    }
    
    private void exportCookies() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Cookies");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));
        fileChooser.setInitialFileName("pinora-cookies.json");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        
        File selectedFile = fileChooser.showSaveDialog(stage);
        if (selectedFile != null) {
            try {
                String json = cookieManager.exportCookiesToJSON();
                try (FileWriter writer = new FileWriter(selectedFile)) {
                    writer.write(json);
                }
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText("Cookies Exported");
                alert.setContentText("Cookies exported to: " + selectedFile.getAbsolutePath());
                alert.showAndWait();
                
                logger.info("Cookies exported to: {}", selectedFile.getAbsolutePath());
            } catch (Exception e) {
                logger.error("Failed to export cookies", e);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Export Failed");
                alert.setHeaderText("Error");
                alert.setContentText("Failed to export cookies: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
    
    private void importCookies() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Cookies");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                String json = new String(Files.readAllBytes(Paths.get(selectedFile.getAbsolutePath())));
                int imported = cookieManager.importCookiesFromJSON(json);
                
                refreshCookieTable();
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Import Successful");
                alert.setHeaderText("Cookies Imported");
                alert.setContentText("Successfully imported " + imported + " cookies.");
                alert.showAndWait();
                
                logger.info("Imported {} cookies from: {}", imported, selectedFile.getAbsolutePath());
            } catch (Exception e) {
                logger.error("Failed to import cookies", e);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Import Failed");
                alert.setHeaderText("Error");
                alert.setContentText("Failed to import cookies: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
}
