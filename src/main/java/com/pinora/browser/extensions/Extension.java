package com.pinora.browser.extensions;

import com.pinora.browser.ui.BrowserWindow;

/**
 * Simple extension interface for Pinora Browser.
 *
 * Extensions should be packaged as a JAR and include a
 * `META-INF/services/com.pinora.browser.extensions.Extension` file
 * listing the implementing class name so the browser can discover
 * them via {@link java.util.ServiceLoader}.
 */
public interface Extension {
    /**
     * Human readable name for the extension.
     */
    String name();

    /**
     * Called when the extension is loaded. Implementations may
     * modify the UI via the provided {@link BrowserWindow}.
     */
    void onLoad(BrowserWindow window);

    /**
     * Optional cleanup hook when the browser shuts down or extension
     * is unloaded.
     */
    default void onUnload() {}
}
