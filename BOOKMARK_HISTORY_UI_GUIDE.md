# Bookmark & History UI Guide

## Overview

Pinora Browser now includes comprehensive UI panels for managing bookmarks and browsing history. Both panels provide full CRUD operations, search capabilities, and real-time statistics.

## Features

### Bookmark Management Panel

**Access:** `View Menu > Bookmarks` or press `Ctrl+B`

Features:
- ✓ **View All Bookmarks**: Organized list with title, URL, and timestamp
- ✓ **Add Bookmarks**: Quick "Add Bookmark" button to save current page
- ✓ **Search**: Real-time search across bookmark titles and URLs
- ✓ **Delete Bookmarks**: Remove individual bookmarks with one click
- ✓ **Navigate**: Click "Open" to visit bookmarked pages
- ✓ **Statistics**: Live count of total bookmarks

### History Management Panel

**Access:** `View Menu > History` or press `Ctrl+H`

Features:
- ✓ **View History**: Complete list of visited pages with timestamps
- ✓ **Search**: Filter history entries by URL or page title
- ✓ **Delete Entries**: Remove individual history entries
- ✓ **Clear All**: Quickly clear entire history with confirmation
- ✓ **Navigate**: Jump back to previously visited pages
- ✓ **Statistics**: Live count of history entries

## UI Components

### Bookmark Panel (`BookmarkHistoryPanel`)

Located in: `src/main/java/com/pinora/browser/ui/BookmarkHistoryPanel.java`

```
┌─────────────────────────────┐
│ Total: 5 bookmarks          │  <- Live statistics
├─────────────────────────────┤
│ Search bookmarks... [input] │  <- Real-time search
├─────────────────────────────┤
│    [Add Bookmark]           │  <- Quick action button
├─────────────────────────────┤
│                             │
│ ☐ Google                    │  <- Bookmark item
│   https://google.com        │
│   [Open] [Delete]           │
│                             │
│ ☐ YouTube                   │
│   https://youtube.com       │
│   [Open] [Delete]           │
│                             │
└─────────────────────────────┘
```

### History Panel (`BookmarkHistoryPanel` - History Tab)

```
┌─────────────────────────────┐
│ Total: 42 entries           │  <- Live statistics
├─────────────────────────────┤
│ Search history... [input]   │  <- Real-time search
├─────────────────────────────┤
│   [Clear History]           │  <- Bulk action
├─────────────────────────────┤
│                             │
│ ☐ github.com                │  <- History entry
│   https://github.com        │
│   Mar 24, 19:30             │
│   [Open] [Delete]           │
│                             │
│ ☐ Stack Overflow            │
│   https://stackoverflow.com │
│   Mar 24, 15:45             │
│   [Open] [Delete]           │
│                             │
└─────────────────────────────┘
```

## Usage Examples

### Adding a Bookmark

1. Navigate to any webpage
2. Press `Ctrl+B` or go to `View Menu > Bookmarks`
3. Click "Add Bookmark"
4. Enter bookmark name
5. Click OK

**Location**: `~/.pinora/bookmarks.json`

### Searching Bookmarks

1. Open Bookmarks panel (`Ctrl+B`)
2. Type in the search box
3. Results update in real-time
4. Search works for both title and URL

### Clearing History

1. Open History panel (`Ctrl+H`)
2. Click "Clear History"
3. Confirm the action
4. All history entries are deleted

**Location**: `~/.pinora/history.json`

### Finding Old Pages

1. Open History panel (`Ctrl+H`)
2. Browse or search for the page
3. Click "Open" to navigate to it

## Technical Details

### Data Persistence

Both bookmarks and history are saved to disk automatically:

- **Bookmarks**: `~/.pinora/bookmarks.json`
- **History**: `~/.pinora/history.json`

Format: JSON with auto-save on every change

### Statistics Display

- **Bookmarks**: Shows total count (e.g., "Total: 5 bookmarks")
- **History**: Shows total with proper pluralization (e.g., "Total: 42 entries", "Total: 1 entry")
- **Auto-Update**: Stats refresh immediately when items are added/removed

### Search Implementation

- **Real-time**: Updates as you type
- **Scope**: Searches title, URL, and domain
- **Case-insensitive**: Lowercase matching for user convenience

### Cell Rendering

