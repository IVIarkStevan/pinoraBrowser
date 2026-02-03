package com.pinora.browser.test;

import com.pinora.browser.ui.BrowserWindow;
import com.pinora.browser.ui.ExtensionsDialog;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Simple launcher to visually test that the Extensions option opens.
 * It disables auto-loading of extensions so the dialog is safe to open.
 */
public class OpenExtensionsLauncher extends Application {

    public static void main(String[] args) {
        // Disable auto-loading of extension JARs so the dialog can be inspected
        System.setProperty("pinora.extensions.autoload", "false");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        BrowserWindow browserWindow = new BrowserWindow();
        browserWindow.show(primaryStage);

        // Open the Extensions dialog for manual verification
        new ExtensionsDialog(browserWindow, browserWindow.getExtensionManager()).showAndWait();

        // Close after inspection
        primaryStage.close();
    }
}
