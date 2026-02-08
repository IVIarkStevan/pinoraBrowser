// background.js - Background script logic

console.log('[Background] Extension initialized');

// Listen for messages from content scripts and popup
browser.runtime.onMessage = {
  addListener: function(callback) {
    console.log('[Background] Listener registered');
    // Store listener for message handling
  }
};

// Listen for tab changes
browser.runtime.onMessage.addListener((message, sender, sendResponse) => {
  console.log('[Background] Message received:', message);
  
  if (message.action === 'openGoogleTab') {
    browser.tabs.create({ url: 'https://www.google.com' });
    sendResponse({ status: 'Tab opened' });
  } else if (message.action === 'getCurrentTab') {
    // Return current tab info
    sendResponse({ url: 'https://example.com' });
  } else if (message.action === 'pageLoaded') {
    console.log('[Background] Page loaded from content script');
    sendResponse({ status: 'received' });
  }
});

console.log('[Background] Script loaded and ready');
