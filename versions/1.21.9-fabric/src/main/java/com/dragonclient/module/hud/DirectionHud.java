package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

public class DirectionHud extends HudModule {
    
    public DirectionHud() {
        super("Direction", "Displays facing direction");
        this.x = 5;  // Bottom left, below coordinates
        this.y = 300;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        float yaw = MathHelper.wrapDegrees(client.player.getYaw());
        String direction = getDirection(yaw);
        String text = direction + " (" + String.format("%.1f", yaw) + "°)";
        
        // Draw background - #1D1C1C at 50% opacity
        int textWidth = client.textRenderer.getWidth(text);
        int textHeight = client.textRenderer.fontHeight;
        context.fill(x - 6, y - 6, x + textWidth + 6, y + textHeight + 6, 0x801D1C1C);
        
        // Draw outer border - #161616 at 100% opacity
        context.fill(x - 6, y - 6, x + textWidth + 6, y - 5, 0xFF161616); // Top
        context.fill(x - 6, y + textHeight + 5, x + textWidth + 6, y + textHeight + 6, 0xFF161616); // Bottom
        context.fill(x - 6, y - 6, x - 5, y + textHeight + 6, 0xFF161616); // Left
        context.fill(x + textWidth + 5, y - 6, x + textWidth + 6, y + textHeight + 6, 0xFF161616); // Right
        
        // Draw inset shadow - Dark gray for depth
        context.fill(x - 5, y - 5, x + textWidth + 5, y - 4, 0x80000000); // Top inner shadow
        context.fill(x - 5, y - 5, x - 4, y + textHeight + 5, 0x80000000); // Left inner shadow
        
        // Draw text - White at 100% opacity without shadow
        context.drawText(client.textRenderer, text, x, y, 0xFFFFFFFF, false);
        this.width = textWidth;
        this.height = textHeight;
    }

    private String getDirection(float yaw) {
        if (yaw < 0) yaw += 360;
        
        if (yaw >= 337.5 || yaw < 22.5) return "South";
        if (yaw >= 22.5 && yaw < 67.5) return "South-West";
        if (yaw >= 67.5 && yaw < 112.5) return "West";
        if (yaw >= 112.5 && yaw < 157.5) return "North-West";
        if (yaw >= 157.5 && yaw < 202.5) return "North";
        if (yaw >= 202.5 && yaw < 247.5) return "North-East";
        if (yaw >= 247.5 && yaw < 292.5) return "East";
        return "South-East";
    }
}
