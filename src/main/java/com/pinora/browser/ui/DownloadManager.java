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
    private VBox view;
    
    public void show(Stage owner) {
        if (stage == null) {
            stage = new Stage();
            stage.setTitle("Downloads - Pinora Browser");
            stage.setWidth(700);
            stage.setHeight(400);
            stage.initOwner(owner);
            stage.setScene(new Scene(getView()));
        }
        stage.show();
    }
    
    public void addDownload(String filename, long size) {
        if (downloadList == null) ensureView();
        DownloadItem item = new DownloadItem(filename, size);
        downloadList.getItems().add(item);
        logger.info("Download added: {}", filename);
    }

    /**
     * Return an embeddable view for use in a Tab or other container.
     */
    public VBox getView() {
        if (view == null) ensureView();
        return view;
    }

    private void ensureView() {
        view = new VBox(10);
        view.setPadding(new Insets(10));

        Label titleLabel = new Label("Downloads");
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        downloadList = new ListView<>();
        downloadList.setStyle("-fx-control-inner-background: #f5f5f5;");

        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center-right;");
        Button clearButton = new Button("Clear List");
        clearButton.setOnAction(e -> downloadList.getItems().clear());
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> {
            if (stage != null) stage.close();
        });
        buttonBox.getChildren().addAll(clearButton, closeButton);

        view.getChildren().addAll(titleLabel, downloadList, buttonBox);
        VBox.setVgrow(downloadList, javafx.scene.layout.Priority.ALWAYS);
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
