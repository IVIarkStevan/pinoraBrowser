package com.pinora.browser.extensions.webext;

import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;
import com.pinora.browser.extensions.webext.WebExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Injects content scripts into web pages based on manifest rules.
 */
public class ContentScriptInjector {

    private static final Logger logger = LoggerFactory.getLogger(ContentScriptInjector.class);

    /**
     * Inject the browser API shim for extension compatibility.
     * This must be done before content scripts to ensure browser.* is available.
     */
    private static void injectBrowserAPIShim(WebView webView) {
        WebEngine engine = webView.getEngine();
        try {
            String shimCode = loadResource("/js/browser-api-shim.js");
            if (shimCode != null) {
                engine.executeScript(shimCode);
                logger.debug("Injected browser API shim");
            } else {
                logger.warn("Could not load browser-api-shim.js, trying inline shim");
                // Fallback: inline minimal shim
                engine.executeScript(getInlineShim());
            }
        } catch (Exception e) {
            logger.warn("Failed to inject browser API shim: {}", e.getMessage());
            try {
                // Fallback to inline shim if file loading fails
                engine.executeScript(getInlineShim());
            } catch (Exception e2) {
                logger.warn("Failed to inject inline shim: {}", e2.getMessage());
            }
        }
    }

    /**
     * Get an inline/minimal browser API shim as fallback.
     */
    private static String getInlineShim() {
        return "if(typeof browser==='undefined'){window.browser={" +
            "storage:{local:{get:function(){return Promise.resolve()},set:function(){return Promise.resolve()},remove:function(){return Promise.resolve()}}," +
            "sync:{get:function(){return Promise.resolve()},set:function(){return Promise.resolve()},remove:function(){return Promise.resolve()}}," +
            "onChanged:{addListener:function(){}}}," +
            "runtime:{id:'ext-'+Math.random().toString(36).substr(2,9)," +
            "getManifest:function(){return{manifest_version:3,name:'WebExtension',version:'1.0.0'}}," +
            "getURL:function(p){return 'chrome-extension://'+this.id+'/'+p}," +
            "sendMessage:function(){return Promise.resolve({status:'ok'})}," +
            "onMessage:{addListener:function(){}}}," +
            "tabs:{create:function(o){return Promise.resolve({id:1,url:o.url})},query:function(){return Promise.resolve([{id:1,url:window.location.href,title:document.title,active:true}])}," +
            "update:function(){return Promise.resolve()},remove:function(){return Promise.resolve()}}," +
            "action:{setIcon:function(){return Promise.resolve()},setTitle:function(){return Promise.resolve()},setBadgeText:function(){return Promise.resolve()},onClicked:{addListener:function(){}}}};" +
            "console.log('[browser-api-shim] Loaded inline browser API compatibility shim');}";
    }

    /**
     * Inject content scripts into a WebView based on the current URL.
     */
    public static void injectScripts(WebView webView, String url, List<WebExtensionManifest.ContentScript> contentScripts) {
        // Inject browser API shim first
        injectBrowserAPIShim(webView);
        
        for (WebExtensionManifest.ContentScript cs : contentScripts) {
            if (matchesUrl(url, cs.matches)) {
                injectContentScript(webView, cs);
            }
        }
    }

    public static void injectScripts(WebView webView, String url, List<WebExtensionManifest.ContentScript> contentScripts, WebExtensionContext ctx) {
        // Inject browser API shim first
        injectBrowserAPIShim(webView);
        
        for (WebExtensionManifest.ContentScript cs : contentScripts) {
            if (matchesUrl(url, cs.matches)) {
                injectContentScript(webView, cs, ctx);
            }
        }
    }

    /**
     * Check if a URL matches any of the patterns.
     */
    private static boolean matchesUrl(String url, List<String> patterns) {
        for (String pattern : patterns) {
            if (matchesPattern(url, pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Simple wildcard pattern matching (e.g., "https://*.example.com/*").
     */
    private static boolean matchesPattern(String url, String pattern) {
        // Convert glob pattern to regex
        String regex = pattern
                .replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".");
        return url.matches(regex);
    }

    /**
     * Inject a single content script into the WebView.
     */
    private static void injectContentScript(WebView webView, WebExtensionManifest.ContentScript cs) {
        WebEngine engine = webView.getEngine();
        
        // Inject CSS files
        for (String cssFile : cs.cssFiles) {
            try {
                String css = loadResource(cssFile);
                if (css != null) {
                    String jsCode = "var style = document.createElement('style'); style.innerHTML = " 
                        + escapeJSString(css) + "; document.head.appendChild(style);";
                    engine.executeScript(jsCode);
                    logger.info("Injected CSS: {}", cssFile);
                }
            } catch (Exception e) {
                logger.warn("Failed to inject CSS {}: {}", cssFile, e.getMessage());
            }
        }

        // Inject JS files
        for (String jsFile : cs.jsFiles) {
            try {
                String js = loadResource(jsFile);
                if (js != null) {
                    engine.executeScript(js);
                    logger.info("Injected JS: {}", jsFile);
                }
            } catch (Exception e) {
                logger.warn("Failed to inject JS {}: {}", jsFile, e.getMessage());
            }
        }
    }

    private static void injectContentScript(WebView webView, WebExtensionManifest.ContentScript cs, WebExtensionContext ctx) {
        WebEngine engine = webView.getEngine();

        // Inject CSS files from extension
        for (String cssFile : cs.cssFiles) {
            try {
                String css = ctx.loadResource(cssFile);
                if (css == null) css = loadResource(cssFile);
                if (css != null) {
                    String jsCode = "var style = document.createElement('style'); style.innerHTML = " 
                        + escapeJSString(css) + "; document.head.appendChild(style);";
                    engine.executeScript(jsCode);
                    logger.info("Injected extension CSS: {}", cssFile);
                }
            } catch (Exception e) {
                logger.warn("Failed to inject extension CSS {}: {}", cssFile, e.getMessage());
            }
        }

        // Inject JS files from extension
        for (String jsFile : cs.jsFiles) {
            try {
                String js = ctx.loadResource(jsFile);
                if (js == null) js = loadResource(jsFile);
                if (js != null) {
                    engine.executeScript(js);
                    logger.info("Injected extension JS: {}", jsFile);
                }
            } catch (Exception e) {
                logger.warn("Failed to inject extension JS {}: {}", jsFile, e.getMessage());
            }
        }
    }

    /**
     * Load a resource file from the classpath (stub implementation).
     */
    private static String loadResource(String path) {
        try {
            java.io.InputStream is = ContentScriptInjector.class.getResourceAsStream(path);
            if (is != null) {
                return new String(is.readAllBytes());
            }
        } catch (Exception e) {
            logger.warn("Could not load resource {}: {}", path, e.getMessage());
        }
        return null;
    }

    /**
     * Escape a string for use in JavaScript.
     */
    private static String escapeJSString(String str) {
        return "'" + str.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n") + "'";
    }
}
