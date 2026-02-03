package com.pinora.browser.extensions.webext.installer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Minimal installer for WebExtensions: installs ZIPs or downloads from a URL and unpacks
 */
public class WebExtensionInstaller {

    private static final Logger logger = LoggerFactory.getLogger(WebExtensionInstaller.class);

    private final Path webExtRoot;

    public WebExtensionInstaller() {
        this.webExtRoot = detectDefaultWebExtensionsFolder();
        try {
            if (!Files.exists(webExtRoot)) Files.createDirectories(webExtRoot);
        } catch (Exception e) {
            throw new RuntimeException("Could not create webextensions folder: " + e.getMessage(), e);
        }
    }

    private Path detectDefaultWebExtensionsFolder() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        try {
            if (os.contains("win")) {
                String appdata = System.getenv("APPDATA");
                if (appdata != null && !appdata.isEmpty()) {
                    return Paths.get(appdata, "PinoraBrowser", "webextensions");
                }
                return Paths.get(userHome, "AppData", "Roaming", "PinoraBrowser", "webextensions");
            } else if (os.contains("mac")) {
                return Paths.get(userHome, "Library", "Application Support", "PinoraBrowser", "webextensions");
            } else {
                // Linux and others
                return Paths.get(userHome, ".local", "share", "pinora-browser", "webextensions");
            }
        } catch (Exception e) {
            // Fallback to working directory
            return Paths.get(System.getProperty("user.dir"), "webextensions");
        }
    }

    public Path getWebExtensionsRoot() {
        return webExtRoot;
    }

    public String installFromZip(File zipFile) throws Exception {
        if (zipFile == null || !zipFile.exists()) throw new IllegalArgumentException("ZIP file not found");
        String id = "ext-" + UUID.randomUUID().toString().substring(0,8);
        Path dest = webExtRoot.resolve(id);
        Files.createDirectories(dest);
        unzipTo(zipFile.toPath(), dest);
        logger.info("Installed extension {} from {}", id, zipFile.getName());
        return id;
    }

    public String installFromURL(String urlStr) throws Exception {
        if (urlStr == null || urlStr.isEmpty()) throw new IllegalArgumentException("URL is empty");
        // Download to temp file
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        conn.connect();
        int code = conn.getResponseCode();
        if (code >= 400) throw new RuntimeException("Download failed: HTTP " + code);

        Path tmp = Files.createTempFile("we-download", ".zip");
        try (InputStream in = new BufferedInputStream(conn.getInputStream()); FileOutputStream out = new FileOutputStream(tmp.toFile())) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) != -1) out.write(buf, 0, r);
        }

        try {
            return installFromZip(tmp.toFile());
        } finally {
            try { Files.deleteIfExists(tmp); } catch (Exception ignored) {}
        }
    }

    public void uninstall(String id) throws Exception {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("Invalid id");
        Path target = webExtRoot.resolve(id);
        if (!Files.exists(target)) throw new IllegalArgumentException("Extension not found: " + id);
        // Recursively delete
        Files.walk(target)
            .sorted((a,b) -> b.compareTo(a))
            .forEach(p -> {
                try { Files.deleteIfExists(p); } catch (Exception e) { logger.warn("Failed to delete {}: {}", p, e.getMessage()); }
            });
        logger.info("Uninstalled extension {}", id);
    }

    private void unzipTo(Path zip, Path dest) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path out = dest.resolve(entry.getName()).normalize();
                if (!out.startsWith(dest)) throw new RuntimeException("Zip entry outside target: " + entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(out);
                } else {
                    if (out.getParent() != null) Files.createDirectories(out.getParent());
                    try (FileOutputStream fos = new FileOutputStream(out.toFile())) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zis.read(buffer)) > 0) fos.write(buffer, 0, len);
                    }
                }
            }
        }
    }
}
