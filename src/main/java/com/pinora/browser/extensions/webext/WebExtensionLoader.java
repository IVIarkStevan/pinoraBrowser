package com.pinora.browser.extensions.webext;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads WebExtensions from directories containing manifest.json.
 */
public class WebExtensionLoader {

    private static final Logger logger = LoggerFactory.getLogger(WebExtensionLoader.class);
    private static final Gson gson = new Gson();

    private File extensionsDir;
    private Map<String, WebExtensionContext> loadedExtensions = new HashMap<>();

    public WebExtensionLoader() {
        this.extensionsDir = new File("webextensions");
    }

    /**
     * Discover and load all WebExtensions from the webextensions/ directory.
     */
    public void loadAll() {
        if (!extensionsDir.exists()) {
            boolean created = extensionsDir.mkdirs();
            if (created) {
                logger.info("Created webextensions directory at {}", extensionsDir.getAbsolutePath());
            }
            logger.info("No WebExtensions found (directory does not exist)");
            return;
        }

        File[] subdirs = extensionsDir.listFiles(File::isDirectory);
        if (subdirs == null || subdirs.length == 0) {
            logger.info("No WebExtension subdirectories found in {}", extensionsDir.getAbsolutePath());
            return;
        }

        for (File dir : subdirs) {
            try {
                loadExtension(dir);
            } catch (Exception e) {
                logger.warn("Failed to load WebExtension from {}: {}", dir.getName(), e.getMessage());
            }
        }

        logger.info("Loaded {} WebExtension(s)", loadedExtensions.size());
    }

    /**
     * Load a single WebExtension from a directory.
     */
    public WebExtensionContext loadExtension(File extensionDir) throws Exception {
        File manifestFile = new File(extensionDir, "manifest.json");
        if (!manifestFile.exists()) {
            throw new IllegalArgumentException("manifest.json not found in " + extensionDir.getAbsolutePath());
        }

        // Parse manifest
        String manifestContent = new String(Files.readAllBytes(manifestFile.toPath()));
        JsonObject manifestJson = gson.fromJson(manifestContent, JsonObject.class);
        WebExtensionManifest manifest = new WebExtensionManifest(manifestJson);

        // Create context
        WebExtensionContext context = new WebExtensionContext(extensionDir, manifest);
        loadedExtensions.put(context.getExtensionId(), context);

        logger.info("Loaded WebExtension: {} v{} (ID: {})", 
            manifest.getName(), manifest.getVersion(), context.getExtensionId());

        return context;
    }

    /**
     * Get a loaded extension by ID.
     */
    public WebExtensionContext getExtension(String extensionId) {
        return loadedExtensions.get(extensionId);
    }

    /**
     * Get all loaded extensions.
     */
    public List<WebExtensionContext> getAllExtensions() {
        return new ArrayList<>(loadedExtensions.values());
    }

    /**
     * Get information about all loaded extensions.
     */
    public List<ExtensionInfo> getExtensionInfos() {
        List<ExtensionInfo> infos = new ArrayList<>();
        for (WebExtensionContext ctx : loadedExtensions.values()) {
            WebExtensionManifest manifest = ctx.getManifest();
            infos.add(new ExtensionInfo(
                ctx.getExtensionId(),
                manifest.getName(),
                manifest.getVersion(),
                manifest.getDescription(),
                manifest.getIcons().get("128"),
                manifest.getIcons().get("48"),
                manifest.getIcons().get("16")
            ));
        }
        return infos;
    }

    /**
     * Information about a WebExtension.
     */
    public static class ExtensionInfo {
        public String id;
        public String name;
        public String version;
        public String description;
        public String iconLarge;  // 128x128
        public String iconMedium; // 48x48
        public String iconSmall;  // 16x16

        public ExtensionInfo(String id, String name, String version, String description, 
                           String iconLarge, String iconMedium, String iconSmall) {
            this.id = id;
            this.name = name;
            this.version = version;
            this.description = description;
            this.iconLarge = iconLarge;
            this.iconMedium = iconMedium;
            this.iconSmall = iconSmall;
        }
    }
}
