# Cookie Management System - Developer Reference

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    Pinora Browser                                │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ BrowserWindow (Main Application)                          │   │
│  │ - Menu Integration (Tools → Cookie Manager)              │   │
│  │ - Preferences Dialog with Cookie Settings                │   │
│  └──────────────────────────────────────────────────────────┘   │
│           ↓                                    ↓                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ CookieManagerDialog (Professional UI)                    │   │
│  │ - Cookie Table View                                       │   │
│  │ - Domain Filtering                                        │   │
│  │ - Policy Controls                                         │   │
│  │ - Details Panel                                           │   │
│  │ - Import/Export Buttons                                   │   │
│  └──────────────────────────────────────────────────────────┘   │
│           ↓                                                      │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ BrowserEngine (Core Application)                          │   │
│  │ - CookieManager Initialization                            │   │
│  │ - getCookieManager() Accessor                             │   │
│  └──────────────────────────────────────────────────────────┘   │
│           ↓                                                      │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ CookieManager (Core Engine)                               │   │
│  │ - Cookie Storage & Retrieval                              │   │
│  │ - Domain Policies                                         │   │
│  │ - Tracking Detection                                      │   │
│  │ - Import/Export JSON                                      │   │
│  │ - Persistence Layer                                       │   │
│  └──────────────────────────────────────────────────────────┘   │
│           ↓                                                      │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ CookieData (Data Model)                                   │   │
│  │ - Cookie Properties                                       │   │
│  │ - Validation & Helpers                                    │   │
│  └──────────────────────────────────────────────────────────┘   │
│           ↓                                                      │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ File System (Persistence)                                 │   │
│  │ - ~/.pinora/cookies.json                                  │   │
│  │ - ~/.pinora/cookie_policy.json                            │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## Class Hierarchy

```
com.pinora.browser.core.CookieData (Serializable)
├── Properties: name, value, domain, path, secure, httpOnly, sessionOnly
├── Methods: isExpired(), getExpiryTimeString(), equals(), hashCode()
└── Representations: toString()

com.pinora.browser.core.CookieManager
├── Storage: Map<String, CookieData>
├── Policies: Map<String, Boolean>
├── Core Methods:
│   ├── setCookie(CookieData)
│   ├── getCookie(domain, path, name)
│   ├── getCookiesForDomain(domain)
│   ├── deleteAll Variants
│   ├── exportCookiesToJSON()
│   └── importCookiesFromJSON(String)
├── Policy Methods:
│   ├── setDomainPolicy(domain, allowed)
│   ├── setBlockTrackingCookies(boolean)
│   └── setBlockThirdPartyCookies(boolean)
└── Persistence:
    ├── saveCookies()
    ├── loadCookies()
    ├── saveCookiePolicy()
    └── loadCookiePolicy()

com.pinora.browser.ui.CookieManagerDialog
├── UI Components:
│   ├── TableView<CookieData> - Main table
│   ├── ComboBox<String> - Domain filter
│   ├── TextArea - Details display
│   ├── CheckBox - Policy toggles
│   └── Buttons - Actions
├── Methods:
│   ├── show(Stage)
│   ├── refreshCookieTable()
│   ├── filterCookies()
│   ├── displayCookieDetails(CookieData)
│   ├── deleteSelectedCookie()
│   ├── exportCookies()
│   ├── importCookies()
│   └── updateStatistics()
└── Constants: Column widths, button styles

com.pinora.browser.ui.PreferencesDialog
├── Sections:
│   ├── General Settings
│   ├── Privacy & Security
│   ├── Cookies (NEW)
│   └── Search
├── Cookie Section Features:
│   ├── Accept cookies toggle
│   ├── Block tracking toggle
│   ├── Block third-party toggle
│   └── Open Cookie Manager button
└── Methods: show(Stage), createCookieSettings(Stage)

com.pinora.browser.core.BrowserEngine
├── Managers:
│   ├── HistoryManager
│   ├── BookmarkManager
│   ├── CacheManager
│   └── CookieManager (NEW)
└── Methods:
    ├── navigate(String)
    ├── getHistoryManager()
    ├── getBookmarkManager()
    ├── getCacheManager()
    └── getCookieManager() (NEW)

com.pinora.browser.ui.BrowserWindow
├── Menus:
│   ├── File
│   ├── Edit (Preferences updated)
│   ├── View
│   ├── History
│   ├── Extensions
│   ├── Tools (NEW)
│   └── Help
├── Tools Menu Items:
│   └── Cookie Manager...
└── Event Handlers:
    ├── preferencesAction: Opens PreferencesDialog
    └── cookieManagerAction: Opens CookieManagerDialog
```

