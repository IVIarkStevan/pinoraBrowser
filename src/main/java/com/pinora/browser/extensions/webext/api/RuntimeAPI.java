package com.pinora.browser.extensions.webext.api;

/**
 * Implements browser.runtime API for WebExtensions.
 * Provides extension metadata and lifecycle methods.
 */
public class RuntimeAPI {

    private String extensionId;
    private String manifestVersion;
    private String version;
    private String name;

    public RuntimeAPI(String extensionId, String name, String version, String manifestVersion) {
        this.extensionId = extensionId;
        this.name = name;
        this.version = version;
        this.manifestVersion = manifestVersion;
    }

    /**
     * Get the extension ID.
     */
    public String getId() {
        return extensionId;
    }

    /**
     * Get the manifest version.
     */
    public String getManifestVersion() {
        return manifestVersion;
    }

    /**
     * Get the extension version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get the extension name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get extension URL (not fully implemented, returns a stub).
     */
    public String getURL(String path) {
        return "chrome-extension://" + extensionId + "/" + path;
    }
}
