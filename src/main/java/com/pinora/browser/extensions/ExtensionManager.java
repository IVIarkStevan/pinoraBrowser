package com.pinora.browser.extensions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.pinora.browser.ui.BrowserWindow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

/**
 * Loads extension JARs placed into the `extensions/` directory.
 * Extensions must use the ServiceLoader pattern (provide
 * `META-INF/services/com.pinora.browser.extensions.Extension`).
 */
public class ExtensionManager {

    private static final Logger logger = LoggerFactory.getLogger(ExtensionManager.class);

    private final File extensionsDir;
    private final File configFile;

    private final Map<File, Extension> loadedInstances = new HashMap<>();
    private final Map<File, URLClassLoader> classLoaders = new HashMap<>();
    private final Map<String, Boolean> enabledMap = new HashMap<>();

    public ExtensionManager() {
        this.extensionsDir = new File("extensions");
        this.configFile = new File(extensionsDir, "config.properties");
        loadConfig();
    }

    /**
     * Load all extension JARs and invoke their {@code onLoad}.
     */
    public void loadExtensions(BrowserWindow window) {
        String auto = System.getProperty("pinora.extensions.autoload", "true");
        if ("false".equalsIgnoreCase(auto)) {
            logger.info("Auto-loading of extensions disabled via system property");
            return;
        }
        if (!extensionsDir.exists()) {
            boolean created = extensionsDir.mkdirs();
            if (created) {
                logger.info("Created extensions directory at {}", extensionsDir.getAbsolutePath());
            }
            logger.info("No extensions found ({} does not contain JARs)", extensionsDir.getAbsolutePath());
            return;
        }

        File[] files = extensionsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        if (files == null || files.length == 0) {
            logger.info("No extension JARs found in {}", extensionsDir.getAbsolutePath());
            return;
        }

        int count = 0;
        for (File f : files) {
            try {
                logger.info("Discovered extension JAR: {}", f.getName());
                boolean enabled = enabledMap.getOrDefault(f.getName(), Boolean.TRUE);
                if (enabled) {
                    loadJarExtension(f, window);
                    count++;
                }
            } catch (Throwable t) {
                logger.warn("Failed to load extension {}: {}", f.getName(), t.getMessage());
            }
        }

        logger.info("Loaded {} extension(s)", count);
    }

    private void loadJarExtension(File jarFile, BrowserWindow window) {
        if (loadedInstances.containsKey(jarFile)) {
            return; // already loaded
        }

        try {
            URL url = jarFile.toURI().toURL();
            URLClassLoader loader = new URLClassLoader(new URL[]{url}, getClass().getClassLoader());
            classLoaders.put(jarFile, loader);
            ServiceLoader<Extension> sl = ServiceLoader.load(Extension.class, loader);
            for (Extension ext : sl) {
                try {
                    ext.onLoad(window);
                    loadedInstances.put(jarFile, ext);
                    logger.info("Loaded extension instance: {} from {}", ext.name(), jarFile.getName());
                } catch (Throwable t) {
                    logger.error("Extension {} threw an error during onLoad: {}", ext.name(), t.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load extension JAR {}: {}", jarFile.getName(), e.getMessage());
        }
    }

    private void unloadJarExtension(File jarFile) {
        Extension ext = loadedInstances.remove(jarFile);
        URLClassLoader loader = classLoaders.remove(jarFile);
        if (ext != null) {
            try {
                ext.onUnload();
                logger.info("Unloaded extension: {}", ext.name());
            } catch (Throwable t) {
                logger.warn("Error during extension onUnload: {}", t.getMessage());
            }
        }
        if (loader != null) {
            try {
                loader.close();
            } catch (IOException e) {
                logger.warn("Failed to close classloader for {}: {}", jarFile.getName(), e.getMessage());
            }
        }
    }

    public List<ExtensionDescriptor> listExtensions() {
        List<ExtensionDescriptor> list = new ArrayList<>();
        File[] files = extensionsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        if (files == null) return list;
        for (File f : files) {
            String jarName = f.getName();
            boolean enabled = enabledMap.getOrDefault(jarName, Boolean.TRUE);
            boolean loaded = loadedInstances.containsKey(f);
            String display = loaded ? loadedInstances.get(f).name() : "(not loaded)";
            list.add(new ExtensionDescriptor(jarName, display, enabled, loaded));
        }
        return list;
    }

    public void enableExtension(String jarName, BrowserWindow window) {
        File jar = new File(extensionsDir, jarName);
        enabledMap.put(jarName, Boolean.TRUE);
        saveConfig();
        if (jar.exists()) {
            loadJarExtension(jar, window);
        }
    }

    public void disableExtension(String jarName) {
        File jar = new File(extensionsDir, jarName);
        enabledMap.put(jarName, Boolean.FALSE);
        saveConfig();
        if (jar.exists()) {
            unloadJarExtension(jar);
        }
    }

    private void loadConfig() {
        if (!configFile.exists()) return;
        Properties p = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            p.load(fis);
            for (String name : p.stringPropertyNames()) {
                enabledMap.put(name, Boolean.parseBoolean(p.getProperty(name)));
            }
        } catch (IOException e) {
            logger.warn("Could not read extensions config: {}", e.getMessage());
        }
    }

    private void saveConfig() {
        if (!extensionsDir.exists()) {
            extensionsDir.mkdirs();
        }
        Properties p = new Properties();
        for (Map.Entry<String, Boolean> e : enabledMap.entrySet()) {
            p.setProperty(e.getKey(), e.getValue().toString());
        }
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            p.store(fos, "Pinora extensions enabled state");
        } catch (IOException e) {
            logger.warn("Could not save extensions config: {}", e.getMessage());
        }
    }

    public static class ExtensionDescriptor {
        private final String jarName;
        private final String displayName;
        private final boolean enabled;
        private final boolean loaded;

        public ExtensionDescriptor(String jarName, String displayName, boolean enabled, boolean loaded) {
            this.jarName = jarName;
            this.displayName = displayName;
            this.enabled = enabled;
            this.loaded = loaded;
        }

        public String getJarName() { return jarName; }
        public String getDisplayName() { return displayName; }
        public boolean isEnabled() { return enabled; }
        public boolean isLoaded() { return loaded; }
    }
}
