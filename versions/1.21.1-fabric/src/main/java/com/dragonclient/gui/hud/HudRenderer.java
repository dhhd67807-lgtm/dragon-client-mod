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
            
            if (debugLog != null && renderCallCount % 60 == 0) {
                debugLog.println("\n=== HUD RENDER CYCLE #" + renderCallCount + " ===");
                debugLog.println("Timestamp: " + System.currentTimeMillis());
            }
            
            int enabledCount = 0;
            int totalModules = 0;
            
            for (Module module : DragonClientMod.getInstance().getModuleManager().getModules()) {
                if (module instanceof HudModule) {
                    totalModules++;
                    HudModule hudModule = (HudModule) module;
                    
                    if (debugLog != null && renderCallCount % 60 == 0) {
                        debugLog.println("Module: " + hudModule.getName() + 
                                       " | Enabled: " + hudModule.isEnabled() +
                                       " | X: " + hudModule.getX() + 
                                       " | Y: " + hudModule.getY() +
                                       " | Width: " + hudModule.getWidth() +
                                       " | Height: " + hudModule.getHeight() +
                                       " | Scale: " + hudModule.getScale());
                    }
                    
                    if (hudModule.isEnabled()) {
                        enabledCount++;
                        
                        // Apply module-specific scaling
                        var matrices = context.getMatrices();
                        matrices.push();
                        
                        float moduleScale = hudModule.getScale() / 4.0f; // Normalize to base scale (4.0)
                        matrices.translate((float)hudModule.getX(), (float)hudModule.getY(), 0f);
                        matrices.scale(moduleScale, moduleScale, 1.0f);
                        matrices.translate((float)-hudModule.getX(), (float)-hudModule.getY(), 0f);
                        
                        hudModule.render(context, tickDelta);
                        
                        matrices.pop();
                    }
                }
            }
            
            if (debugLog != null && renderCallCount % 60 == 0) {
                debugLog.println("Total HUD modules: " + totalModules + " | Enabled: " + enabledCount);
                debugLog.flush();
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
