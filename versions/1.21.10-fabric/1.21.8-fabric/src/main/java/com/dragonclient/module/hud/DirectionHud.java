package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

public class DirectionHud extends HudModule {
    
    public DirectionHud() {
        super("Direction", "Displays facing direction");
        this.x = 5;
        this.y = 65;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        float yaw = MathHelper.wrapDegrees(client.player.getYaw());
        String direction = getDirection(yaw);
        String text = direction + " (" + String.format("%.1f", yaw) + "°)";
        
        context.drawText(client.textRenderer, text, x, y, 0xFFFFFF, true);
        this.width = client.textRenderer.getWidth(text);
        this.height = client.textRenderer.fontHeight;
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
