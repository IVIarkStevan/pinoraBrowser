# Cookie Management System - Implementation Guide

## Overview

A comprehensive cookie management system has been implemented for Pinora Browser to address privacy and debugging needs. The system supports persistent and session-only cookies with advanced filtering, editing, deletion, and import/export capabilities.

## Components Implemented

### 1. **CookieData** - [src/main/java/com/pinora/browser/core/CookieData.java](src/main/java/com/pinora/browser/core/CookieData.java)
Core data model representing a single cookie with all its properties:
- **Name & Value**: Cookie identifier and content
- **Domain & Path**: Scope of the cookie
- **Security Flags**: Secure, HttpOnly, SameSite attributes
- **Expiry Management**: Persistent vs. session-only cookies
- **Timestamps**: Creation and last accessed times
- **Methods**:
  - `isExpired()`: Check if cookie has expired
  - `getExpiryTimeString()`: Human-readable expiry format

### 2. **CookieManager** - [src/main/java/com/pinora/browser/core/CookieManager.java](src/main/java/com/pinora/browser/core/CookieManager.java)
Central management engine for all cookie operations:

#### Core Features:
- **Cookie Storage**: Persistent JSON-based storage at `~/.pinora/cookies.json`
- **Session Management**: Supports both persistent and session-only cookies
- **Expiration Handling**: Automatic cleanup of expired cookies
- **Domain Filtering**: Get cookies by domain with automatic subdomain matching
- **Domain Policies**: Allow/block cookies per domain
- **Tracking Protection**: Built-in detection and blocking of tracking cookies

#### Key Methods:
```java
// Add or update cookies
setCookie(CookieData cookie)

// Retrieve cookies
getCookie(domain, path, name)
getCookiesForDomain(domain)
getAllValidCookies()
getAllCookies()

// Delete operations
deleteCookie(domain, path, name)
deleteAllCookiesForDomain(domain)
deleteAllCookies()
deleteSessionCookies()
deleteExpiredCookies()

// Policy management
setDomainPolicy(domain, allowed)
isDomainAllowed(domain)
setBlockThirdPartyCookies(block)
setBlockTrackingCookies(block)

// Import/Export
exportCookiesToJSON()
importCookiesFromJSON(json)

// Statistics
getCookieStatistics()
getAllDomains()
```

#### Cookie Policies:
- **Block Third-Party Cookies**: Prevents cookies from domains other than the current site
- **Block Tracking Cookies**: Blocks common tracking patterns (Google Analytics, Facebook, Yandex, etc.)
- **Domain Whitelisting/Blacklisting**: Per-domain cookie acceptance policies

#### Persistent Storage:
- Cookies stored in JSON format for easy inspection
- Automatic saving on each modification
- Policy settings stored separately in `~/.pinora/cookie_policy.json`

### 3. **CookieManagerDialog** - [src/main/java/com/pinora/browser/ui/CookieManagerDialog.java](src/main/java/com/pinora/browser/ui/CookieManagerDialog.java)
Complete UI for cookie management with professional interface:

#### Main Components:

**Toolbar Section:**
- Statistics display (Total, Session, Persistent, Secure, Domains)
- Domain filtering with live search
- Cookie policy toggles (Block tracking, Block third-party)

**Main Table:**
- Sortable columns: Name, Domain, Value (truncated), Expiry
- Real-time filtering by domain
- Selection-based details display

**Details Panel:**
- Complete cookie information display
- Read-only formatted view with hex encoding where needed
- Shows creation time, expiry, and access information

**Action Buttons:**
- **Delete Selected**: Remove individual cookies
- **Delete All**: Clear all cookies (with confirmation)
- **Delete Session Cookies**: Remove non-persistent cookies
- **Export**: Save cookies to JSON file for backup/transfer
- **Import**: Load cookies from JSON file
- **Refresh**: Update table and statistics

#### Usage Patterns:
```
Menu Path: Tools → Cookie Manager...
```

### 4. **Updated PreferencesDialog** - [src/main/java/com/pinora/browser/ui/PreferencesDialog.java](src/main/java/com/pinora/browser/ui/PreferencesDialog.java)
Extended preferences with new Cookie Settings tab:

**Cookie Settings Include:**
- Accept cookies toggle
- Block third-party cookies
- Block tracking cookies
- Save cookies between sessions toggle
- Direct link to full Cookie Manager
- Informational text about cookie persistence

### 5. **BrowserEngine Integration** - [src/main/java/com/pinora/browser/core/BrowserEngine.java](src/main/java/com/pinora/browser/core/BrowserEngine.java)
Core engine enhanced with cookie management:
```java
private CookieManager cookieManager;
public CookieManager getCookieManager() { return cookieManager; }
```

### 6. **BrowserWindow Menu Integration** - [src/main/java/com/pinora/browser/ui/BrowserWindow.java](src/main/java/com/pinora/browser/ui/BrowserWindow.java)
New UI elements added:
- **Edit Menu**: "Preferences" now opens preferences with cookie settings
- **Tools Menu**: New "Cookie Manager..." option for direct access

