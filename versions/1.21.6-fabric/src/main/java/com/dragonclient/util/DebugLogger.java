package com.dragonclient.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DebugLogger {
    private static PrintWriter writer;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    static {
        try {
            // Write to Minecraft run directory
            Path logPath = Paths.get(".", "dragonclient-debug.log");
            writer = new PrintWriter(new FileWriter(logPath.toFile(), true), true);
            log("=== Dragon Client Debug Logger Started ===");
            log("Log file location: " + logPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to create debug log file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void log(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logLine = "[" + timestamp + "] " + message;
        
        // Print to console
        System.out.println(logLine);
        
        // Write to file
        if (writer != null) {
            writer.println(logLine);
            writer.flush();
        }
    }
    
    public static void close() {
        if (writer != null) {
            log("=== Dragon Client Debug Logger Closed ===");
            writer.close();
        }
    }
}
