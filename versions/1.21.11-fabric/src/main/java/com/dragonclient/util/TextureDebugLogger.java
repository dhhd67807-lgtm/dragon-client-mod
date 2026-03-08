package com.dragonclient.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TextureDebugLogger {
    private static final File LOG_FILE = new File(System.getProperty("user.home"), "dragonclient-texture-debug.log");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static boolean initialized = false;
    
    static {
        initializeLog();
    }
    
    private static void initializeLog() {
        if (!initialized) {
            try {
                // Create new log file (overwrite if exists)
                if (LOG_FILE.exists()) {
                    LOG_FILE.delete();
                }
                LOG_FILE.createNewFile();
                
                // Write header
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
                    bw.write("=".repeat(80));
                    bw.newLine();
                    bw.write("Dragon Client 1.21.11 Texture Debug Log");
                    bw.newLine();
                    bw.write("Started: " + LocalDateTime.now().format(TIME_FORMAT));
                    bw.newLine();
                    bw.write("=".repeat(80));
                    bw.newLine();
                    bw.newLine();
                    bw.flush();
                }
                
                initialized = true;
                System.out.println("[DragonClient] Texture debug log created: " + LOG_FILE.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("[DragonClient] Failed to initialize debug log: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public static synchronized void log(String message) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            String timestamp = LocalDateTime.now().format(TIME_FORMAT);
            bw.write("[" + timestamp + "] [INFO] " + message);
            bw.newLine();
            bw.flush();
            
            // Also print to console
            System.out.println("[DragonClient] " + message);
        } catch (IOException e) {
            System.err.println("[DragonClient] Failed to write to debug log: " + e.getMessage());
        }
    }
    
    public static synchronized void logException(String message, Exception e) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            String timestamp = LocalDateTime.now().format(TIME_FORMAT);
            bw.write("[" + timestamp + "] [ERROR] " + message);
            bw.newLine();
            bw.write("Exception: " + e.getClass().getName() + ": " + e.getMessage());
            bw.newLine();
            
            // Write stack trace
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            bw.write(sw.toString());
            bw.newLine();
            bw.flush();
            
            // Also print to console
            System.err.println("[DragonClient] " + message);
            e.printStackTrace();
        } catch (IOException ex) {
            System.err.println("[DragonClient] Failed to write exception to debug log: " + ex.getMessage());
        }
    }
    
    public static String getLogFilePath() {
        return LOG_FILE.getAbsolutePath();
    }
}