## Features

### 1. Cookie Viewing & Filtering
- View all cookies with comprehensive details
- Filter by domain with live search
- Display format includes: name, domain, path, value preview, and expiry status
- Session vs. permanent cookie indication

### 2. Cookie Editing & Deletion
- Delete individual cookies
- Bulk delete by domain
- Delete all session cookies at once
- Delete all cookies with one action
- Confirmation dialogs for destructive operations

### 3. Session & Persistent Cookie Support
- **Session Cookies**: Stored in memory, cleared on browser close
- **Persistent Cookies**: Saved to disk, restored on startup
- **Auto-expiration**: Expired cookies automatically removed

### 4. Cookie Policies & Privacy
- **Tracking Cookie Detection**: Identifies common tracking patterns:
  - _ga, _gid (Google Analytics)
  - fbp, _fbp (Facebook Pixel)
  - _ym_ (Yandex)
  - doubleclick (DoubleClick)
  - And many more...

- **Configurable Policies**:
  - Block by domain
  - Block by type (tracking, third-party)
  - Per-domain whitelisting

### 5. Import/Export for Management
- **Export**: Save all cookies to JSON file
  - Useful for backup and migration
  - Human-readable format
  - Includes all metadata (expiry, flags, etc.)

- **Import**: Load cookies from JSON file
  - Batch import capability
  - Automatic validation
  - Progress feedback

### 6. Statistics & Monitoring
Real-time statistics display:
- Total cookie count
- Session vs. persistent breakdown
- Secure vs. non-secure count
- HTTP-only cookies count
- Unique domain count

## File Storage Locations

```
~/.pinora/
├── cookies.json          # All stored cookies
└── cookie_policy.json    # Domain policies and global settings
```

### Cookie JSON Format:
```json
[
  {
    "name": "session_id",
    "value": "abc123def456",
    "domain": "example.com",
    "path": "/",
    "secure": true,
    "httpOnly": true,
    "sessionOnly": false,
    "expiryTime": 1740000000000,
    "sameSite": "Lax",
    "createdTime": 1735000000000,
    "lastAccessedTime": 1735800000000
  }
]
```

## Integration Points

### Menu Access Paths:
1. **Edit → Preferences** → Cookies tab
2. **Tools → Cookie Manager...**

### Programmatic Access:
```java
// From BrowserEngine
CookieManager cm = browserEngine.getCookieManager();

// Set a cookie
CookieData cookie = new CookieData("name", "value", "example.com", "/");
cookie.setSecure(true);
cookie.setExpiryTime(System.currentTimeMillis() + 86400000); // 1 day
cm.setCookie(cookie);

// Get cookies for a domain
List<CookieData> cookies = cm.getCookiesForDomain("example.com");

// Export for debugging
String json = cm.exportCookiesToJSON();
```

## Security Considerations

1. **File Permissions**: Cookies stored in user home directory with standard permissions
2. **Sensitive Data**: Passwords and tokens stored as plaintext (typical cookie behavior)
3. **Cookie Validation**: No automatic sanitization (follows browser standards)
4. **Expiry Checking**: Automatic removal of expired cookies before retrieval

## Future Enhancements

Potential improvements for future versions:
- Cookie encryption on disk
- Advanced cookie search with regex support
- Cookie categorization and tagging
- Cookie analytics and visualization
- Integration with privacy dashboards
- Automatic cookie cleanup policies
- Cookie sync across devices
- Cookie auditing and change history

## Testing

To test the cookie management system:

1. **Basic Operations**:
   - Open Tools → Cookie Manager
   - Navigate to websites that set cookies
   - Refresh the manager to see new cookies
   - View cookie details

2. **Filtering**:
   - Use domain filter dropdown
   - Observe real-time table updates

3. **Import/Export**:
   - Click "Export" and save cookies
   - Delete all cookies
   - Click "Import" and select saved file
   - Verify cookies are restored

4. **Policies**:
   - Toggle "Block tracking cookies"
   - Visit a website with tracking cookies
   - Verify they are blocked

5. **Performance**:
   - Load multiple cookies (export from another browser)
   - Verify UI responsiveness
   - Check memory usage

## Troubleshooting

**Cookies not persisting**:
- Check file permissions on ~/.pinora/
- Verify disk space available
- Check application logs

**Cookie Manager not opening**:
- Ensure CookieManager is initialized in BrowserEngine
- Check for exceptions in application logs
- Verify PreferencesDialog parameters

**Import failing**:
- Validate JSON format
- Check file encoding (should be UTF-8)
- Ensure file contains valid cookie array

## Building & Compilation

The system integrates seamlessly with the existing Maven build:

```bash
# Full build
mvn clean package

# Compile only
mvn clean compile

# Run tests
mvn test
```

All components are fully integrated and require no additional dependencies beyond those already in the project.
