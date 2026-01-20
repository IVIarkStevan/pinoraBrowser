package com.pinora.browser;

import javafx.application.Application;
import javafx.stage.Stage;
import com.pinora.browser.ui.BrowserWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for Pinora Browser application
 */
public class PinoraBrowser extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(PinoraBrowser.class);
    
    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting Pinora Browser...");
            
            BrowserWindow browserWindow = new BrowserWindow();
            browserWindow.show(primaryStage);
            
            logger.info("Pinora Browser started successfully");
        } catch (Exception e) {
            logger.error("Error starting Pinora Browser", e);
            System.exit(1);
        }
    }
    
    public static void main(String[] args) {
        logger.info("Pinora Browser Application Launched");
        launch(args);
    }
}
