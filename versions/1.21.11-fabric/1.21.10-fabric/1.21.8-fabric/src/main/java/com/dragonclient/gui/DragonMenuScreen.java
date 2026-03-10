package com.dragonclient.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DragonMenuScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("DragonClient");
    private static final int MENU_WIDTH = 200;
    private static final int MENU_HEIGHT = 320;
    private static final int TARGET_GUI_SCALE = 2;
    
    private static final Identifier DRAGON_LOGO    = Identifier.of("dragonclient", "textures/gui/new-dragon.png");
    private static final Identifier HEADER_TEXTURE = Identifier.of("dragonclient", "textures/gui/header.png");
    private static final Identifier CS_STAR_ICON   = Identifier.of("dragonclient", "textures/gui/cs_star_8.png");
    
    private int guiLeft;
    private int guiTop;
    
    public DragonMenuScreen() {
        super(Text.literal("Dragon Menu"));
    }

    // -------------------------------------------------------------------------
    // Init
    // -------------------------------------------------------------------------
    @Override
    protected void init() {
        super.init();
        MinecraftClient client = MinecraftClient.getInstance();
        
        // Calculate as if GUI scale is 2, regardless of actual scale
        int scaledWidth = client.getWindow().getFramebufferWidth() / TARGET_GUI_SCALE;
        int scaledHeight = client.getWindow().getFramebufferHeight() / TARGET_GUI_SCALE;
        this.guiLeft = (scaledWidth - MENU_WIDTH) / 2;
        this.guiTop = (scaledHeight - MENU_HEIGHT) / 2;
        
        int buttonWidth   = 160;
        int buttonHeight  = 35;
        int buttonX       = guiLeft + (MENU_WIDTH - buttonWidth) / 2;
        int startY        = guiTop + 120;
        int buttonSpacing = 45;
        
        // Invisible (zero-alpha) ButtonWidgets — they own all click/focus logic.
        // We draw our own styled backgrounds in render(); no vanilla button chrome needed.
        this.addDrawableChild(ButtonWidget.builder(Text.empty(), btn -> 
            MinecraftClient.getInstance().setScreen(new DragonClientScreen()))
            .dimensions(buttonX, startY, buttonWidth, buttonHeight)
            .build());        
        this.addDrawableChild(ButtonWidget.builder(Text.empty(), btn -> { 
            /* TODO: HUD editor */ 
        }).dimensions(buttonX, startY + buttonSpacing, buttonWidth, buttonHeight)
            .build());
        
        // Hide vanilla rendering — we paint everything ourselves
        this.children().forEach(child -> {
            if (child instanceof ButtonWidget btn) btn.setAlpha(0f);
        });
    }

    // -------------------------------------------------------------------------
    // Render
    // -------------------------------------------------------------------------
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1.21.6+: Skip renderBackground() - causes "Can only blur once per frame" error
        // Draw a dark overlay instead
        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
        
        MinecraftClient client = MinecraftClient.getInstance();
        double currentScale = client.getWindow().getScaleFactor();
        double targetScale = TARGET_GUI_SCALE;
        float scaleFactor = (float)(targetScale / currentScale);
        
        int transformedMouseX = (int)(mouseX / scaleFactor);
        int transformedMouseY = (int)(mouseY / scaleFactor);
        
        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.scale(scaleFactor, scaleFactor);
        
        renderMenu(context, transformedMouseX, transformedMouseY);
        
        matrices.popMatrix();
    }
    
    private void renderMenu(DrawContext context, int mouseX, int mouseY) {
        // Main panel
        drawRoundedRect  (context, guiLeft, guiTop, MENU_WIDTH, MENU_HEIGHT, 0xFF100C08);
        drawRoundedBorder(context, guiLeft, guiTop, MENU_WIDTH, MENU_HEIGHT, 0xFF2A2622);
        
        // Dragon logo
        int logoSize = 80;
        int logoX    = guiLeft + (MENU_WIDTH - logoSize) / 2;
        int logoY    = guiTop  + 20;
        drawTexture(context, DRAGON_LOGO, logoX, logoY, logoSize, logoSize);
        
        // Buttons
        int buttonWidth   = 160;
        int buttonHeight  = 35;
        int buttonX       = guiLeft + (MENU_WIDTH - buttonWidth) / 2;
        int startY        = guiTop  + 120;
        int buttonSpacing = 45;
        
        String[] labels    = {"MODS", "HUD"};
        
        for (int i = 0; i < labels.length; i++) {
            int     by        = startY + (i * buttonSpacing);
            boolean isHovered = mouseX >= buttonX && mouseX <= buttonX + buttonWidth
                             && mouseY >= by      && mouseY <= by + buttonHeight;
            
            // Draw header texture as button background
            // 1.21.6+: Blending and shader colors handled automatically
            drawTexture(context, HEADER_TEXTURE, buttonX, by, buttonWidth, buttonHeight);
            
            drawRoundedBorder(context, buttonX, by, buttonWidth, buttonHeight,
                             isHovered ? 0xFFFF4444 : 0xFF1A1614);
            
            // Star icon (only MODS and CAPES for now)
            int iconSize = 12;
            int textWidth = this.textRenderer.getWidth(labels[i]);
            int totalW    = (i < 2 ? iconSize + 5 : 0) + textWidth;
            int cx        = buttonX + (buttonWidth - totalW) / 2;
            
            if (i < 2) {
                // 1.21.6+: Blending and shader colors handled automatically
                drawTexture(context, CS_STAR_ICON, cx, by + 12, iconSize, iconSize);
            }
            
            int textX     = cx + (i < 2 ? iconSize + 5 : 0);
            int textColor = isHovered ? 0xFFFEFEFE : 0xFFAAAAAA;
            // Version-compatible text drawing
            try {
                context.drawText(this.textRenderer, labels[i], textX, by + 12, textColor, false);
            } catch (NoSuchMethodError e) {
                // 1.21.11: drawText without shadow parameter
                try {
                    java.lang.reflect.Method drawTextMethod = context.getClass().getMethod("drawText",
                        net.minecraft.client.font.TextRenderer.class, String.class, 
                        int.class, int.class, int.class);
                    drawTextMethod.invoke(context, this.textRenderer, labels[i], textX, by + 12, textColor);
                } catch (Exception ex) {
                    // Give up
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Texture helper — for 1.21.6+
    // -------------------------------------------------------------------------
    private void drawTexture(DrawContext context, Identifier texture,
                            int x, int y, int width, int height) {
        // 1.21.6+: Use RenderPipelines.GUI_TEXTURED for GUI texture rendering
        context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, 
                          texture, x, y, 0f, 0f, width, height, width, height);
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------
    // FIX #3: mouseClicked is fully delegated to ButtonWidget children via
    // super.mouseClicked — no manual hit-testing needed or wanted here.
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        MinecraftClient client = MinecraftClient.getInstance();
        double currentScale = client.getWindow().getScaleFactor();
        double targetScale = TARGET_GUI_SCALE;
        float scaleFactor = (float)(targetScale / currentScale);
        
        int transformedMouseX = (int)(mouseX / scaleFactor);
        int transformedMouseY = (int)(mouseY / scaleFactor);
        
        int buttonWidth   = 160;
        int buttonHeight  = 35;
        int buttonX       = guiLeft + (MENU_WIDTH - buttonWidth) / 2;
        int startY        = guiTop + 120;
        int buttonSpacing = 45;
        
        for (int i = 0; i < 2; i++) {
            int by = startY + (i * buttonSpacing);
            if (transformedMouseX >= buttonX && transformedMouseX <= buttonX + buttonWidth &&
                transformedMouseY >= by && transformedMouseY <= by + buttonHeight) {
                if (i == 0) {
                    MinecraftClient.getInstance().setScreen(new DragonClientScreen());
                } else if (i == 1) {
                    MinecraftClient.getInstance().setScreen(new HudEditorScreen());
                }
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }

    // -------------------------------------------------------------------------
    // Drawing helpers
    // -------------------------------------------------------------------------
    private void drawRoundedRect(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x + 2, y,         x + w - 2, y + h,         color);
        ctx.fill(x,     y + 2,     x + w,     y + h - 2,     color);
        ctx.fill(x + 1, y + 1,     x + 2,     y + 2,         color);
        ctx.fill(x + w - 2, y + 1, x + w - 1, y + 2,         color);
        ctx.fill(x + 1, y + h - 2, x + 2,     y + h - 1,     color);
        ctx.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, color);
    }
    
    private void drawRoundedBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x + 2,     y,         x + w - 2, y + 1,         color);
        ctx.fill(x + 2,     y + h - 1, x + w - 2, y + h,         color);
        ctx.fill(x,         y + 2,     x + 1,     y + h - 2,     color);
        ctx.fill(x + w - 1, y + 2,     x + w,     y + h - 2,     color);
        ctx.fill(x + 1,     y + 1,     x + 2,     y + 2,         color);
        ctx.fill(x + w - 2, y + 1,     x + w - 1, y + 2,         color);
        ctx.fill(x + 1,     y + h - 2, x + 2,     y + h - 1,     color);
        ctx.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1,     color);
    }
}
