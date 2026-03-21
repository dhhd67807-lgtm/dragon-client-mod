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
        this.x = 24;
        this.y = 22;
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
        
        int textWidth = client.textRenderer.getWidth(fps);
        int textHeight = client.textRenderer.fontHeight;
        applyDefaultTopRight(client, textWidth + (PANEL_PADDING_X * 2), 22);
        drawLiquidGlassTextPanel(context, textWidth, textHeight);

        context.drawText(client.textRenderer, fps, x, y, 0xFFFFFFFF, false);
        
        this.width = textWidth;
        this.height = textHeight;
    }
}
