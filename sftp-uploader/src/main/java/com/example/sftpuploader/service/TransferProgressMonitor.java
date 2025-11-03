package com.example.sftpuploader.service;

import com.jcraft.jsch.SftpProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferProgressMonitor implements SftpProgressMonitor {
    private static final Logger logger = LoggerFactory.getLogger(TransferProgressMonitor.class);
    private final String filename;
    private long transferredBytes = 0;
    private long totalBytes = 0;
    private long startTime;
    private long lastLogTime;
    private static final long LOG_INTERVAL = 1000; // Log every second

    public TransferProgressMonitor(String filename) {
        this.filename = filename;
    }

    @Override
    public void init(int op, String src, String dest, long max) {
        this.startTime = System.currentTimeMillis();
        this.lastLogTime = startTime;
        this.totalBytes = max;
        logger.info("Starting transfer of {} - Total size: {}", filename, formatSize(max));
    }

    @Override
    public boolean count(long count) {
        transferredBytes += count;
        long currentTime = System.currentTimeMillis();
        
        // Log progress every second
        if (currentTime - lastLogTime >= LOG_INTERVAL) {
            double progress = (double) transferredBytes / totalBytes * 100;
            double speed = calculateSpeed(transferredBytes, currentTime - startTime);
            double eta = calculateETA(transferredBytes, totalBytes, currentTime - startTime);
            
            logger.info("{} - Progress: {}% ({}/{}) - Speed: {} MB/s - ETA: {} seconds",
                    filename,
                    String.format("%.1f", progress),
                    formatSize(transferredBytes),
                    formatSize(totalBytes),
                    String.format("%.1f", speed),
                    String.format("%.1f", eta));
            
            lastLogTime = currentTime;
        }
        return true;
    }

    @Override
    public void end() {
        long duration = System.currentTimeMillis() - startTime;
        double avgSpeed = calculateSpeed(transferredBytes, duration);
        
        logger.info("Transfer completed for {} - Total: {} - Avg Speed: {} MB/s - Duration: {} seconds",
                filename,
                formatSize(transferredBytes),
                String.format("%.1f", avgSpeed),
                String.format("%.1f", duration / 1000.0));
    }

    private double calculateSpeed(long bytes, long milliseconds) {
        return (bytes / 1024.0 / 1024.0) / (milliseconds / 1000.0);
    }

    private double calculateETA(long transferred, long total, long elapsed) {
        if (transferred == 0) return -1;
        double bytesPerMillisecond = (double) transferred / elapsed;
        return (total - transferred) / bytesPerMillisecond / 1000.0;
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}