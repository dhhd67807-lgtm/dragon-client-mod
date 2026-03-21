package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class HealthHud extends HudModule {
    
    public HealthHud() {
        super("Health", "Displays health");
        this.x = 5;  // Top left, below ping
        this.y = 65;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        float health = client.player.getHealth();
        float maxHealth = client.player.getMaxHealth();
        
        String text = String.format("❤ %.1f/%.1f", health, maxHealth);
        
        // Draw background - #1D1C1C at 50% opacity
        int textWidth = client.textRenderer.getWidth(text);
        int textHeight = client.textRenderer.fontHeight;
        context.fill(x - 8, y - 8, x + textWidth + 8, y + textHeight + 8, 0x551D1C1C);
        
        // Draw outer border - #161616 at 100% opacity
        context.fill(x - 8, y - 8, x + textWidth + 8, y - 7, 0xFF161616); // Top
        context.fill(x - 8, y + textHeight + 7, x + textWidth + 8, y + textHeight + 8, 0xFF161616); // Bottom
        context.fill(x - 8, y - 8, x - 7, y + textHeight + 8, 0xFF161616); // Left
        context.fill(x + textWidth + 7, y - 8, x + textWidth + 8, y + textHeight + 8, 0xFF161616); // Right
        
        // Draw inset shadow - Dark gray for depth
        context.fill(x - 7, y - 7, x + textWidth + 7, y - 6, 0x50000000); // Top inner shadow
        context.fill(x - 7, y - 7, x - 6, y + textHeight + 7, 0x50000000); // Left inner shadow
        
        // Draw text - White at 100% opacity without shadow
        context.drawText(client.textRenderer, text, x, y, 0xFFFFFFFF, false);
        this.width = textWidth;
        this.height = textHeight;
    }
}
