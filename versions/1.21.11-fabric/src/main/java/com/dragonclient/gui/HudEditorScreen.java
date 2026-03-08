package com.dragonclient.gui;

import com.dragonclient.DragonClientMod;
import com.dragonclient.module.Module;
import com.dragonclient.module.hud.HudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Paths;

public class HudEditorScreen extends Screen {
    private static PrintWriter debugLog;
    private HudModule selectedModule = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private static final Identifier DRAGON_LOGO = Identifier.of("dragonclient", "textures/gui/new-dragon.png");
    
    static {
        try {
            String logPath = Paths.get(System.getProperty("user.home"), "hud-editor-debug.log").toString();
            debugLog = new PrintWriter(new FileWriter(logPath, false), true); // false = overwrite each session
            debugLog.println("=== HUD Editor Debug Log Started ===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HudEditorScreen() {
        super(Text.literal("HUD Editor"));
        if (debugLog != null) {
            debugLog.println("=== HudEditorScreen created ===");
            debugLog.flush();
        }
    }
    
    @Override
    protected void init() {
        super.init();
        if (debugLog != null) {
            debugLog.println("=== HudEditorScreen init() called ===");
            debugLog.println("  Screen size: " + this.width + "x" + this.height);
            debugLog.flush();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw semi-transparent background
        context.fill(0, 0, this.width, this.height, 0x80000000);
        
        MinecraftClient client = MinecraftClient.getInstance();
        
        // Use the SAME scaling as in-game HUD rendering (from MixinInGameHud)
        double guiScale = client.getWindow().getScaleFactor();
        float baseScale = 4.0f;  // Base 4x scale (same as in-game HUD)
        float hudScale = baseScale / (float)guiScale;
        
        if (debugLog != null) {
            debugLog.println("RENDER: screenWidth=" + this.width + " screenHeight=" + this.height);
            debugLog.println("  guiScale=" + guiScale + " baseScale=" + baseScale + " hudScale=" + hudScale);
            debugLog.println("  mouseX=" + mouseX + " mouseY=" + mouseY);
        }
        
        // Handle dragging in render loop
        if (selectedModule != null && selectedModule.isDragging()) {
            int transformedMouseX = (int)(mouseX / hudScale);
            int transformedMouseY = (int)(mouseY / hudScale);
            
            int newX = transformedMouseX - dragOffsetX;
            int newY = transformedMouseY - dragOffsetY;
            
            selectedModule.setX(newX);
            selectedModule.setY(newY);
        }
        
        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.scale(hudScale, hudScale);
        
        // Render all HUD modules with scaling (same as HudRenderer does in-game)
        int moduleCount = 0;
        for (Module module : DragonClientMod.getInstance().getModuleManager().getEnabledModules()) {
            if (module instanceof HudModule) {
                HudModule hudModule = (HudModule) module;
                moduleCount++;
                
                if (debugLog != null) {
                    debugLog.println("  Module " + moduleCount + ": " + hudModule.getName() + 
                                   " x=" + hudModule.getX() + " y=" + hudModule.getY() + 
                                   " w=" + hudModule.getWidth() + " h=" + hudModule.getHeight() +
                                   " scale=" + hudModule.getScale() + " selected=" + (hudModule == selectedModule));
                }
                
                // Apply module-specific scaling (same as HudRenderer)
                matrices.pushMatrix();
                
                float moduleScale = hudModule.getScale() / 4.0f; // Normalize to base scale (4.0)
                matrices.translate(hudModule.getX(), hudModule.getY());
                matrices.scale(moduleScale, moduleScale);
                matrices.translate(-hudModule.getX(), -hudModule.getY());
                
                // Render the HUD module
                hudModule.render(context, delta);
                
                matrices.popMatrix();
                
                // Draw semi-transparent overlay for non-selected modules
                boolean isSelected = (hudModule == selectedModule);
                if (!isSelected) {
                    int x = hudModule.getX();
                    int y = hudModule.getY();
                    int w = (int)(hudModule.getWidth() * moduleScale);
                    int h = (int)(hudModule.getHeight() * moduleScale);
                    // Draw 50% black overlay to simulate reduced opacity
                    context.fill(x, y, x + w, y + h, 0x80000000);
                }
            }
        }
        
        if (debugLog != null) {
            debugLog.println("  Total HUD modules rendered: " + moduleCount);
            debugLog.flush();
        }
        
        matrices.popMatrix();
        
        // Draw centered instruction with dragon logo at screen center
        int logoSize = 60;
        String instruction = "Press ESC to save and exit";
        int textWidth = this.textRenderer.getWidth(instruction);
        int totalHeight = logoSize + 10 + this.textRenderer.fontHeight; // logo + gap + text
        
        // Center both horizontally and vertically
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startY = centerY - totalHeight / 2; // Center vertically
        
        // Draw dragon logo
        int logoX = centerX - logoSize / 2;
        context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
                          DRAGON_LOGO, logoX, startY, 0f, 0f, logoSize, logoSize, logoSize, logoSize, 0xFFFFFFFF);
        
        // Draw text below logo
        int textX = centerX - textWidth / 2;
        int textY = startY + logoSize + 10;
        context.drawText(this.textRenderer, instruction, textX, textY, 0xFFFFFFFF, true);
        
        if (selectedModule != null) {
            String info = String.format("%s - Scale: %.1fx", selectedModule.getName(), selectedModule.getScale());
            context.drawText(this.textRenderer, info, 10, 55, 0xFF00FF00, true);
        }
        
        super.render(context, mouseX, mouseY, delta);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (debugLog != null) {
            debugLog.println("\n=== MOUSE_CLICKED EVENT ===");
            debugLog.println("  button=" + button + " mouseX=" + mouseX + " mouseY=" + mouseY);
            debugLog.flush();
        }
        
        // Accept both button 0 (left) and button 1 (which might be left on Mac)
        if (button == 0 || button == 1) {
            MinecraftClient client = MinecraftClient.getInstance();
            
            // Use the SAME scaling as in-game HUD rendering
            double guiScale = client.getWindow().getScaleFactor();
            float baseScale = 4.0f;
            float hudScale = baseScale / (float)guiScale;
            
            // Transform mouse coordinates to HUD space
            int transformedMouseX = (int)(mouseX / hudScale);
            int transformedMouseY = (int)(mouseY / hudScale);
            
            if (debugLog != null) {
                debugLog.println("  hudScale=" + hudScale);
                debugLog.println("  transformedMouseX=" + transformedMouseX + " transformedMouseY=" + transformedMouseY);
            }
            
            System.out.println("HUD Editor Click: mouseX=" + transformedMouseX + " mouseY=" + transformedMouseY + " scale=" + hudScale);
            
            // Check if clicking on any HUD module
            int checkCount = 0;
            for (Module module : DragonClientMod.getInstance().getModuleManager().getEnabledModules()) {
                if (module instanceof HudModule) {
                    HudModule hudModule = (HudModule) module;
                    int x = hudModule.getX();
                    int y = hudModule.getY();
                    float moduleScale = hudModule.getScale() / 4.0f; // Normalize to base scale
                    int w = (int)(hudModule.getWidth() * moduleScale);
                    int h = (int)(hudModule.getHeight() * moduleScale);
                    
                    checkCount++;
                    boolean isInside = transformedMouseX >= x && transformedMouseX <= x + w && 
                                      transformedMouseY >= y && transformedMouseY <= y + h;
                    
                    if (debugLog != null) {
                        debugLog.println("  Check " + checkCount + ": " + hudModule.getName() + 
                                       " x=" + x + " y=" + y + " w=" + w + " h=" + h + 
                                       " inside=" + isInside);
                    }
                    
                    System.out.println("  Checking " + hudModule.getName() + ": x=" + x + " y=" + y + " w=" + w + " h=" + h);
                    
                    if (isInside) {
                        selectedModule = hudModule;
                        hudModule.setDragging(true);
                        dragOffsetX = transformedMouseX - x;
                        dragOffsetY = transformedMouseY - y;
                        
                        if (debugLog != null) {
                            debugLog.println("  SELECTED: " + hudModule.getName() + " dragOffsetX=" + dragOffsetX + " dragOffsetY=" + dragOffsetY);
                            debugLog.flush();
                        }
                        
                        System.out.println("  SELECTED: " + hudModule.getName());
                        return true;
                    }
                }
            }
            
            selectedModule = null;
            if (debugLog != null) {
                debugLog.println("  No module selected (checked " + checkCount + " modules)");
                debugLog.flush();
            }
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (debugLog != null) {
            debugLog.println("\nMOUSE_RELEASED: button=" + button + " mouseX=" + mouseX + " mouseY=" + mouseY);
            if (selectedModule != null) {
                debugLog.println("  Released module: " + selectedModule.getName());
            }
            debugLog.flush();
        }
        
        if ((button == 0 || button == 1) && selectedModule != null) {
            selectedModule.setDragging(false);
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (debugLog != null) {
            debugLog.println("\n=== MOUSE_DRAGGED EVENT ===");
            debugLog.println("  button=" + button + " mouseX=" + mouseX + " mouseY=" + mouseY);
            debugLog.println("  deltaX=" + deltaX + " deltaY=" + deltaY);
            if (selectedModule != null) {
                debugLog.println("  selectedModule=" + selectedModule.getName() + " dragging=" + selectedModule.isDragging());
            } else {
                debugLog.println("  No module selected");
            }
            debugLog.flush();
        }
        
        if ((button == 0 || button == 1) && selectedModule != null && selectedModule.isDragging()) {
            MinecraftClient client = MinecraftClient.getInstance();
            float scaleX = client.getWindow().getScaledWidth() / 1920f;
            float scaleY = client.getWindow().getScaledHeight() / 1080f;
            float hudScale = Math.min(scaleX, scaleY);
            
            // Transform mouse coordinates to HUD space
            int transformedMouseX = (int)(mouseX / hudScale);
            int transformedMouseY = (int)(mouseY / hudScale);
            
            int newX = transformedMouseX - dragOffsetX;
            int newY = transformedMouseY - dragOffsetY;
            
            if (debugLog != null) {
                debugLog.println("MOUSE_DRAGGED: " + selectedModule.getName());
                debugLog.println("  mouseX=" + mouseX + " mouseY=" + mouseY);
                debugLog.println("  transformedMouseX=" + transformedMouseX + " transformedMouseY=" + transformedMouseY);
                debugLog.println("  dragOffsetX=" + dragOffsetX + " dragOffsetY=" + dragOffsetY);
                debugLog.println("  oldX=" + selectedModule.getX() + " oldY=" + selectedModule.getY());
                debugLog.println("  newX=" + newX + " newY=" + newY);
                debugLog.flush();
            }
            
            System.out.println("Dragging " + selectedModule.getName() + " to x=" + newX + " y=" + newY);
            
            selectedModule.setX(newX);
            selectedModule.setY(newY);
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (debugLog != null) {
            debugLog.println("\nMOUSE_SCROLLED: verticalAmount=" + verticalAmount);
            if (selectedModule != null) {
                debugLog.println("  Module: " + selectedModule.getName());
                debugLog.println("  oldScale=" + selectedModule.getScale());
            } else {
                debugLog.println("  No module selected");
            }
        }
        
        if (selectedModule != null) {
            float oldScale = selectedModule.getScale();
            float newScale = selectedModule.getScale() + (float)(verticalAmount * 0.2); // Increased increment for 3x-5x range
            selectedModule.setScale(newScale);
            
            if (debugLog != null) {
                debugLog.println("  newScale=" + selectedModule.getScale() + " (requested=" + newScale + ")");
                debugLog.flush();
            }
            
            return true;
        }
        return false;
    }

    @Override
    public void close() {
        // Save config when closing HUD editor
        com.dragonclient.DragonClientMod.getInstance().getModuleManager().saveConfig();
        super.close();
    }

    public boolean shouldPause() {
        return false;
    }
}
