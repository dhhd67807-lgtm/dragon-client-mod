package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.GameOptions;

public class KeystrokesHud extends HudModule {
    
    public KeystrokesHud() {
        super("Keystrokes", "Displays key presses");
        this.x = 450;  // Far right bottom
        this.y = 280;
        this.width = 60;
        this.height = 60;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        GameOptions options = client.options;
        
        // Calculate position from right edge
        int windowWidth = client.getWindow().getScaledWidth();
        int rightX = (windowWidth / 4) - 65;  // 65 pixels from right (60 width + 5 margin)
        
        // W key
        drawKey(context, "W", rightX + 20, y, options.forwardKey.isPressed());
        // A key
        drawKey(context, "A", rightX, y + 20, options.leftKey.isPressed());
        // S key
        drawKey(context, "S", rightX + 20, y + 20, options.backKey.isPressed());
        // D key
        drawKey(context, "D", rightX + 40, y + 20, options.rightKey.isPressed());
        // Space
        drawKey(context, "---", rightX, y + 40, options.jumpKey.isPressed(), 60);
    }

    private void drawKey(DrawContext context, String key, int x, int y, boolean pressed) {
        drawKey(context, key, x, y, pressed, 18);
    }

    private void drawKey(DrawContext context, String key, int x, int y, boolean pressed, int width) {
        // Background - darker when pressed, lighter when not pressed
        int bgColor = pressed ? 0xFF0A0A0A : 0x801D1C1C;  // Darker black when pressed
        context.fill(x, y, x + width, y + 18, bgColor);
        // Outer border - #161616 at 100% opacity
        context.fill(x, y, x + width, y + 1, 0xFF161616); // Top
        context.fill(x, y + 17, x + width, y + 18, 0xFF161616); // Bottom
        context.fill(x, y, x + 1, y + 18, 0xFF161616); // Left
        context.fill(x + width - 1, y, x + width, y + 18, 0xFF161616); // Right
        
        // Draw inset shadow - Dark gray for depth
        context.fill(x + 1, y + 1, x + width - 1, y + 2, 0x80000000); // Top inner shadow
        context.fill(x + 1, y + 1, x + 2, y + 17, 0x80000000); // Left inner shadow
        
        MinecraftClient client = MinecraftClient.getInstance();
        int textX = x + (width - client.textRenderer.getWidth(key)) / 2;
        int textY = y + 5;
        // Text - White without shadow
        context.drawText(client.textRenderer, key, textX, textY, 0xFFFFFFFF, false);
    }
}
