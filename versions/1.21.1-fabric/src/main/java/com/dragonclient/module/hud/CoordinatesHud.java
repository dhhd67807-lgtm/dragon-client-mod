package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

public class CoordinatesHud extends HudModule {
    
    public CoordinatesHud() {
        super("Coordinates", "Displays player coordinates");
        this.x = 5;  // Bottom left
        this.y = 280;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        
        if (player == null) return;
        
        String coords = String.format("XYZ: %.1f / %.1f / %.1f", 
            player.getX(), player.getY(), player.getZ());
        
        // Draw background - #1D1C1C at 50% opacity
        int textWidth = client.textRenderer.getWidth(coords);
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
        context.drawText(client.textRenderer, coords, x, y, 0xFFFFFFFF, false);
        this.width = textWidth;
        this.height = textHeight;
    }
}
