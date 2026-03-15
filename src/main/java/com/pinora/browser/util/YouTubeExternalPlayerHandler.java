package com.pinora.browser.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Handles YouTube video playback using external players (VLC + yt-dlp)
 * Provides HD/1080p+ quality playback independent of WebEngine limitations
 */
public class YouTubeExternalPlayerHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(YouTubeExternalPlayerHandler.class);
    
    // YouTube format codes
    public static final int FMT_480P = 18;  // 480p MPEG-4
    public static final int FMT_720P = 22;  // 720p H.264
    public static final int FMT_BEST = -1;  // Best available quality
    
    private static final String VLC_WINDOWS = "C:\\Program Files\\VideoLAN\\VLC\\vlc.exe";
    private static final String VLC_WINDOWS_ALT = "C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe";
    private static final String VLC_LINUX = "/usr/bin/vlc";
    private static final String VLC_MACOS = "/Applications/VLC.app/Contents/MacOS/VLC";
    
    private boolean isWindows;
    private boolean isLinux;
    private boolean vlcAvailable;
    private boolean ytdlpAvailable;
    
    public YouTubeExternalPlayerHandler() {
        String os = System.getProperty("os.name").toLowerCase();
        this.isWindows = os.contains("win");
        this.isLinux = os.contains("linux");
        
        this.vlcAvailable = checkVLCAvailable();
        this.ytdlpAvailable = checkYtdlpAvailable();
        
        logger.info("YouTube External Player Handler initialized - VLC: {}, yt-dlp: {}", 
                vlcAvailable, ytdlpAvailable);
    }
    
    /**
     * Check if VLC is available on the system
     */
    private boolean checkVLCAvailable() {
        try {
            ProcessBuilder pb;
            if (isWindows) {
                pb = new ProcessBuilder("where", "vlc");
            } else {
                pb = new ProcessBuilder("which", "vlc");
            }
            pb.redirectErrorStream(true);
            Process p = pb.start();
            boolean result = p.waitFor(3, TimeUnit.SECONDS);
            return result && p.exitValue() == 0;
        } catch (Exception e) {
            logger.debug("VLC not found: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if yt-dlp is available on the system
     */
    private boolean checkYtdlpAvailable() {
        try {
            ProcessBuilder pb;
            if (isWindows) {
                pb = new ProcessBuilder("where", "yt-dlp");
            } else {
                pb = new ProcessBuilder("which", "yt-dlp");
            }
            pb.redirectErrorStream(true);
            Process p = pb.start();
            boolean result = p.waitFor(3, TimeUnit.SECONDS);
            return result && p.exitValue() == 0;
        } catch (Exception e) {
            logger.debug("yt-dlp not found: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if external player is available and ready to use
     */
    public boolean isAvailable() {
        return vlcAvailable && ytdlpAvailable;
    }
    
    /**
     * Get status message about what's available
     */
    public String getStatusMessage() {
        if (vlcAvailable && ytdlpAvailable) {
            return "External player ready (VLC + yt-dlp)";
        } else if (!vlcAvailable && !ytdlpAvailable) {
            return "Install VLC (videolan.org) and yt-dlp for HD playback";
        } else if (!vlcAvailable) {
            return "Install VLC from videolan.org for HD playback";
        } else {
            return "Install yt-dlp for HD playback";
        }
    }
    
    /**
     * Play YouTube URL with external player (VLC + yt-dlp)
     * Uses best video quality available
     */
    public void playYouTubeURL(String youtubeUrl) {
        playYouTubeURL(youtubeUrl, FMT_BEST);
    }
    
    /**
     * Play YouTube URL with specific format
     * @param youtubeUrl The YouTube URL
     * @param format Format code: FMT_480P, FMT_720P, or FMT_BEST
     */
    public void playYouTubeURL(String youtubeUrl, int format) {
        if (!isAvailable()) {
            logger.warn("External player not available");
            return;
        }
        
        new Thread(() -> {
            try {
                // Add format parameter to force specific quality
                String urlWithFormat = youtubeUrl;
                if (format == FMT_480P || format == FMT_720P) {
                    urlWithFormat = injectFormatParameter(youtubeUrl, format);
                    logger.info("Playing YouTube URL with fmt={}: {}", format, urlWithFormat);
                } else {
                    logger.info("Playing YouTube URL with best quality: {}", youtubeUrl);
                }
                
                // Extract best video quality using yt-dlp
                String streamUrl = getYouTubeStreamURL(urlWithFormat);
                
                if (streamUrl == null || streamUrl.isEmpty()) {
                    logger.error("Could not extract stream URL from YouTube");
                    return;
                }
                
                logger.info("Extracted stream URL, launching VLC");
                launchVLC(streamUrl);
                
            } catch (Exception e) {
                logger.error("Error playing YouTube video: {}", e.getMessage(), e);
            }
        }).start();
    }
    
    /**
     * Inject format parameter into YouTube URL
     * Converts: youtube.com/watch?v=ID to youtube.com/watch?v=ID&fmt=FORMAT
     */
    private String injectFormatParameter(String youtubeUrl, int format) {
        try {
            if (youtubeUrl.contains("&fmt=")) {
                // Replace existing fmt parameter
                return youtubeUrl.replaceAll("&fmt=\\d+", "&fmt=" + format);
            } else if (youtubeUrl.contains("?")) {
                // Add fmt parameter after existing parameters
                return youtubeUrl + "&fmt=" + format;
            } else {
                // Add fmt parameter as first query parameter
                return youtubeUrl + "?fmt=" + format;
            }
        } catch (Exception e) {
            logger.warn("Error injecting format parameter: {}", e.getMessage());
            return youtubeUrl;
        }
    }
    
    /**
     * Extract the best quality stream URL using yt-dlp
     */
    private String getYouTubeStreamURL(String youtubeUrl) {
        try {
            ProcessBuilder pb;
            
            if (isWindows) {
                // Windows command
                pb = new ProcessBuilder("yt-dlp", 
                    "-f", "best[height<=1080]/best",
                    "-g",  // Get URL only
                    youtubeUrl);
            } else {
                // Linux command
                pb = new ProcessBuilder("yt-dlp",
                    "-f", "best[height<=1080]/best",
                    "-g",  // Get URL only
                    youtubeUrl);
            }
            
            pb.redirectErrorStream(true);
            Process p = pb.start();
            
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }
            
            boolean completed = p.waitFor(30, TimeUnit.SECONDS);
            if (!completed) {
                p.destroyForcibly();
                logger.error("yt-dlp timed out");
                return null;
            }
            
            if (p.exitValue() != 0) {
                logger.error("yt-dlp failed with exit code: {}", p.exitValue());
                return null;
            }
            
            String streamUrl = output.toString().trim();
            if (streamUrl.isEmpty()) {
                logger.error("No stream URL returned from yt-dlp");
                return null;
            }
            
            logger.info("Successfully extracted stream URL");
            return streamUrl;
            
        } catch (Exception e) {
            logger.error("Error extracting YouTube stream URL: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Launch VLC with the stream URL
     */
    private void launchVLC(String streamUrl) {
        try {
            ProcessBuilder pb;
            
            if (isWindows) {
                pb = new ProcessBuilder("vlc", 
                    "--file-caching=5000",
                    "--network-caching=5000",
                    streamUrl);
                pb.directory(new java.io.File("C:\\Program Files\\VideoLAN\\VLC"));
            } else if (isLinux) {
                pb = new ProcessBuilder("vlc",
                    "--file-caching=5000",
                    "--network-caching=5000",
                    streamUrl);
            } else {
                pb = new ProcessBuilder("/Applications/VLC.app/Contents/MacOS/VLC",
                    "--file-caching=5000",
                    "--network-caching=5000",
                    streamUrl);
            }
            
            pb.start();
            logger.info("VLC launched successfully");
            
        } catch (Exception e) {
            logger.error("Error launching VLC: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Check if URL is a YouTube video URL
     */
    public static boolean isYouTubeURL(String url) {
        if (url == null) return false;
        return url.contains("youtube.com") || url.contains("youtu.be");
    }
    
    /**
     * Extract YouTube video ID from URL
     */
    public static String extractVideoId(String url) {
        if (url == null) return null;
        
        if (url.contains("youtu.be/")) {
            return url.substring(url.indexOf("youtu.be/") + 9).split("[&?]")[0];
        }
        
        if (url.contains("youtube.com")) {
            String[] parts = url.split("[?&]");
            for (String part : parts) {
                if (part.startsWith("v=")) {
                    return part.substring(2);
                }
            }
        }
        
        return null;
    }
}
