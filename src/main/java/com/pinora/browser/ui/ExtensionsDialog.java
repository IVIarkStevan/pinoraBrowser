package com.pinora.browser.ui;

import com.pinora.browser.extensions.ExtensionManager;
import com.pinora.browser.extensions.webext.WebExtensionLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

/**
 * Dialog to manage both legacy (Extension interface) and WebExtensions.
 */
public class ExtensionsDialog {

    private final BrowserWindow window;
    private final ExtensionManager manager;
    private final WebExtensionLoader webExtLoader;

    public ExtensionsDialog(BrowserWindow window, ExtensionManager manager) {
        this.window = window;
        this.manager = manager;
        this.webExtLoader = new WebExtensionLoader();
    }

    public void showAndWait() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Manage Extensions");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Create tabs for Legacy and WebExtensions
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Legacy Extensions Tab
        Tab legacyTab = new Tab("Legacy Extensions", createLegacyExtensionsPanel());
        legacyTab.setDisable(false);

        // WebExtensions Tab
        Tab webExtTab = new Tab("WebExtensions", createWebExtensionsPanel());
        webExtTab.setDisable(false);

        tabPane.getTabs().addAll(legacyTab, webExtTab);

        // Bottom buttons
        Button close = new Button("Close");
        close.setOnAction(e -> stage.close());
        HBox buttons = new HBox(8, close);
        buttons.setPadding(new Insets(8, 0, 0, 0));

        root.setCenter(tabPane);
        root.setBottom(buttons);

        Scene scene = new Scene(root, 700, 450);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private VBox createLegacyExtensionsPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(10));

        Label infoLabel = new Label("Legacy Java Extension Plugins");
        infoLabel.setStyle("-fx-font-weight: bold;");

        TableView<ExtensionManager.ExtensionDescriptor> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ExtensionManager.ExtensionDescriptor, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDisplayName()));

        TableColumn<ExtensionManager.ExtensionDescriptor, String> jarCol = new TableColumn<>("Jar");
        jarCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getJarName()));

        TableColumn<ExtensionManager.ExtensionDescriptor, String> enabledCol = new TableColumn<>("Enabled");
        enabledCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().isEnabled() ? "Yes" : "No"));

        TableColumn<ExtensionManager.ExtensionDescriptor, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().isLoaded() ? "Loaded" : "Not loaded"));

        table.getColumns().addAll(nameCol, jarCol, enabledCol, statusCol);

        List<ExtensionManager.ExtensionDescriptor> items = manager.listExtensions();
        ObservableList<ExtensionManager.ExtensionDescriptor> data = FXCollections.observableArrayList(items);
        table.setItems(data);

        Button enableBtn = new Button("Enable");
        Button disableBtn = new Button("Disable");
        Button refresh = new Button("Refresh");

        enableBtn.setOnAction(e -> {
            ExtensionManager.ExtensionDescriptor sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                manager.enableExtension(sel.getJarName(), window);
                table.getItems().setAll(manager.listExtensions());
            }
        });

        disableBtn.setOnAction(e -> {
            ExtensionManager.ExtensionDescriptor sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                manager.disableExtension(sel.getJarName());
                table.getItems().setAll(manager.listExtensions());
            }
        });

        refresh.setOnAction(e -> table.getItems().setAll(manager.listExtensions()));

        HBox buttons = new HBox(8, enableBtn, disableBtn, refresh);
        buttons.setPadding(new Insets(8, 0, 0, 0));

        panel.getChildren().addAll(infoLabel, table, buttons);
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

        return panel;
    }

    private VBox createWebExtensionsPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(10));

        Label infoLabel = new Label("Firefox/Chrome Compatible WebExtensions");
        infoLabel.setStyle("-fx-font-weight: bold;");

        // Load WebExtensions
        webExtLoader.loadAll();
        List<WebExtensionLoader.ExtensionInfo> infos = webExtLoader.getExtensionInfos();

        if (infos.isEmpty()) {
            Label emptyLabel = new Label("No WebExtensions installed.\nPlace WebExtension folders in: webextensions/");
            emptyLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12;");
            panel.getChildren().addAll(infoLabel, emptyLabel);
            return panel;
        }

        VBox extensionsList = new VBox(10);
        extensionsList.setPadding(new Insets(5));

        for (WebExtensionLoader.ExtensionInfo info : infos) {
            HBox extCard = createExtensionCard(info);
            extensionsList.getChildren().add(extCard);
        }

        ScrollPane scroll = new ScrollPane(extensionsList);
        scroll.setFitToWidth(true);

        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> {
            webExtLoader.loadAll();
            // Refresh the panel
        });

        HBox buttons = new HBox(8, refresh);
        buttons.setPadding(new Insets(8, 0, 0, 0));

        panel.getChildren().addAll(infoLabel, scroll, buttons);
        VBox.setVgrow(scroll, javafx.scene.layout.Priority.ALWAYS);

        return panel;
    }

    private HBox createExtensionCard(WebExtensionLoader.ExtensionInfo info) {
        HBox card = new HBox(10);
        card.setPadding(new Insets(8));
        card.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: #f8f8f8;");

        // Icon
        ImageView iconView = new ImageView();
        iconView.setFitWidth(48);
        iconView.setFitHeight(48);
        if (info.iconMedium != null) {
            try {
                iconView.setImage(new Image(info.iconMedium));
            } catch (Exception e) {
                // Icon not available
            }
        }

        // Details
        VBox details = new VBox(2);
        Label name = new Label(info.name + " v" + info.version);
        name.setStyle("-fx-font-weight: bold;");
        Label desc = new Label(info.description != null ? info.description : "(No description)");
        desc.setStyle("-fx-font-size: 11; -fx-text-fill: #666666;");
        Label id = new Label("ID: " + info.id.substring(0, 8) + "...");
        id.setStyle("-fx-font-size: 10; -fx-text-fill: #999999;");
        details.getChildren().addAll(name, desc, id);

        // Status
        Label status = new Label("Enabled");
        status.setStyle("-fx-font-size: 11; -fx-text-fill: #0066cc;");

        card.getChildren().addAll(iconView, details);
        HBox.setHgrow(details, javafx.scene.layout.Priority.ALWAYS);
        card.getChildren().add(status);

        return card;
    }
}
