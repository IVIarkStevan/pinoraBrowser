package com.pinora.browser.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Download manager UI
 */
public class DownloadManager {
    
    private static final Logger logger = LoggerFactory.getLogger(DownloadManager.class);
    
    private ListView<DownloadItem> downloadList;
    private Stage stage;
    
    public void show(Stage owner) {
        if (stage == null) {
            stage = new Stage();
            stage.setTitle("Downloads - Pinora Browser");
            stage.setWidth(700);
            stage.setHeight(400);
            stage.initOwner(owner);
            
            VBox root = new VBox(10);
            root.setPadding(new Insets(10));
            
            Label titleLabel = new Label("Downloads");
            titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
            
            downloadList = new ListView<>();
            downloadList.setStyle("-fx-control-inner-background: #f5f5f5;");
            
            HBox buttonBox = new HBox(10);
            buttonBox.setStyle("-fx-alignment: center-right;");
            Button clearButton = new Button("Clear List");
            Button closeButton = new Button("Close");
            closeButton.setOnAction(e -> stage.close());
            buttonBox.getChildren().addAll(clearButton, closeButton);
            
            root.getChildren().addAll(titleLabel, downloadList, buttonBox);
            VBox.setVgrow(downloadList, javafx.scene.layout.Priority.ALWAYS);
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
        }
        
        stage.show();
    }
    
    public void addDownload(String filename, long size) {
        DownloadItem item = new DownloadItem(filename, size);
        downloadList.getItems().add(item);
        logger.info("Download added: {}", filename);
    }
    
    private static class DownloadItem {
        String filename;
        long size;
        
        DownloadItem(String filename, long size) {
            this.filename = filename;
            this.size = size;
        }
        
        @Override
        public String toString() {
            return filename + " (" + formatSize(size) + ")";
        }
        
        private static String formatSize(long bytes) {
            if (bytes <= 0) return "0 B";
            final String[] units = new String[]{"B", "KB", "MB", "GB"};
            int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
            return String.format("%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
        }
    }
}
