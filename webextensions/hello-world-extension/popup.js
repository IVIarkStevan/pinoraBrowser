// popup.js - Popup UI logic

document.getElementById('btnNewTab').addEventListener('click', () => {
  // Create a new tab (would work if browser.tabs.create was exposed)
  console.log('New Tab button clicked');
  browser.runtime.sendMessage({ action: 'openGoogleTab' });
});

document.getElementById('btnSaveData').addEventListener('click', () => {
  // Save data to storage
  const count = parseInt(localStorage.getItem('clickCount') || '0') + 1;
  localStorage.setItem('clickCount', count);
  console.log('Saved data. Click count:', count);
  document.querySelector('.info').textContent = 'Saved! Click count: ' + count;
});

// Update page info when popup opens
browser.runtime.sendMessage({ action: 'getCurrentTab' }, (response) => {
  if (response && response.url) {
    document.getElementById('pageInfo').textContent = response.url;
  }
});
