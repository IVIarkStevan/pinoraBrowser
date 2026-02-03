package com.pinora.browser.extensions.webext;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a WebExtension manifest (manifest.json).
 * Supports Firefox and Chrome extension formats.
 */
public class WebExtensionManifest {

    private String manifestVersion;
    private String name;
    private String version;
    private String description;
    private List<String> permissions = new ArrayList<>();
    private Map<String, String> icons = new HashMap<>();
    private String defaultPopup;
    private String backgroundScript;
    private List<ContentScript> contentScripts = new ArrayList<>();
    private JsonObject rawManifest;

    public WebExtensionManifest(JsonObject manifest) {
        this.rawManifest = manifest;
        parse(manifest);
    }

    private void parse(JsonObject manifest) {
        this.manifestVersion = manifest.has("manifest_version") ? manifest.get("manifest_version").getAsString() : "3";
        this.name = manifest.has("name") ? manifest.get("name").getAsString() : "Unknown";
        this.version = manifest.has("version") ? manifest.get("version").getAsString() : "1.0.0";
        this.description = manifest.has("description") ? manifest.get("description").getAsString() : "";

        // Parse permissions
        if (manifest.has("permissions")) {
            JsonArray perms = manifest.getAsJsonArray("permissions");
            for (JsonElement perm : perms) {
                permissions.add(perm.getAsString());
            }
        }

        // Parse icons
        if (manifest.has("icons")) {
            JsonObject iconsObj = manifest.getAsJsonObject("icons");
            for (String key : iconsObj.keySet()) {
                icons.put(key, iconsObj.get(key).getAsString());
            }
        }

        // Parse page action / action
        if (manifest.has("action")) {
            JsonObject action = manifest.getAsJsonObject("action");
            if (action.has("default_popup")) {
                this.defaultPopup = action.get("default_popup").getAsString();
            }
        }

        // Parse background scripts
        if (manifest.has("background")) {
            JsonObject bg = manifest.getAsJsonObject("background");
            if (bg.has("scripts")) {
                JsonArray scripts = bg.getAsJsonArray("scripts");
                if (scripts.size() > 0) {
                    this.backgroundScript = scripts.get(0).getAsString();
                }
            }
        }

        // Parse content scripts
        if (manifest.has("content_scripts")) {
            JsonArray scripts = manifest.getAsJsonArray("content_scripts");
            for (JsonElement scriptElem : scripts) {
                JsonObject scriptObj = scriptElem.getAsJsonObject();
                ContentScript cs = new ContentScript();
                
                if (scriptObj.has("matches")) {
                    JsonArray matches = scriptObj.getAsJsonArray("matches");
                    for (JsonElement match : matches) {
                        cs.matches.add(match.getAsString());
                    }
                }
                
                if (scriptObj.has("js")) {
                    JsonArray jsFiles = scriptObj.getAsJsonArray("js");
                    for (JsonElement js : jsFiles) {
                        cs.jsFiles.add(js.getAsString());
                    }
                }
                
                if (scriptObj.has("css")) {
                    JsonArray cssFiles = scriptObj.getAsJsonArray("css");
                    for (JsonElement css : cssFiles) {
                        cs.cssFiles.add(css.getAsString());
                    }
                }

                if (scriptObj.has("run_at")) {
                    cs.runAt = scriptObj.get("run_at").getAsString();
                }

                contentScripts.add(cs);
            }
        }
    }

    // Getters
    public String getManifestVersion() { return manifestVersion; }
    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getDescription() { return description; }
    public List<String> getPermissions() { return permissions; }
    public Map<String, String> getIcons() { return icons; }
    public String getDefaultPopup() { return defaultPopup; }
    public String getBackgroundScript() { return backgroundScript; }
    public List<ContentScript> getContentScripts() { return contentScripts; }
    public JsonObject getRawManifest() { return rawManifest; }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission) || permissions.contains("<all_urls>");
    }

    /**
     * Represents a content script entry from manifest.json.
     */
    public static class ContentScript {
        public List<String> matches = new ArrayList<>();
        public List<String> jsFiles = new ArrayList<>();
        public List<String> cssFiles = new ArrayList<>();
        public String runAt = "document_idle";
    }
}
