package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Paths;

public class FpsCounterHud extends HudModule {
    private static PrintWriter debugLog;
    private static int renderCount = 0;
    
    static {
        try {
            String logPath = Paths.get(System.getProperty("user.home"), "fps-render-debug.log").toString();
            debugLog = new PrintWriter(new FileWriter(logPath, true), true);
            debugLog.println("=== FPS Counter Started ===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public FpsCounterHud() {
        super("FPS Counter", "Displays current FPS");
        this.x = 5;  // Top left
        this.y = 5;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        renderCount++;
        
        MinecraftClient client = MinecraftClient.getInstance();
        String fps = client.getCurrentFps() + " FPS";
        
        if (debugLog != null && renderCount % 60 == 0) {
            debugLog.println("Frame " + renderCount + ": Drawing at x=" + x + " y=" + y + " text='" + fps + "'");
            debugLog.flush();
        }
        
        // Draw background - #1D1C1C at 50% opacity
        int textWidth = client.textRenderer.getWidth(fps);
        int textHeight = client.textRenderer.fontHeight;
        context.fill(x - 8, y - 8, x + textWidth + 8, y + textHeight + 8, 0x551D1C1C);
        
        // Draw outer border - #161616 at 100% opacity
        context.fill(x - 8, y - 8, x + textWidth + 8, y - 7, 0xFF161616); // Top
        context.fill(x - 8, y + textHeight + 7, x + textWidth + 8, y + textHeight + 8, 0xFF161616); // Bottom
        context.fill(x - 8, y - 8, x - 7, y + textHeight + 8, 0xFF161616); // Left
        context.fill(x + textWidth + 7, y - 8, x + textWidth + 8, y + textHeight + 8, 0xFF161616); // Right
        
        // Draw inset shadow - Dark gray for depth
        context.fill(x - 7, y - 7, x + textWidth + 7, y - 6, 0x50000000); // Top inner shadow
        context.fill(x - 7, y - 7, x - 6, y + textHeight + 7, 0x50000000); // Left inner shadow
        
        // Draw text - White at 100% opacity without shadow
        context.drawText(client.textRenderer, fps, x, y, 0xFFFFFFFF, false);
        
        this.width = textWidth;
        this.height = textHeight;
    }
}
