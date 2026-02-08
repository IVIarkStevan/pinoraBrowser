# WebExtensions Support

Pinora Browser now supports **Firefox/Chrome-compatible WebExtensions** in addition to the legacy Java extension format.

## Quick Start

### Installing a WebExtension

1. Create a folder inside the `webextensions/` directory with your extension name
2. Add a `manifest.json` file to your extension folder
3. Launch the browser and open **Extensions → Manage Extensions**
4. Navigate to the **WebExtensions** tab to see your extension

### Example: manifest.json

```json
{
  "manifest_version": 3,
  "name": "My Awesome Extension",
  "version": "1.0.0",
  "description": "A brief description of what this extension does",
  "permissions": ["tabs", "storage", "webRequest"],
  "icons": {
    "16": "images/icon-16.png",
    "48": "images/icon-48.png",
    "128": "images/icon-128.png"
  },
  "action": {
    "default_popup": "popup.html"
  },
  "background": {
    "scripts": ["background.js"]
  },
  "content_scripts": [
    {
      "matches": ["https://example.com/*"],
      "js": ["content.js"],
      "css": ["content.css"],
      "run_at": "document_end"
    }
  ]
}
```

## Supported APIs

### browser.tabs
- `browser.tabs.create(options)` - Create a new tab
- `browser.tabs.query(queryInfo)` - Find tabs matching criteria
- `browser.tabs.update(tabId, updateInfo)` - Modify tab properties
- `browser.tabs.remove(tabId)` - Close a tab

### browser.storage
- `browser.storage.local.get(key)` - Read from local storage
- `browser.storage.local.set(key, value)` - Write to local storage
- `browser.storage.local.remove(key)` - Delete from local storage
- `browser.storage.sync.*` - Same as local (synced across sessions)
- `browser.storage.onChanged` - Listen for storage updates

### browser.messaging
- `browser.runtime.sendMessage(message, callback)` - Send message between parts
- `browser.runtime.onMessage.addListener(handler)` - Listen for messages

### browser.runtime
- `browser.runtime.id` - Get extension ID
- `browser.runtime.getURL(path)` - Get extension resource URL
- `browser.runtime.getManifest()` - Get manifest data

## Directory Structure

```
webextensions/
├── my-extension/
│   ├── manifest.json
│   ├── popup.html
│   ├── popup.js
│   ├── background.js
│   ├── content.js
│   ├── content.css
│   └── images/
│       ├── icon-16.png
│       ├── icon-48.png
│       └── icon-128.png
└── another-extension/
    ├── manifest.json
    └── ...
```

## Content Scripts

Content scripts are injected into web pages that match the specified URL patterns:

```javascript
// content.js
console.log("Content script loaded on:", window.location.href);

// Modify the page
document.body.style.backgroundColor = "lightblue";

// Send message to background script
browser.runtime.sendMessage({ action: "pageLoaded" });
```

## Background Scripts

Background scripts run in the browser context and can access most APIs:

```javascript
// background.js
browser.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.action === "pageLoaded") {
    console.log("Page loaded:", sender.url);
    sendResponse({ status: "received" });
  }
});

// Create a new tab
browser.tabs.create({ url: "https://example.com" });
```

## Popup UI

Add a popup that appears when clicking the extension icon:

```html
<!DOCTYPE html>
<html>
<head>
  <style>
    body { width: 300px; font-family: sans-serif; }
  </style>
</head>
<body>
  <h2>My Extension</h2>
  <button id="btn">Click me</button>
  <script src="popup.js"></script>
</body>
</html>
```

## Limitations

- Content scripts run synchronously (no Promise support yet)
- Some advanced APIs not yet implemented (permissions, webRequest, omnibox)
- Extension resources (icons, JS/CSS files) must be embedded in the manifest folder

## Building an Extension

1. Create your extension folder in `webextensions/`
2. Write your `manifest.json`
3. Add your JS, CSS, and HTML files
4. Add icons (PNG format, 16x16, 48x48, 128x128)
5. Launch Pinora Browser
6. Check **Extensions → Manage Extensions → WebExtensions** tab

Your extension will automatically load!

---

For Firefox/Chrome extension documentation, visit:
- [Firefox WebExtensions API](https://developer.mozilla.org/en-US/docs/Mozilla/Add-ons/WebExtensions)
- [Chrome Extension API](https://developer.chrome.com/docs/extensions/reference/)