Each item displays:
- **Title**: Main display text (bold)
- **URL**: Full URL (clickable, blue)
- **Timestamp**: When added/visited (gray, small)
- **Actions**: "Open" and "Delete" buttons

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Ctrl+B` | Open Bookmarks panel |
| `Ctrl+H` | Open History panel |
| `Ctrl+Y` | Clear History (from History Menu) |
| `Enter` | Search (in search field) |

## Backend Integration

### BookmarkManager

Located in: `src/main/java/com/pinora/browser/core/BookmarkManager.java`

Methods used by UI:
- `getBookmarks()`: Retrieve all bookmarks
- `addBookmark(title, url)`: Add new bookmark
- `removeBookmark(url)`: Delete bookmark
- `searchBookmarks(query)`: Find bookmarks
- `isBookmarked(url)`: Check if URL is bookmarked
- `clearBookmarks()`: Remove all
- `exportToJSON()`: Export all bookmarks
- `importFromJSON(json)`: Import bookmarks

### HistoryManager

Located in: `src/main/java/com/pinora/browser/core/HistoryManager.java`

Methods used by UI:
- `getHistory()`: Retrieve all entries
- `addToHistory(url)`: Record visit
- `deleteEntry(url)`: Delete entry
- `searchHistory(query)`: Find entries
- `clearHistory()`: Remove all
- `getHistoryCount()`: Get total count
- `exportToJSON()`: Export all history
- `importFromJSON(json)`: Import history

## Configuration

### Show/Hide Panels

Panels are shown as modal windows (separate stages). To toggle:
- Use menu items in View menu
- Use keyboard shortcuts
- Panels open on top of the main window

### Customize Appearance

Edit styles in `BookmarkHistoryPanel.java`:

```java
// Stats label style
"-fx-font-size: 10; -fx-text-fill: #666666;"

// Search field style
"-fx-font-size: 11; -fx-padding: 5;"

// Button style
"-fx-padding: 5;"
```

## Troubleshooting

### Bookmarks Not Persisting

**Check:**
- `~/.pinora/bookmarks.json` exists
- File has read/write permissions
- JSON format is valid

**Solution:**
1. Check ConfigManager settings
2. Verify directory permissions
3. Check logs for errors

### History Not Showing

**Check:**
- `~/.pinora/history.json` exists
- Pages are being visited
- History manager is initialized

**Solution:**
1. Verify BrowserEngine initialization
2. Check that navigateTo() calls addToHistory()
3. Check file permissions

### Search Not Working

**Check:**
- Search text is entered
- List items are displayed
- No errors in console

**Solution:**
1. Clear search field
2. Verify bookmark/history data
3. Check search implementation

## Future Enhancements

Planned features for future releases:
- [ ] Sidebar integration (persistent side panel)
- [ ] Export to HTML/CSV
- [ ] Import from other browsers
- [ ] Bookmark organization (folders)
- [ ] History filtering by date range
- [ ] Most visited pages statistics
- [ ] Tag-based organization
- [ ] Cloud synchronization

## API Reference

### BookmarkHistoryPanel Methods

```java
// Show bookmarks tab
selectBookmarksTab()

// Show history tab
selectHistoryTab()

// Refresh all data
refresh()

// Navigate to URL
navigateToUrl(String url)

// Remove item
removeItem(BookmarkHistoryItem item)
```

### Data Classes

```java
// Bookmark item
BookmarkManager.Bookmark {
    String title
    String url
    LocalDateTime timestamp
}

// History entry
HistoryManager.HistoryEntry {
    String url
    LocalDateTime timestamp
    String title (optional)
}

// UI item
BookmarkHistoryPanel.BookmarkHistoryItem {
    String title
    String url
    LocalDateTime timestamp
}
```

## Files Modified

- `src/main/java/com/pinora/browser/ui/BookmarkHistoryPanel.java` - Main UI panel
- `src/main/java/com/pinora/browser/ui/BrowserWindow.java` - Integration
- `src/main/java/com/pinora/browser/core/BookmarkManager.java` - Persistence
- `src/main/java/com/pinora/browser/core/HistoryManager.java` - Persistence

## Version History

- **v1.1.0**: Added UI panels with statistics display
- **v1.0.5**: YouTube format forcing and build optimizations
- **v1.0.4**: YouTube HD player support
- **v1.0.3**: Enhanced Cloudflare compatibility
- **v1.0.2**: Basic Cloudflare shim
- **v1.0.1**: Cookie management improvements
- **v1.0.0**: Initial release

## Support

For issues or feature requests, please visit:
- GitHub Issues: https://github.com/IVIarkStevan/pinoraBrowser/issues
- Documentation: See README.md and other guide files
