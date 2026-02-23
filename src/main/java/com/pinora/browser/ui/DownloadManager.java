package com.pinora.browser.ui;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Download manager UI with support for Save-as, progress, pause/resume/cancel and auto-rename.
 */
public class DownloadManager {

    private static final Logger logger = LoggerFactory.getLogger(DownloadManager.class);

    private ListView<DownloadEntry> downloadList;
    private Stage stage;
    private VBox view;

    // track active tasks by id
    private final ConcurrentHashMap<DownloadEntry, Thread> activeThreads = new ConcurrentHashMap<>();

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

    /** Start a download with a Save-as dialog. */
    public void startDownload(String urlStr, Stage owner) {
        try {
            URL url = URI.create(urlStr).toURL();
            String guessed = new File(url.getPath()).getName();
            if (guessed.isEmpty()) guessed = "download";

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save As");
            chooser.setInitialFileName(guessed);
            File target = chooser.showSaveDialog(owner);
            if (target == null) return;

            // handle duplicate filename by auto-rename
            target = resolveDuplicate(target.toPath()).toFile();

            DownloadEntry entry = new DownloadEntry(target.getName(), urlStr, target.toPath());
            Platform.runLater(() -> {
                if (downloadList == null) ensureView();
                downloadList.getItems().add(entry);
            });

            startDownloadTask(entry);
        } catch (Exception e) {
            logger.warn("Failed to start download: {}", e.getMessage());
        }
    }

    private Path resolveDuplicate(Path target) {
        Path p = target;
        int idx = 1;
        while (Files.exists(p)) {
            String name = target.getFileName().toString();
            int dot = name.lastIndexOf('.');
            String base = (dot == -1) ? name : name.substring(0, dot);
            String ext = (dot == -1) ? "" : name.substring(dot);
            String newName = base + " (" + idx + ")" + ext;
            p = target.getParent().resolve(newName);
            idx++;
        }
        return p;
    }

    private void startDownloadTask(DownloadEntry entry) {
        Thread t = new Thread(() -> {
            try {
                entry.setStatus("Downloading");
                URL url = URI.create(entry.url).toURL();
                Path tmp = entry.target.resolveSibling(entry.target.getFileName().toString() + ".part");

                long existing = Files.exists(tmp) ? Files.size(tmp) : 0L;

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (existing > 0) {
                    conn.setRequestProperty("Range", "bytes=" + existing + "-");
                }
                conn.setRequestProperty("User-Agent", "PinoraBrowser/1.0");
                conn.connect();

                int response = conn.getResponseCode();
                long contentLength = conn.getHeaderFieldLong("Content-Length", -1);
                boolean acceptRanges = "bytes".equalsIgnoreCase(conn.getHeaderField("Accept-Ranges"));
                long total = (contentLength > 0 && existing > 0 && response == 206) ? existing + contentLength : (contentLength > 0 ? contentLength : -1);
                entry.setTotalSize(total);

                try (RandomAccessFile raf = new RandomAccessFile(tmp.toFile(), "rw")) {
                    if (existing > 0) raf.seek(existing);
                    byte[] buf = new byte[8192];
                    int r;
                    long downloaded = existing;
                    try (var in = conn.getInputStream()) {
                        while ((r = in.read(buf)) != -1) {
                            if (entry.isCanceled()) {
                                entry.setStatus("Canceled");
                                break;
                            }
                            while (entry.isPaused()) {
                                Thread.sleep(200);
                                if (entry.isCanceled()) break;
                            }
                            raf.write(buf, 0, r);
                            downloaded += r;
                            final long dl = downloaded;
                            if (total > 0) entry.setProgress((double) dl / total);
                            entry.setDownloaded(dl);
                        }
                    }
                    if (!entry.isCanceled()) {
                        // move .part to final file (overwrite if exists)
                        Files.move(tmp, entry.target, StandardCopyOption.REPLACE_EXISTING);
                        entry.setStatus("Completed");
                        entry.setProgress(1.0);
                    } else {
                        try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
                    }
                }
            } catch (Exception e) {
                logger.warn("Download failed: {}", e.getMessage());
                entry.setStatus("Error: " + e.getMessage());
            } finally {
                activeThreads.remove(entry);
            }
        }, "download-" + entry.name);
        activeThreads.put(entry, t);
        t.start();
    }

    public void addDownload(String filename, long size) {
        if (downloadList == null) ensureView();
        DownloadEntry item = new DownloadEntry(filename, "", null);
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

        downloadList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(DownloadEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label name = new Label();
                name.textProperty().bind(item.nameProperty());
                ProgressBar pb = new ProgressBar();
                pb.setPrefWidth(180);
                pb.progressProperty().bind(item.progressProperty());
                Label pct = new Label();
                pct.textProperty().bind(item.progressProperty().multiply(100).asString("%.0f%%"));
                Button pause = new Button();
                pause.setText(item.isPaused() ? "Resume" : "Pause");
                pause.setOnAction(e -> {
                    if (item.isPaused()) item.resume(); else item.pause();
                });
                Button cancel = new Button("Cancel");
                cancel.setOnAction(e -> item.cancel());
                HBox box = new HBox(8, name, pb, pct, pause, cancel);
                box.setPadding(new Insets(6));
                setGraphic(box);
            }
        });

        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center-right;");
        Button clearButton = new Button("Clear List");
        clearButton.setOnAction(e -> downloadList.getItems().removeIf(d -> d.getStatus().equals("Completed") || d.getStatus().startsWith("Error") || d.getStatus().equals("Canceled")));
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> {
            if (stage != null) stage.close();
        });
        buttonBox.getChildren().addAll(clearButton, closeButton);

        view.getChildren().addAll(titleLabel, downloadList, buttonBox);
        VBox.setVgrow(downloadList, javafx.scene.layout.Priority.ALWAYS);
    }

    public static class DownloadEntry {
        private final StringProperty name = new SimpleStringProperty();
        private final String url;
        private final Path target;
        private volatile boolean paused = false;
        private volatile boolean canceled = false;
        private final DoubleProperty progress = new SimpleDoubleProperty(0);
        private volatile long downloaded = 0;
        private volatile long total = -1;
        private final StringProperty status = new SimpleStringProperty("Queued");

        public DownloadEntry(String name, String url, Path target) {
            this.name.set(name);
            this.url = url;
            this.target = target;
        }

        public String getName() { return name.get(); }
        public StringProperty nameProperty() { return name; }
        public double getProgress() { return progress.get(); }
        public DoubleProperty progressProperty() { return progress; }
        public void setProgress(double p) { Platform.runLater(() -> this.progress.set(p)); }
        public boolean isPaused() { return paused; }
        public void pause() { paused = true; status.set("Paused"); }
        public void resume() { paused = false; status.set("Downloading"); }
        public void cancel() { canceled = true; status.set("Canceled"); }
        public boolean isCanceled() { return canceled; }
        public void setDownloaded(long d) { this.downloaded = d; }
        public void setTotalSize(long t) { this.total = t; }
        public String getStatus() { return status.get(); }
        public StringProperty statusProperty() { return status; }
        public Path getTarget() { return target; }
        public String getUrl() { return url; }
        public void setStatus(String s) { Platform.runLater(() -> status.set(s)); }
        public void setDownloadedAndProgress(long d) { setDownloaded(d); if (total>0) setProgress((double)d/total); }
        public void setDownloadedSilent(long d) { this.downloaded = d; }
        public SimpleStringProperty pausedProperty() { return new SimpleStringProperty(paused ? "true" : "false"); }
    }
}
