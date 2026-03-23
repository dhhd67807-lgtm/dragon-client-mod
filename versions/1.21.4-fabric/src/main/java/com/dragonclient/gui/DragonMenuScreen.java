package com.dragonclient.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public class DragonMenuScreen extends Screen {
    private static final int MENU_WIDTH = 200;
    private static final int MENU_HEIGHT = 280;
    private static final int FIXED_GUI_SCALE = 2;
    
    private static final Identifier DRAGON_LOGO    = Identifier.of("dragonclient", "textures/gui/new-dragon.png");
    private static final Identifier HEADER_TEXTURE = Identifier.of("dragonclient", "textures/gui/header-menu.png");
    
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
        updateLayout(getFixedScaleFactor(client), client);
    }

    private float getFixedScaleFactor(MinecraftClient client) {
        double currentScale = client.getWindow().getScaleFactor();
        if (currentScale <= 0.0d) {
            return 1.0f;
        }
        return (float) (FIXED_GUI_SCALE / currentScale);
    }

    private void updateLayout(float scaleFactor, MinecraftClient client) {
        int fixedScaledWidth;
        int fixedScaledHeight;
        if (this.width > 0 && this.height > 0) {
            fixedScaledWidth = (int) (this.width / scaleFactor);
            fixedScaledHeight = (int) (this.height / scaleFactor);
        } else {
            fixedScaledWidth = client.getWindow().getFramebufferWidth() / FIXED_GUI_SCALE;
            fixedScaledHeight = client.getWindow().getFramebufferHeight() / FIXED_GUI_SCALE;
        }
        this.guiLeft = (fixedScaledWidth - MENU_WIDTH) / 2;
        this.guiTop = (fixedScaledHeight - MENU_HEIGHT) / 2;
    }

    // -------------------------------------------------------------------------
    // Render
    // -------------------------------------------------------------------------
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1.21.11: renderBackground can only be called once per frame - skip it here
        // The parent Screen class handles background rendering
        
        MinecraftClient client = MinecraftClient.getInstance();
        float scaleFactor = getFixedScaleFactor(client);
        updateLayout(scaleFactor, client);
        
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
            // Draw full white button with inset spacing from the button edges
            int borderInset = 2;
            context.fill(buttonX + borderInset, by + borderInset,
                         buttonX + buttonWidth - borderInset, by + buttonHeight - borderInset,
                         0xFFFFFFFF);

            // Use a solid white outline so the cards stay crisp over the background image
            drawRoundedBorder(context, buttonX + borderInset, by + borderInset,
                              buttonWidth - (borderInset * 2), buttonHeight - (borderInset * 2),
                              0xFFFFFFFF);

            int textColor = 0xFF111111;
            String labelText = labels[i];

            float textScale = 1.15f;
            int scaledTextWidth = Math.round(this.textRenderer.getWidth(labelText) * textScale);
            int scaledTextHeight = Math.round(this.textRenderer.fontHeight * textScale);
            int textX = buttonX + (buttonWidth - scaledTextWidth) / 2;
            int textY = by + (buttonHeight - scaledTextHeight) / 2;

            drawScaledLabel(context, labelText, textX, textY, textColor, textScale);
        }
    }

    // -------------------------------------------------------------------------
    // Texture helper — 1.21.3-1.21.4: Uses RenderLayer
    // -------------------------------------------------------------------------
    private void drawTexture(DrawContext context, Identifier texture,
                            int x, int y, int width, int height) {
        // 1.21.3-1.21.4: Use RenderLayer.getGuiTextured()
        context.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured, 
                          texture, x, y, 0, 0, width, height, width, height);
    }
    
    // Texture helper with color/opacity for 1.21.3-1.21.4
    private void drawTextureWithColor(DrawContext context, Identifier texture, int x, int y, int width, int height, int color) {
        // Note: RenderLayer signature doesn't support color parameter directly
        context.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured, 
                          texture, x, y, 0, 0, width, height, width, height);
    }
    private void drawScaledLabel(DrawContext context, String text, int x, int y, int color, float scale) {
        var matrices = context.getMatrices();
        boolean pushed = false;
        try {
            try {
                matrices.getClass().getMethod("pushMatrix").invoke(matrices);
            } catch (NoSuchMethodException e) {
                matrices.getClass().getMethod("push").invoke(matrices);
            }
            pushed = true;

            try {
                matrices.getClass().getMethod("scale", float.class, float.class).invoke(matrices, scale, scale);
            } catch (NoSuchMethodException e) {
                matrices.getClass().getMethod("scale", float.class, float.class, float.class).invoke(matrices, scale, scale, 1.0f);
            }

            int scaledX = Math.round(x / scale);
            int scaledY = Math.round(y / scale);
            drawLabelText(context, text, scaledX, scaledY, color);
        } catch (Exception e) {
            drawLabelText(context, text, x, y, color);
        } finally {
            if (pushed) {
                try {
                    try {
                        matrices.getClass().getMethod("popMatrix").invoke(matrices);
                    } catch (NoSuchMethodException e) {
                        matrices.getClass().getMethod("pop").invoke(matrices);
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void drawLabelText(DrawContext context, String text, int x, int y, int color) {
        try {
            context.drawText(this.textRenderer, text, x, y, color, false);
        } catch (NoSuchMethodError e) {
            try {
                java.lang.reflect.Method drawTextMethod = context.getClass().getMethod(
                    "drawText",
                    net.minecraft.client.font.TextRenderer.class,
                    String.class,
                    int.class,
                    int.class,
                    int.class
                );
                drawTextMethod.invoke(context, this.textRenderer, text, x, y, color);
            } catch (Exception ignored) {
            }
        }
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------
    // Handle clicks in fixed-scale coordinate space.
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        MinecraftClient client = MinecraftClient.getInstance();
        float scaleFactor = getFixedScaleFactor(client);
        updateLayout(scaleFactor, client);
        
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
