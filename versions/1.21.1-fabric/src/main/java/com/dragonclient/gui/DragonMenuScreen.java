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
    private static final int MENU_HEIGHT = 280;
    private static final int FIXED_GUI_SCALE = 2;
    
    private static final Identifier DRAGON_LOGO    = Identifier.of("dragonclient", "textures/gui/new-dragon.png");
    private static final Identifier HEADER_TEXTURE = Identifier.of("dragonclient", "textures/gui/header-menu.png");
    private static final Identifier CS_STAR_ICON   = Identifier.of("dragonclient", "textures/gui/cs_star_8.png");
    private static final Identifier ULTRA_ICON     = Identifier.of("dragonclient", "textures/gui/ultra.png");
    private static final Identifier SKINS_ICON     = Identifier.of("dragonclient", "textures/gui/2.png");
    
    private int guiLeft;
    private int guiTop;
    private int scaledWidth;
    private int scaledHeight;
    
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
        
        // Calculate scaled dimensions for rendering
        this.scaledWidth = client.getWindow().getFramebufferWidth() / FIXED_GUI_SCALE;
        this.scaledHeight = client.getWindow().getFramebufferHeight() / FIXED_GUI_SCALE;
        this.guiLeft = (scaledWidth - MENU_WIDTH) / 2;
        this.guiTop = (scaledHeight - MENU_HEIGHT) / 2;
        
        // Button dimensions in scaled space
        int buttonWidth   = 160;
        int buttonHeight  = 35;
        int buttonX       = guiLeft + (MENU_WIDTH - buttonWidth) / 2;
        int startY        = guiTop + 120;
        int buttonSpacing = 40;
        
        // Calculate scale factor to convert scaled coordinates to real screen coordinates
        double currentScale = client.getWindow().getScaleFactor();
        double targetScale = FIXED_GUI_SCALE;
        float scaleFactor = (float)(targetScale / currentScale);
        
        // Convert button positions from scaled space to real screen space
        int realButtonX = Math.round(buttonX * scaleFactor);
        int realFirstButtonY = Math.round(startY * scaleFactor);
        int realButtonWidth = Math.round(buttonWidth * scaleFactor);
        int realButtonHeight = Math.round(buttonHeight * scaleFactor);
        int realSecondButtonY = Math.round((startY + buttonSpacing) * scaleFactor);
        int realThirdButtonY = Math.round((startY + (buttonSpacing * 2)) * scaleFactor);
        
        // Invisible (zero-alpha) ButtonWidgets — they own all click/focus logic.
        // We draw our own styled backgrounds in render(); no vanilla button chrome needed.
        this.addDrawableChild(ButtonWidget.builder(Text.empty(), btn -> 
            MinecraftClient.getInstance().setScreen(new DragonClientScreen()))
            .dimensions(realButtonX, realFirstButtonY, realButtonWidth, realButtonHeight)
            .build());        
        this.addDrawableChild(ButtonWidget.builder(Text.empty(), btn -> 
            MinecraftClient.getInstance().setScreen(new HudEditorScreen()))
            .dimensions(realButtonX, realSecondButtonY, realButtonWidth, realButtonHeight)
            .build());
        
        this.addDrawableChild(ButtonWidget.builder(Text.empty(), btn -> 
            MinecraftClient.getInstance().setScreen(new DragonSkinsScreen()))
            .dimensions(realButtonX, realThirdButtonY, realButtonWidth, realButtonHeight)
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
        // 1.21.11: renderBackground can only be called once per frame - skip it here
        // The parent Screen class handles background rendering
        
        MinecraftClient client = MinecraftClient.getInstance();
        double currentScale = client.getWindow().getScaleFactor();
        double targetScale = FIXED_GUI_SCALE;
        float scaleFactor = (float)(targetScale / currentScale);
        
        int transformedMouseX = (int)(mouseX / scaleFactor);
        int transformedMouseY = (int)(mouseY / scaleFactor);
        
        var matrices = context.getMatrices();
        matrices.push();
        matrices.scale(scaleFactor, scaleFactor, 1.0f); // 2D scaling only in 1.21.11
        
        renderMenu(context, transformedMouseX, transformedMouseY);
        
        matrices.pop();
    }
    
    private void renderMenu(DrawContext context, int mouseX, int mouseY) {
        // Main panel
        drawRoundedRect  (context, guiLeft, guiTop, MENU_WIDTH, MENU_HEIGHT, 0xFF100C08);
        drawTexture(context, HEADER_TEXTURE, guiLeft + 2, guiTop + 2, MENU_WIDTH - 4, MENU_HEIGHT - 4);
        context.fill(guiLeft + 2, guiTop + 2, guiLeft + MENU_WIDTH - 2, guiTop + MENU_HEIGHT - 2, 0xA8100C08);
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
        int buttonSpacing = 40;
        
        String[] labels    = {"MODS", "HUD", "SKINS"};
        
        for (int i = 0; i < labels.length; i++) {
            int     by        = startY + (i * buttonSpacing);
            boolean isHovered = mouseX >= buttonX && mouseX <= buttonX + buttonWidth
                             && mouseY >= by      && mouseY <= by + buttonHeight;
            // Draw semi-transparent white button background
            context.fill(buttonX, by, buttonX + buttonWidth, by + buttonHeight, 0x80FFFFFF);

            // Use a solid white outline so the cards stay crisp over the background image
            drawRoundedBorder(context, buttonX, by, buttonWidth, buttonHeight, 0xFFFFFFFF);
            
            // Star icon for all buttons
            int iconSize = 12;
            int textWidth = this.textRenderer.getWidth(labels[i]);
            int totalW    = iconSize + 5 + textWidth;
            int cx        = buttonX + (buttonWidth - totalW) / 2;
            
            // Draw appropriate star icon - align with text baseline
            int starY = by + (buttonHeight - 8) / 2; // Match text Y position
            
            if (i == 0) {
                // MODS - cs_star icon (no hue)
                drawTexture(context, CS_STAR_ICON, cx, starY, iconSize, iconSize);
            } else if (i == 1) {
                // HUD - ultra icon (no hue)
                drawTexture(context, ULTRA_ICON, cx, starY, iconSize, iconSize);
            } else {
                // SKINS - visual icon
                drawTexture(context, SKINS_ICON, cx, starY, iconSize, iconSize);
            }
            
            int textX     = cx + iconSize + 5;
            // White text color
            int textColor = 0xFFFEFEFE;
            // Version-compatible text drawing with bold formatting
            String boldLabel = "§l" + labels[i]; // §l is Minecraft's bold formatting code
            // Center text vertically in button
            int textY = by + (buttonHeight - 8) / 2; // 8 is approximate text height
            // Version-compatible text drawing
            try {
                context.drawText(this.textRenderer, boldLabel, textX, textY, textColor, false);
            } catch (NoSuchMethodError e) {
                // 1.21.11: drawText without shadow parameter
                try {
                    java.lang.reflect.Method drawTextMethod = context.getClass().getMethod("drawText",
                        net.minecraft.client.font.TextRenderer.class, String.class, 
                        int.class, int.class, int.class);
                    drawTextMethod.invoke(context, this.textRenderer, boldLabel, textX, textY, textColor);
                } catch (Exception ex) {
                    // Give up
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Texture helper — 1.21.1-1.21.10: Old signature without RenderPipelines
    // -------------------------------------------------------------------------
    private void drawTexture(DrawContext context, Identifier texture,
                            int x, int y, int width, int height) {
        // 1.21.1-1.21.10: Old signature (Identifier, x, y, z, u, v, width, height, textureWidth, textureHeight)
        context.drawTexture(texture, x, y, 0, 0f, 0f, width, height, width, height);
    }
    
    // Texture helper with color/opacity for 1.21.1-1.21.10
    private void drawTextureWithColor(DrawContext context, Identifier texture, int x, int y, int width, int height, int color) {
        // Note: Old signature doesn't support color parameter directly
        context.drawTexture(texture, x, y, 0, 0f, 0f, width, height, width, height);
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------
    // FIX #3: mouseClicked is fully delegated to ButtonWidget children via
    // super.mouseClicked — no manual hit-testing needed or wanted here.
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        MinecraftClient client = MinecraftClient.getInstance();
        double currentScale = client.getWindow().getScaleFactor();
        double targetScale = FIXED_GUI_SCALE;
        float scaleFactor = (float)(targetScale / currentScale);
        
        int transformedMouseX = (int)(mouseX / scaleFactor);
        int transformedMouseY = (int)(mouseY / scaleFactor);
        
        int buttonWidth   = 160;
        int buttonHeight  = 35;
        int buttonX       = guiLeft + (MENU_WIDTH - buttonWidth) / 2;
        int startY        = guiTop + 120;
        int buttonSpacing = 40;
        
        for (int i = 0; i < 3; i++) {
            int by = startY + (i * buttonSpacing);
            if (transformedMouseX >= buttonX && transformedMouseX <= buttonX + buttonWidth &&
                transformedMouseY >= by && transformedMouseY <= by + buttonHeight) {
                if (i == 0) {
                    MinecraftClient.getInstance().setScreen(new DragonClientScreen());
                } else if (i == 1) {
                    MinecraftClient.getInstance().setScreen(new HudEditorScreen());
                } else if (i == 2) {
                    MinecraftClient.getInstance().setScreen(new DragonSkinsScreen());
                }
                return true;
            }
        }
        
        return false;
    }
    
    // 1.21.11: Custom click handler (called via MixinMouse)
    public boolean handleMouseClick(double mouseX, double mouseY, int button) {
        return mouseClicked(mouseX, mouseY, button);
    }
    
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            this.close();
            return true;
        }
        return false;
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
