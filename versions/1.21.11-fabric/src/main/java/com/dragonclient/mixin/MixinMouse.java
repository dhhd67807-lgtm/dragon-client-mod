package com.dragonclient.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Paths;

@Mixin(Mouse.class)
public class MixinMouse {
    
    private static PrintWriter mouseLog;
    
    static {
        try {
            String logPath = Paths.get(System.getProperty("user.home"), "mouse-mixin-debug.log").toString();
            mouseLog = new PrintWriter(new FileWriter(logPath, false), true);
            mouseLog.println("=== Mouse Mixin Debug Log Started (1.21.11) ===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 1.21.11: Inject BEFORE Screen.mouseClicked is called - at this point we still have raw double x, y, int button
    @Inject(method = "onMouseButton", 
            at = @At(value = "INVOKE", 
                     target = "Lnet/minecraft/client/gui/screen/Screen;mouseClicked(Lnet/minecraft/client/gui/Click;Z)Z"),
            cancellable = true)
    private void onMouseButton(CallbackInfo ci,
                              @Local(ordinal = 0) Screen screen,
                              @Local(ordinal = 0) double mouseX,
                              @Local(ordinal = 1) double mouseY,
                              @Local(ordinal = 0, argsOnly = true) int mouseButton) {
        
        try {
            String screenName = screen.getClass().getSimpleName();
            String fullClassName = screen.getClass().getName();
            
            if (mouseLog != null) {
                mouseLog.println("\n=== onMouseButton called (1.21.11) ===");
                mouseLog.println("  Screen type: " + screenName);
                mouseLog.println("  Full class name: " + fullClassName);
                mouseLog.println("  mouseX=" + mouseX + " mouseY=" + mouseY + " button=" + mouseButton);
            }
            
            // Handle HudEditorScreen and DragonClientScreen (check both simple and full class names)
            boolean isHudEditor = screenName.equals("HudEditorScreen") || fullClassName.contains("HudEditorScreen");
            boolean isDragonClient = screenName.equals("DragonClientScreen") || fullClassName.contains("DragonClientScreen");
            
            if (!isHudEditor && !isDragonClient) {
                if (mouseLog != null) {
                    mouseLog.println("  SKIP: Not HudEditorScreen or DragonClientScreen");
                    mouseLog.flush();
                }
                return;
            }
            
            String detectedScreen = isHudEditor ? "HudEditorScreen" : "DragonClientScreen";
            
            if (mouseLog != null) {
                mouseLog.println("  " + detectedScreen + " detected!");
                mouseLog.flush();
            }
            
            System.out.println("[DragonClient] MixinMouse.onMouseButton - Screen: " + detectedScreen + 
                              ", x=" + mouseX + ", y=" + mouseY + ", button=" + mouseButton);
            
            // Call mouseClicked method via reflection
            try {
                java.lang.reflect.Method method = screen.getClass().getMethod("mouseClicked", double.class, double.class, int.class);
                boolean handled = (boolean) method.invoke(screen, mouseX, mouseY, mouseButton);
                
                if (mouseLog != null) {
                    mouseLog.println("  mouseClicked returned: " + handled);
                    mouseLog.flush();
                }
                
                System.out.println("[DragonClient] Click handled by " + detectedScreen + ": " + handled);
                
                if (handled) {
                    ci.cancel(); // Cancel the event - don't pass to Screen.mouseClicked
                }
            } catch (Exception e) {
                if (mouseLog != null) {
                    mouseLog.println("  Error calling mouseClicked: " + e.getMessage());
                    e.printStackTrace(mouseLog);
                    mouseLog.flush();
                }
                System.err.println("[DragonClient] Error in MixinMouse: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (mouseLog != null) {
                mouseLog.println("  ERROR: " + e.getMessage());
                e.printStackTrace(mouseLog);
                mouseLog.flush();
            }
            System.err.println("[DragonClient] Error in MixinMouse: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
