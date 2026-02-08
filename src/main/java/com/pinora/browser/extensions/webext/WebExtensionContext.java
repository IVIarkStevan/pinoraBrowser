package com.pinora.browser.extensions.webext;

import com.pinora.browser.extensions.webext.api.TabsAPI;
import com.pinora.browser.extensions.webext.api.StorageAPI;
import com.pinora.browser.extensions.webext.api.MessagingAPI;
import com.pinora.browser.extensions.webext.api.RuntimeAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

/**
 * Runtime context for a loaded WebExtension.
 * Provides access to browser APIs.
 */
public class WebExtensionContext {

    private static final Logger logger = LoggerFactory.getLogger(WebExtensionContext.class);

    private String extensionId;
    private File extensionDir;
    private WebExtensionManifest manifest;

    // APIs
    private TabsAPI tabsAPI;
    private StorageAPI storageAPI;
    private MessagingAPI messagingAPI;
    private RuntimeAPI runtimeAPI;

    public WebExtensionContext(File extensionDir, WebExtensionManifest manifest) {
        this.extensionId = UUID.randomUUID().toString().substring(0, 32);
        this.extensionDir = extensionDir;
        this.manifest = manifest;

        // Initialize APIs
        this.tabsAPI = new TabsAPI();
        this.storageAPI = new StorageAPI();
        this.messagingAPI = new MessagingAPI();
        this.runtimeAPI = new RuntimeAPI(extensionId, manifest.getName(), manifest.getVersion(), manifest.getManifestVersion());

        logger.info("Created WebExtensionContext for {} (ID: {})", manifest.getName(), extensionId);
    }

    public String getExtensionId() { return extensionId; }
    public File getExtensionDir() { return extensionDir; }
    public WebExtensionManifest getManifest() { return manifest; }

    public TabsAPI getTabs() { return tabsAPI; }
    public StorageAPI getStorage() { return storageAPI; }
    public MessagingAPI getMessaging() { return messagingAPI; }
    public RuntimeAPI getRuntime() { return runtimeAPI; }

    /**
     * Load a resource file from the extension directory.
     */
    public String loadResource(String path) {
        try {
            File file = new File(extensionDir, path);
            if (file.exists() && file.isFile()) {
                return new String(Files.readAllBytes(file.toPath()));
            }
        } catch (Exception e) {
            logger.warn("Could not load resource {}: {}", path, e.getMessage());
        }
        return null;
    }

    /**
     * Get the URL for an extension resource.
     */
    public String getResourceUrl(String path) {
        return "extension://" + extensionId + "/" + path;
    }
}
