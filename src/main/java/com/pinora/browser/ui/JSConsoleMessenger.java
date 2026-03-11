package com.pinora.browser.ui;

/**
 * JavaScript console messenger bridge
 * Provides methods that JavaScript can call to log messages to the Java console
 */
public class JSConsoleMessenger {
    
    private DeveloperConsole developperConsole;
    
    public JSConsoleMessenger(DeveloperConsole console) {
        this.developperConsole = console;
    }
    
    /**
     * Log method callable from JavaScript (console.log)
     */
    public void log(String message) {
        if (developperConsole != null) {
            developperConsole.addMessage("log", message);
        }
    }
    
    /**
     * Info method callable from JavaScript (console.info)
     */
    public void info(String message) {
        if (developperConsole != null) {
            developperConsole.addMessage("info", message);
        }
    }
    
    /**
     * Warning method callable from JavaScript (console.warn)
     */
    public void warn(String message) {
        if (developperConsole != null) {
            developperConsole.addMessage("warn", message);
        }
    }
    
    /**
     * Error method callable from JavaScript (console.error)
     */
    public void error(String message) {
        if (developperConsole != null) {
            developperConsole.addMessage("error", message);
        }
    }
    
    /**
     * Debug method callable from JavaScript (console.debug)
     */
    public void debug(String message) {
        if (developperConsole != null) {
            developperConsole.addMessage("debug", message);
        }
    }
}
