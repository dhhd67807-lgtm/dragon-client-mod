package com.dragonclient.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Paths;

@Mixin(Mouse.class)
public abstract class MixinMouse {
    
    private static PrintWriter mouseLog;
    
    static {
        try {
            String logPath = Paths.get(System.getProperty("user.home"), "mouse-mixin-debug.log").toString();
            mouseLog = new PrintWriter(new FileWriter(logPath, false), true);
            mouseLog.println("=== Mouse Mixin Debug Log Started ===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Shadow
    private double x;
    
    @Shadow
    private double y;
    
    // 1.21.1-1.21.10 use old signature: onMouseButton(long window, int button, int action, int mods)
    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButtonHead(long window, int button, int action, int mods, CallbackInfo ci) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            
            if (mouseLog != null) {
                mouseLog.println("\n=== onMouseButton called ===");
                mouseLog.println("  window=" + window + " button=" + button + " action=" + action);
                mouseLog.println("  client=" + (client != null ? "present" : "null"));
                mouseLog.println("  currentScreen=" + (client != null && client.currentScreen != null ? 
                    client.currentScreen.getClass().getSimpleName() : "null"));
            }
            
            if (client == null || client.currentScreen == null) {
                if (mouseLog != null) {
                    mouseLog.println("  SKIP: No client or screen");
                    mouseLog.flush();
                }
                return;
            }
            
            Screen screen = client.currentScreen;
            String screenName = screen.getClass().getSimpleName();
            
            if (mouseLog != null) {
                mouseLog.println("  Screen type: " + screenName);
            }
            
            // Handle HudEditorScreen and DragonClientScreen
            if (!screenName.equals("HudEditorScreen") && !screenName.equals("DragonClientScreen") && !screenName.equals("DragonSkinsScreen")) {
                if (mouseLog != null) {
                    mouseLog.println("  SKIP: Not HudEditorScreen, DragonClientScreen or DragonSkinsScreen");
                    mouseLog.flush();
                }
                return;
            }
            
            // Calculate scaled mouse coordinates
            double mouseX = this.x * (double)client.getWindow().getScaledWidth() / (double)client.getWindow().getWidth();
            double mouseY = this.y * (double)client.getWindow().getScaledHeight() / (double)client.getWindow().getHeight();
            
            if (mouseLog != null) {
                mouseLog.println("  " + screenName + " detected!");
                mouseLog.println("  button=" + button + " action=" + action);
                mouseLog.println("  mouseX=" + mouseX + " mouseY=" + mouseY);
            }
            
            System.out.println("[DragonClient] Mouse event - button=" + button + " action=" + action + " x=" + mouseX + " y=" + mouseY);
            
            // action: 1 = press, 0 = release
            if (action == 1) {
                // Mouse press - try to call mouseClicked via reflection
                if (mouseLog != null) {
                    mouseLog.println("  Calling screen.mouseClicked() via reflection");
                }
                try {
                    java.lang.reflect.Method method = screen.getClass().getMethod("mouseClicked", double.class, double.class, int.class);
                    boolean handled = (boolean) method.invoke(screen, mouseX, mouseY, button);
                    if (mouseLog != null) {
                        mouseLog.println("  mouseClicked (DDI) returned: " + handled);
                        mouseLog.flush();
                    }
                } catch (NoSuchMethodException e) {
                    if (mouseLog != null) {
                        mouseLog.println("  mouseClicked (DDI) not found");
                        mouseLog.flush();
                    }
                } catch (Exception e) {
                    if (mouseLog != null) {
                        mouseLog.println("  Error calling mouseClicked: " + e.getMessage());
                        e.printStackTrace(mouseLog);
                        mouseLog.flush();
                    }
                }
            } else if (action == 0) {
                // Mouse release - try to call mouseReleased via reflection
                if (mouseLog != null) {
                    mouseLog.println("  Calling screen.mouseReleased() via reflection");
                }
                try {
                    java.lang.reflect.Method method = screen.getClass().getMethod("mouseReleased", double.class, double.class, int.class);
                    boolean handled = (boolean) method.invoke(screen, mouseX, mouseY, button);
                    if (mouseLog != null) {
                        mouseLog.println("  mouseReleased (DDI) returned: " + handled);
                        mouseLog.flush();
                    }
                } catch (NoSuchMethodException e) {
                    if (mouseLog != null) {
                        mouseLog.println("  mouseReleased (DDI) not found");
                        mouseLog.flush();
                    }
                } catch (Exception e) {
                    if (mouseLog != null) {
                        mouseLog.println("  Error calling mouseReleased: " + e.getMessage());
                        e.printStackTrace(mouseLog);
                        mouseLog.flush();
                    }
                }
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
    
    // Handle mouse scroll for HUD editor scaling
    @Inject(method = "onMouseScroll", at = @At("HEAD"))
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            
            if (client == null || client.currentScreen == null) {
                return;
            }
            
            Screen screen = client.currentScreen;
            String screenName = screen.getClass().getSimpleName();
            
            // Only handle HudEditorScreen, DragonClientScreen and DragonSkinsScreen
            if (!screenName.equals("HudEditorScreen") && !screenName.equals("DragonClientScreen") && !screenName.equals("DragonSkinsScreen")) {
                return;
            }
            
            // Calculate scaled mouse coordinates
            double mouseX = this.x * (double)client.getWindow().getScaledWidth() / (double)client.getWindow().getWidth();
            double mouseY = this.y * (double)client.getWindow().getScaledHeight() / (double)client.getWindow().getHeight();
            
            if (mouseLog != null) {
                mouseLog.println("\n=== onMouseScroll called ===");
                mouseLog.println("  Screen: " + screenName);
                mouseLog.println("  horizontal=" + horizontal + " vertical=" + vertical);
                mouseLog.println("  mouseX=" + mouseX + " mouseY=" + mouseY);
            }
            
            // Call mouseScrolled via reflection
            try {
                java.lang.reflect.Method method = screen.getClass().getMethod("mouseScrolled", 
                    double.class, double.class, double.class, double.class);
                boolean handled = (boolean) method.invoke(screen, mouseX, mouseY, horizontal, vertical);
                if (mouseLog != null) {
                    mouseLog.println("  mouseScrolled returned: " + handled);
                    mouseLog.flush();
                }
            } catch (Exception e) {
                if (mouseLog != null) {
                    mouseLog.println("  Error calling mouseScrolled: " + e.getMessage());
                    e.printStackTrace(mouseLog);
                    mouseLog.flush();
                }
            }
        } catch (Exception e) {
            if (mouseLog != null) {
                mouseLog.println("  ERROR in onMouseScroll: " + e.getMessage());
                e.printStackTrace(mouseLog);
                mouseLog.flush();
            }
        }
    }
}
