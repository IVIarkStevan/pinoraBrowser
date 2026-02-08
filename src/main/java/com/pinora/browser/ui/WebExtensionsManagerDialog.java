package com.pinora.browser.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.pinora.browser.extensions.ExtensionManager;
import com.pinora.browser.extensions.webext.installer.WebExtensionInstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Dialog to install/uninstall WebExtensions from ZIPs or GitHub releases.
 */
public class WebExtensionsManagerDialog extends Dialog<Void> {

    private static final Logger logger = LoggerFactory.getLogger(WebExtensionsManagerDialog.class);

    private final ExtensionManager extensionManager;
    private final WebExtensionInstaller installer;
    private final com.pinora.browser.extensions.webext.WebExtensionLoader webExtLoader = new com.pinora.browser.extensions.webext.WebExtensionLoader();
    private final Stage owner;

    private ListView<String> listView;

    public WebExtensionsManagerDialog(Stage owner, ExtensionManager extensionManager, WebExtensionInstaller installer) {
        this.owner = owner;
        this.extensionManager = extensionManager;
        this.installer = installer;

        setTitle("WebExtensions Manager");
        initOwner(owner);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        listView = new ListView<>();
        listView.setPrefSize(600, 300);
        refreshList();

        root.setCenter(listView);

        // Controls
        HBox controls = new HBox(8);
        controls.setPadding(new Insets(8,0,0,0));

        Button installZip = new Button("Install from ZIP / XPI...");
        installZip.setOnAction(e -> onInstallZip());

        Button installGit = new Button("Install from GitHub URL");
        installGit.setOnAction(e -> onInstallFromGit());

        Button uninstall = new Button("Uninstall");
        uninstall.setOnAction(e -> onUninstall());

        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> refreshList());

        Button close = new Button("Close");
        close.setOnAction(e -> this.close());

        controls.getChildren().addAll(installZip, installGit, uninstall, refresh, close);
        root.setBottom(controls);

        getDialogPane().setContent(root);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    }

    private void refreshList() {
        try {
            Path webExtRoot = installer.getWebExtensionsRoot();
            if (!Files.exists(webExtRoot)) Files.createDirectories(webExtRoot);

            listView.getItems().setAll(
                Files.list(webExtRoot)
                    .filter(Files::isDirectory)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList())
            );
        } catch (Exception e) {
            logger.warn("Failed to list webextensions: {}", e.getMessage());
        }
    }

    private void onInstallZip() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select WebExtension ZIP");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Extension Files", "*.zip", "*.xpi"));
        File file = chooser.showOpenDialog(owner);
        if (file != null) {
            try {
                String id = installer.installFromZip(file);
                // Attempt to load the newly installed extension so it's immediately available
                try {
                    webExtLoader.loadExtension(installer.getWebExtensionsRoot().resolve(id).toFile());
                } catch (Exception ex) {
                    logger.warn("Failed to load installed extension {}: {}", id, ex.getMessage());
                }
                refreshList();
                Alert a = new Alert(Alert.AlertType.INFORMATION, "Installed: " + id, ButtonType.OK);
                a.showAndWait();
            } catch (Exception e) {
                logger.warn("Install from ZIP failed: {}", e.getMessage());
                Alert a = new Alert(Alert.AlertType.ERROR, "Install failed: " + e.getMessage(), ButtonType.OK);
                a.showAndWait();
            }
        }
    }

    private void onInstallFromGit() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Install from GitHub");
        dialog.setHeaderText("Enter the direct URL to a ZIP (GitHub release asset or repo archive)");
        dialog.setContentText("URL:");
        dialog.showAndWait().ifPresent(url -> {
            try {
                String id = installer.installFromURL(url.trim());
                refreshList();
                Alert a = new Alert(Alert.AlertType.INFORMATION, "Installed: " + id, ButtonType.OK);
                a.showAndWait();
            } catch (Exception e) {
                logger.warn("Install from URL failed: {}", e.getMessage());
                Alert a = new Alert(Alert.AlertType.ERROR, "Install failed: " + e.getMessage(), ButtonType.OK);
                a.showAndWait();
            }
        });
    }

    private void onUninstall() {
        String selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Uninstall extension '" + selected + "'?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                try {
                    installer.uninstall(selected);
                    refreshList();
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Uninstalled: " + selected, ButtonType.OK);
                    a.showAndWait();
                } catch (Exception e) {
                    logger.warn("Uninstall failed: {}", e.getMessage());
                    Alert a = new Alert(Alert.AlertType.ERROR, "Uninstall failed: " + e.getMessage(), ButtonType.OK);
                    a.showAndWait();
                }
            }
        });
    }
}
