package com.pinora.browser.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.pinora.browser.extensions.webext.WebExtensionLoader;
import com.pinora.browser.extensions.webext.WebExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays installed extension icons in the browser toolbar.
 * Allows users to click extension icons to view their popup or perform their action.
 */
public class ExtensionIconBar extends HBox {
    private static final Logger logger = LoggerFactory.getLogger(ExtensionIconBar.class);
    private final WebExtensionLoader extensionLoader;

    public ExtensionIconBar(WebExtensionLoader extensionLoader) {
        this.extensionLoader = extensionLoader;
        setSpacing(4);
        setStyle("-fx-padding: 0;");
        setAlignment(Pos.CENTER_RIGHT);
        
        if (extensionLoader != null) {
            loadExtensionIcons();
        }
    }

    private void loadExtensionIcons() {
        for (WebExtensionContext ctx : extensionLoader.getAllExtensions()) {
            try {
                com.pinora.browser.extensions.webext.WebExtensionManifest manifest = ctx.getManifest();
                
                // Check if extension has a popup/action
                if (manifest.getDefaultPopup() != null && !manifest.getDefaultPopup().isEmpty()) {
                    Button extButton = createExtensionButton(ctx, manifest);
                    if (extButton != null) {
                        getChildren().add(extButton);
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to load extension icon for {}: {}", ctx.getExtensionId(), e.getMessage());
            }
        }
    }

    private Button createExtensionButton(WebExtensionContext ctx, com.pinora.browser.extensions.webext.WebExtensionManifest manifest) {
        try {
            Button btn = new Button();
            btn.setPrefSize(32, 32);
            btn.setStyle("-fx-padding: 2; -fx-font-size: 11;");
            btn.setTooltip(new javafx.scene.control.Tooltip(manifest.getName()));

            // Try to load extension icon from manifest icons
            if (manifest.getIcons() != null && !manifest.getIcons().isEmpty()) {
                String iconPath = manifest.getIcons().values().iterator().next();
                try {
                    Image icon = ctx.loadImage(iconPath);
                    if (icon != null) {
                        ImageView imageView = new ImageView(icon);
                        imageView.setFitWidth(24);
                        imageView.setFitHeight(24);
                        btn.setGraphic(imageView);
                    } else {
                        btn.setText("E");
                    }
                } catch (Exception e) {
                    btn.setText(manifest.getName().substring(0, 1).toUpperCase());
                }
            } else {
                // Use first letter as icon if no image available
                btn.setText(manifest.getName().substring(0, 1).toUpperCase());
            }

            // On click, show extension popup or perform action
            btn.setOnAction(e -> onExtensionClicked(ctx, manifest));

            return btn;
        } catch (Exception e) {
            logger.warn("Failed to create extension button: {}", e.getMessage());
            return null;
        }
    }

    private void onExtensionClicked(WebExtensionContext ctx, com.pinora.browser.extensions.webext.WebExtensionManifest manifest) {
        try {
            showExtensionPopup(ctx, manifest.getDefaultPopup());
        } catch (Exception e) {
            logger.warn("Failed to open extension popup: {}", e.getMessage());
        }
    }

    private void showExtensionPopup(WebExtensionContext ctx, String popupPath) {
        try {
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(ctx.getManifest().getName());
            stage.setWidth(300);
            stage.setHeight(300);

            javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
            String popupContent = ctx.loadResource(popupPath);
            
            if (popupContent != null) {
                webView.getEngine().load("data:text/html;charset=utf-8," + 
                    java.net.URLEncoder.encode(popupContent, "UTF-8").replace("+", "%20"));
                
                javafx.scene.Scene scene = new javafx.scene.Scene(webView);
                stage.setScene(scene);
                stage.show();
                logger.info("Opened extension popup: {}", popupPath);
            }
        } catch (Exception e) {
            logger.warn("Failed to load extension popup {}: {}", popupPath, e.getMessage());
        }
    }
}

