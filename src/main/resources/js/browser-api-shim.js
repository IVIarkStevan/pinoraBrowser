/**
 * browser API shim for WebExtensions in Pinora Browser
 * Provides basic browser.* API compatibility for Firefox/Chrome extensions
 */

if (typeof browser === 'undefined') {
    window.browser = {
        // Storage API
        storage: {
            local: {
                get: function(key) {
                    return Promise.resolve(localStorage.getItem(key));
                },
                set: function(obj) {
                    for (const [k, v] of Object.entries(obj)) {
                        localStorage.setItem(k, JSON.stringify(v));
                    }
                    return Promise.resolve();
                },
                remove: function(key) {
                    localStorage.removeItem(key);
                    return Promise.resolve();
                }
            },
            sync: {
                get: function(key) {
                    return Promise.resolve(localStorage.getItem(key));
                },
                set: function(obj) {
                    for (const [k, v] of Object.entries(obj)) {
                        localStorage.setItem(k, JSON.stringify(v));
                    }
                    return Promise.resolve();
                },
                remove: function(key) {
                    localStorage.removeItem(key);
                    return Promise.resolve();
                }
            },
            onChanged: {
                addListener: function(callback) {
                    window.addEventListener('storage', (e) => {
                        callback({ [e.key]: { newValue: e.newValue, oldValue: e.oldValue } });
                    });
                }
            }
        },

        // Runtime API
        runtime: {
            id: 'extension-' + Math.random().toString(36).substr(2, 9),
            
            getManifest: function() {
                return {
                    manifest_version: 3,
                    name: 'WebExtension',
                    version: '1.0.0'
                };
            },
            
            getURL: function(path) {
                return 'chrome-extension://' + this.id + '/' + path;
            },
            
            sendMessage: function(extensionId, message, options, callback) {
                // If extensionId is omitted, send to background page
                if (typeof extensionId === 'object') {
                    callback = options;
                    message = extensionId;
                    extensionId = null;
                }
                
                // Dispatch custom event so background script can listen
                const event = new CustomEvent('browser-runtime-message', {
                    detail: { message, sender: { id: extensionId } }
                });
                window.dispatchEvent(event);
                
                if (callback) {
                    callback({ status: 'ok' });
                }
                return Promise.resolve({ status: 'ok' });
            },
            
            onMessage: {
                addListener: function(callback) {
                    window.addEventListener('browser-runtime-message', (e) => {
                        callback(e.detail.message, e.detail.sender, function(response) {
                            // Handle response
                        });
                    });
                }
            },
            
            onInstalled: {
                addListener: function(callback) {
                    // Called when extension is installed
                }
            }
        },

        // Tabs API
        tabs: {
            create: function(options) {
                // In a real browser, this would create a new tab
                // For now, we simulate it by posting a message
                console.log('[browser.tabs.create]', options);
                return Promise.resolve({ id: 1, url: options.url, active: options.active !== false });
            },
            
            query: function(queryInfo) {
                // Return dummy tab info for current page
                return Promise.resolve([{
                    id: 1,
                    url: window.location.href,
                    title: document.title,
                    active: true,
                    status: 'complete'
                }]);
            },
            
            update: function(tabId, updateInfo) {
                console.log('[browser.tabs.update]', tabId, updateInfo);
                return Promise.resolve({ id: tabId, ...updateInfo });
            },
            
            remove: function(tabIds) {
                console.log('[browser.tabs.remove]', tabIds);
                return Promise.resolve();
            },
            
            get: function(tabId) {
                return Promise.resolve({
                    id: tabId,
                    url: window.location.href,
                    title: document.title,
                    active: true
                });
            },
            
            onActivated: {
                addListener: function(callback) {}
            },
            
            onUpdated: {
                addListener: function(callback) {}
            },
            
            onRemoved: {
                addListener: function(callback) {}
            }
        },

        // WebRequest API
        webRequest: {
            onBeforeRequest: {
                addListener: function(callback, filter, extraInfoSpec) {
                    // Would intercept network requests
                }
            }
        },

        // Action API (for extension icon)
        action: {
            setIcon: function(options) {
                return Promise.resolve();
            },
            setTitle: function(options) {
                return Promise.resolve();
            },
            setBadgeText: function(options) {
                return Promise.resolve();
            },
            onClicked: {
                addListener: function(callback) {}
            }
        }
    };
    
    console.log('[browser-api-shim] Loaded browser API compatibility shim');
}
