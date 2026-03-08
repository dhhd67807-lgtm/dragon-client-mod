package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.GameOptions;

public class KeystrokesHud extends HudModule {
    
    public KeystrokesHud() {
        super("Keystrokes", "Displays key presses");
        this.x = 5;
        this.y = 160;
        this.width = 60;
        this.height = 60;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        GameOptions options = client.options;
        
        // W key
        drawKey(context, "W", x + 20, y, options.forwardKey.isPressed());
        // A key
        drawKey(context, "A", x, y + 20, options.leftKey.isPressed());
        // S key
        drawKey(context, "S", x + 20, y + 20, options.backKey.isPressed());
        // D key
        drawKey(context, "D", x + 40, y + 20, options.rightKey.isPressed());
        // Space
        drawKey(context, "---", x, y + 40, options.jumpKey.isPressed(), 60);
    }

    private void drawKey(DrawContext context, String key, int x, int y, boolean pressed) {
        drawKey(context, key, x, y, pressed, 18);
    }

    private void drawKey(DrawContext context, String key, int x, int y, boolean pressed, int width) {
        int color = pressed ? 0x80FFFFFF : 0x80000000;
        context.fill(x, y, x + width, y + 18, color);
        context.drawBorder(x, y, width, 18, 0xFFFFFFFF);
        
        MinecraftClient client = MinecraftClient.getInstance();
        int textX = x + (width - client.textRenderer.getWidth(key)) / 2;
        int textY = y + 5;
        context.drawText(client.textRenderer, key, textX, textY, 0xFFFFFF, false);
    }
}
