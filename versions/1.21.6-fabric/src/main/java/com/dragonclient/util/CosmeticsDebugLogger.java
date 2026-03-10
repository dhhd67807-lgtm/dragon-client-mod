package com.dragonclient.util;

import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public final class CosmeticsDebugLogger {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Map<String, Long> LAST_LOG_TIMES = new HashMap<>();
    private static PrintWriter writer;
    private static Path logPath;
    private static boolean initFailed;

    private CosmeticsDebugLogger() {
    }

    private static synchronized void ensureWriter() {
        if (writer != null || initFailed) {
            return;
        }

        try {
            MinecraftClient client = MinecraftClient.getInstance();
            Path runDir = (client != null && client.runDirectory != null)
                    ? client.runDirectory.toPath()
                    : Paths.get(".");
            logPath = runDir.resolve("logs").resolve("dragonclient-cosmetics-debug.log");
            Files.createDirectories(logPath.getParent());

            writer = new PrintWriter(Files.newBufferedWriter(
                    logPath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            ), true);
            log("=== Cosmetics Debug Logger Started ===");
            log("Log file location: " + logPath.toAbsolutePath());
        } catch (IOException e) {
            initFailed = true;
            System.err.println("Failed to create cosmetics debug log: " + e.getMessage());
        }
    }

    public static synchronized void log(String message) {
        ensureWriter();
        if (writer == null) {
            return;
        }
        String timestamp = LocalDateTime.now().format(FORMATTER);
        writer.println("[" + timestamp + "] " + message);
    }

    public static synchronized void logEvery(String key, long intervalMs, String message) {
        long now = System.currentTimeMillis();
        Long last = LAST_LOG_TIMES.get(key);
        if (last == null || now - last >= intervalMs) {
            LAST_LOG_TIMES.put(key, now);
            log(message);
        }
    }

    public static synchronized String getLogFilePath() {
        ensureWriter();
        return logPath != null ? logPath.toAbsolutePath().toString() : "(unavailable)";
    }
}
