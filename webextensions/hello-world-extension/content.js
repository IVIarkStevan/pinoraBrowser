// content.js - Injected into all web pages

console.log('[Content Script] Running on:', window.location.href);

// Send message to background script when page loads
browser.runtime.sendMessage({ action: 'pageLoaded', url: window.location.href });

// Example: Inject a banner on top of the page
const banner = document.createElement('div');
banner.style.cssText = `
  position: fixed; top: 0; left: 0; right: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 10px;
  font-size: 14px;
  z-index: 10000;
  text-align: center;
`;
banner.textContent = '✓ Pinora Browser - Hello World Extension Active';

// Wait for DOM to be ready
if (document.body) {
  document.body.insertBefore(banner, document.body.firstChild);
} else {
  document.addEventListener('DOMContentLoaded', () => {
    document.body.insertBefore(banner, document.body.firstChild);
  });
}

console.log('[Content Script] Banner injected');
