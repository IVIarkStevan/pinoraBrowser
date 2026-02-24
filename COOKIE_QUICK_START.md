# Cookie Management System - Quick Start Guide

## What's New?

Pinora Browser now includes a complete **Cookie Management System** for better privacy control and debugging!

## ✨ Features

### 1. Cookie Viewer
- See all cookies stored by websites
- View complete cookie details (domain, path, expiry, security flags)
- Monitor session vs. persistent cookies
- Real-time statistics dashboard

### 2. Cookie Privacy Controls
- **Block Tracking Cookies**: Automatically prevent analytics tracking
- **Block Third-Party Cookies**: Stop cross-site tracking
- **Per-Domain Policies**: Allow/block cookies for specific domains

### 3. Cookie Management
- Delete individual cookies or entire domains
- Remove all session cookies at once
- View cookie details and metadata
- Filter cookies by domain

### 4. Import/Export
- **Export**: Save all cookies to a file for backup or transfer
- **Import**: Load cookies from a file (great for testing)

---

## How to Use

### Access the Cookie Manager

**Option 1: Direct Access (Recommended)**
1. Click **Tools** → **Cookie Manager...**
2. The Cookie Manager window opens

**Option 2: Through Preferences**
1. Click **Edit** → **Preferences**
2. Go to the **Cookies** tab
3. Click **Open Cookie Manager**

### View Your Cookies

1. Open the **Cookie Manager** dialog
2. All stored cookies appear in the table
3. Click on any cookie to see full details in the right panel
4. Use the **Domain Filter** dropdown to see cookies from a specific website

### Manage Privacy

In the Cookie Manager toolbar:
- ✓ **Block tracking cookies**: Prevents Google Analytics, Facebook Pixel, etc.
- ✓ **Block third-party cookies**: Stops ads tracking you across websites

### Delete Cookies

Choose what to delete:
- **Delete Selected**: Remove one cookie from the table
- **Delete All**: Remove all cookies (requires confirmation)
- **Delete Session Cookies**: Keep persistent cookies, clear temporary ones

### Backup & Import Cookies

**Export to File:**
1. Click **Export**
2. Choose save location
3. Cookies saved as `pinora-cookies.json`

**Import from File:**
1. Click **Import**
2. Select a JSON file with cookies
3. Cookies are added to your browser

---

## Understanding Cookies

### Cookie Types

**Session Cookies**
- Cleared when you close the browser
- Contain temporary login info
- Don't persist between sessions

**Persistent Cookies**
- Saved to disk even after closing browser
- Restored when you restart
- Can expire after set time or never

### Security Flags

**Secure**
- Only sent over HTTPS connections
- Protected from interception

**HttpOnly**
- Cannot be accessed by JavaScript
- Protects against XSS attacks

**SameSite**
- Controls where cookies are sent
- Prevents cross-site request forgery

---

## Privacy Tips

### 1. Block Tracking
✓ Enable "Block tracking cookies" to prevent analytics companies from tracking you across sites

### 2. Clean Up Regularly
- Open Cookie Manager
- Export cookies first (for backup)
- Click "Delete All" to clean everything
- Browser will still work - cookies recreated as needed

### 3. Session-Only Cookies
- Enable "Delete Session Cookies" before leaving for the day
- Keeps sites from knowing you visited

### 4. Check What's Stored
- Periodically open Cookie Manager
- Review which domains have cookies
- Delete cookies from sites you don't trust

---

## Typical Workflow

### For Privacy-Conscious Users
```
1. Daily: Open Cookie Manager
2. Review: Look for unexpected cookies
3. Export: Save cookies from trusted sites
4. Delete: Remove everything
5. Import: Restore trusted cookies
6. Close browser with minimal tracking
```

### For Developers/Testers
```
1. Build site with test data
2. Grab cookies with Export
3. Share JSON file with team
4. Import cookies on another machine
5. Continue testing with same state
```

### For Administrators
```
1. Export cookies from reference setup
2. Create cookie template file
3. Import template for standard testing environment
4. Track cookie changes between versions
```

---

## Storage Location

Cookies are stored in:
- **Linux/Mac**: `~/.pinora/cookies.json`
- **Windows**: `C:\Users\[YourName]\.pinora\cookies.json`

You can manually backup this file or use Export feature.

---

## FAQ

**Q: Will clearing cookies break websites?**
A: Websites will still work but may treat you as a new user. Some might ask you to log in again.

**Q: Are my passwords stored in cookies?**
A: Some passwords may be stored in session cookies. Never share exported cookies with untrusted people.

**Q: Can I edit individual cookie values?**
A: Currently you can view details. To edit, export, modify the JSON, and import back.

**Q: What if I accidentally delete all cookies?**
A: Export your cookies first! Websites will recreate them when you visit.

**Q: Will blocking tracking cookies break websites?**
A: Usually no. Tracking cookies are used for ads, not website functionality. Some analytics may not work, but sites still function.

**Q: How do I backup my cookies?**
A: Use the Export button in Cookie Manager to save to a JSON file.

---

## Tracking Cookie Detection

The browser automatically detects these common tracking patterns:
- `_ga`, `_gid` - Google Analytics
- `fbp`, `_fbp` - Facebook Pixel
- `doubleclick` - Google DoubleClick
- `_ym_` - Yandex Analytics
- And 10+ others

When blocked, you'll see the cookie entry logged but not stored.

---

## Statistics Dashboard

The Cookie Manager shows real-time stats:
- **Total**: All cookies stored
- **Session**: Temporary cookies
- **Persistent**: Saved cookies
- **Secure**: HTTPS-only cookies
- **Domains**: Unique websites with cookies

---

## Keyboard Shortcuts

In Cookie Manager:
- `Ctrl+T` - New Tab (from main browser)
- `Ctrl+J` - Show Downloads
- `Ctrl+Q` - Quit Browser

---

## Need Help?

- Check the detailed documentation: `COOKIE_MANAGEMENT.md`
- Review implementation details: `COOKIE_IMPLEMENTATION_SUMMARY.md`
- View logs for debugging: Look for cookie operations in application logs

---

## What Gets Saved?

Each cookie stores:
- Name & Value
- Domain & Path
- Expiry time
- Security flags (Secure, HttpOnly, SameSite)
- Created & accessed timestamps

All information is needed for the browser to properly use cookies.

---

## Before/After

### Before Cookie Manager
- ❌ No visibility into cookies
- ❌ Can't control tracking
- ❌ No backup option
- ❌ Can't migrate cookies

### After Cookie Manager
- ✓ Complete cookie transparency
- ✓ Privacy controls
- ✓ Easy backup/restore
- ✓ Cookie migration possible
- ✓ Testing and debugging
- ✓ Statistics and monitoring

---

**Enjoy better cookie and privacy management with Pinora Browser!**
