package com.dragonclient.gui.hud;

import com.dragonclient.DragonClientMod;
import com.dragonclient.module.Module;
import com.dragonclient.module.hud.HudModule;
import net.minecraft.client.gui.DrawContext;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Paths;

public class HudRenderer {
    private static PrintWriter debugLog;
    private static int renderCallCount = 0;
    
    static {
        try {
            String logPath = Paths.get(System.getProperty("user.home"), "dragonclient-debug.log").toString();
            debugLog = new PrintWriter(new FileWriter(logPath, true), true);
            debugLog.println("=== Dragon Client HUD Renderer Started ===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void render(DrawContext context, float tickDelta) {
        try {
            renderCallCount++;
            
            // Get stack trace to see who called us
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String caller = stackTrace.length > 2 ? stackTrace[2].toString() : "unknown";
            
            if (debugLog != null && renderCallCount % 60 == 0) {
                debugLog.println("HudRenderer.render() call #" + renderCallCount + " from: " + caller);
                debugLog.flush();
            }
            
            int enabledCount = 0;
            for (Module module : DragonClientMod.getInstance().getModuleManager().getEnabledModules()) {
                if (module instanceof HudModule) {
                    HudModule hudModule = (HudModule) module;
                    enabledCount++;
                    
                    // Apply module-specific scaling
                    var matrices = context.getMatrices();
                    matrices.push();
                    
                    float moduleScale = hudModule.getScale();
                    matrices.translate((float)hudModule.getX(), (float)hudModule.getY(), 0f);
                    matrices.scale(moduleScale, moduleScale, 1.0f);
                    matrices.translate((float)-hudModule.getX(), (float)-hudModule.getY(), 0f);
                    
                    hudModule.render(context, tickDelta);
                    
                    matrices.pop();
                }
            }
            
            if (debugLog != null && enabledCount == 0 && renderCallCount % 60 == 0) {
                debugLog.println("No enabled HUD modules found!");
            }
        } catch (Exception e) {
            if (debugLog != null) {
                debugLog.println("Error rendering HUD: " + e.getMessage());
                e.printStackTrace(debugLog);
            }
            DragonClientMod.LOGGER.error("Error rendering HUD", e);
        }
    }
}