## File Structure

```
pinoraBrowser/
├── src/main/java/com/pinora/browser/
│   ├── core/
│   │   ├── BrowserEngine.java (Modified - Added CookieManager)
│   │   ├── BookmarkManager.java
│   │   ├── CacheManager.java
│   │   ├── HistoryManager.java
│   │   ├── CookieData.java (NEW)
│   │   └── CookieManager.java (NEW)
│   ├── ui/
│   │   ├── BrowserWindow.java (Modified - Added Tools menu)
│   │   ├── PreferencesDialog.java (Modified - Added Cookies tab)
│   │   ├── DownloadManager.java
│   │   ├── ExtensionIconBar.java
│   │   ├── ExtensionsDialog.java
│   │   ├── WebExtensionsManagerDialog.java
│   │   └── CookieManagerDialog.java (NEW)
│   ├── extensions/ (Unchanged)
│   ├── util/ (Unchanged)
│   └── test/ (Unchanged)
├── COOKIE_MANAGEMENT.md (NEW - Complete documentation)
├── COOKIE_IMPLEMENTATION_SUMMARY.md (NEW - Implementation overview)
├── COOKIE_QUICK_START.md (NEW - User guide)
└── pom.xml (Unchanged - No new dependencies)
```

## Data Flow Diagrams

### Cookie Creation Flow
```
Website Sets Cookie
        ↓
CookieData Created
        ↓
CookieManager.setCookie()
        ↓
Policy Check
├── Track Cookie Detection?
├── Third-Party Policy?
└── Domain Policy?
        ↓
Passed → Save
├── In Memory Cache
└── JSON File (persistent)
        ↓
CookieManagerDialog Update
```

### Cookie Retrieval Flow
```
Request Cookie
        ↓
CookieManager.getCookie()
        ↓
Check In-Memory Map
        ↓
Found?
├── Yes → Check Expiry
│   ├── Valid → Update Access Time → Return
│   └── Expired → Delete → Return null
└── No → Return null
```

### Export/Import Flow
```
Export:
  CookieManager
      ↓
  Iterate All Cookies
      ↓
  Create JsonArray
      ↓
  Serialize to JSON String
      ↓
  Save to File
      ↓
  File System

Import:
  File
      ↓
  Read JSON String
      ↓
  Parse Array
      ↓
  Create CookieData Objects
      ↓
  setCookie() Each
      ↓
  Validation & Storage
```

## API Reference

### CookieManager Public API

#### Constructor
```java
public CookieManager()
```

#### Cookie Operations
```java
public void setCookie(CookieData cookie)
public CookieData getCookie(String domain, String path, String name)
public List<CookieData> getCookiesForDomain(String domain)
public List<CookieData> getAllValidCookies()
public List<CookieData> getAllCookies()
public void deleteCookie(String domain, String path, String name)
public void deleteAllCookiesForDomain(String domain)
public void deleteAllCookies()
public void deleteSessionCookies()
public void deleteExpiredCookies()
```

#### Policy Management
```java
public void setDomainPolicy(String domain, boolean allowed)
public boolean isDomainAllowed(String domain)
public Map<String, Boolean> getAllDomainPolicies()
public void setBlockThirdPartyCookies(boolean block)
public boolean isBlockThirdPartyCookies()
public void setBlockTrackingCookies(boolean block)
public boolean isBlockTrackingCookies()
```

#### Import/Export
```java
public String exportCookiesToJSON()
public int importCookiesFromJSON(String json) throws Exception
```

#### Utilities
```java
public List<String> getAllDomains()
public Map<String, Object> getCookieStatistics()
```

### CookieData Public API

#### Getters
```java
public String getName()
public String getValue()
public String getDomain()
public String getPath()
public boolean isSecure()
public boolean isHttpOnly()
public boolean isSessionOnly()
public long getExpiryTime()
public String getSameSite()
public long getCreatedTime()
public long getLastAccessedTime()
public boolean isExpired()
public String getExpiryTimeString()
```

#### Setters
```java
public void setName(String name)
public void setValue(String value)
public void setDomain(String domain)
public void setPath(String path)
public void setSecure(boolean secure)
public void setHttpOnly(boolean httpOnly)
public void setSessionOnly(boolean sessionOnly)
public void setExpiryTime(long expiryTime)
public void setSameSite(String sameSite)
public void setLastAccessedTime(long lastAccessedTime)
public void setCreatedTime(long createdTime)
```

## Configuration & Properties

### Tracking Cookie Patterns Detected
```
_ga, _gid                  (Google Analytics)
fbp, _fbp                  (Facebook Pixel)
fbsb, fbcsb                (Facebook Conversion)
track, analytics           (Generic tracking)
_utm                       (Older analytics)
doubleclick                (DoubleClick)
_ym_, yandex_gid           (Yandex)
```

### Default Settings
```java
blockThirdPartyCookies = true   // Default: Block 3P cookies
blockTrackingCookies = true     // Default: Block tracking
acceptCookies = true            // Default: Accept cookies
saveCookies = true              // Default: Persist to disk
sameSite = "Lax"                // Default: Lax same-site policy
```

### Storage Paths
```
~/.pinora/cookies.json          // Persistent cookie storage
~/.pinora/cookie_policy.json    // Policy configuration
```

## Integration Points

### From BrowserWindow
```java
// Create with CookieManager
PreferencesDialog prefsDialog = new PreferencesDialog(
    browserEngine.getCookieManager()
);
prefsDialog.show(stage);

// Open Cookie Manager
CookieManagerDialog dialog = new CookieManagerDialog(
    browserEngine.getCookieManager()
);
dialog.show(stage);
```

### From Custom Code
```java
// Get manager from application
BrowserEngine engine = new BrowserEngine();
CookieManager cm = engine.getCookieManager();

// Use it
cm.setCookie(cookie);
List<CookieData> cookies = cm.getCookiesForDomain("example.com");
```

## Performance Characteristics

| Operation | Time Complexity | Notes |
|-----------|-----------------|-------|
| setCookie | O(1) | HashMap insertion |
| getCookie | O(1) | HashMap lookup |
| getCookiesByDomain | O(n) | Stream filter |
| deleteCookie | O(1) | HashMap removal |
| deleteByDomain | O(n) | Batch removal |
| exportJSON | O(n) | Full serialization |
| importJSON | O(n) | Full parsing |

## Memory Usage

- Per cookie: ~500 bytes (typical)
- 1000 cookies: ~0.5 MB in memory
- File storage: JSON format, human-readable
- No caching beyond in-memory HashMap

## Thread Safety

- Not inherently thread-safe (no locks)
- UI operations use `Platform.runLater()`
- File I/O synchronized on save
- Safe for single-threaded Swing/JavaFX

## Error Handling

```
CookieManager Exceptions:
├── IOException
│  └── File I/O failures
├── JsonParseException
│  └── Invalid JSON import
└── NullPointerException (prevented)
   └── Parameter validation

CookieManagerDialog Exceptions:
├── FileNotFoundException
│  └── Import file not found
└── IOException
   └── Export file write failure
```

## Testing Considerations

```java
// Create test cookie
CookieData testCookie = new CookieData(
    "test", "value", "test.com", "/"
);

// Add to manager
CookieManager cm = new CookieManager();
cm.setCookie(testCookie);

// Verify stored
assert cm.getCookie("test.com", "/", "test") != null;

// Test expiry
testCookie.setExpiryTime(System.currentTimeMillis() - 1000);
assert testCookie.isExpired() == true;

// Test export/import
String json = cm.exportCookiesToJSON();
CookieManager cm2 = new CookieManager();
int imported = cm2.importCookiesFromJSON(json);
assert imported == 1;
```

## Build & Deployment

```bash
# Build
mvn clean package -DskipTests

# Result
target/pinora-browser-1.0.0.jar  (47 MB fat JAR)

# Run
java -jar target/pinora-browser-1.0.0.jar

# Cookie Manager Access
Tools → Cookie Manager...
Edit → Preferences → Cookies
```

## Compatibility

- **Java Version**: 21+
- **JavaFX**: 21+
- **GSON**: 2.10+ (already included)
- **OS**: Linux, Windows, macOS
- **JVM**: Tested on OpenJDK 21

---

**This document provides developers with full architectural context and API reference for the cookie management system.**
