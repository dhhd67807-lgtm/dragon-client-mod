package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

public class CoordinatesHud extends HudModule {
    
    public CoordinatesHud() {
        super("Coordinates", "Displays player coordinates");
        this.x = 24;
        this.y = 22;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        
        if (player == null) return;
        
        String coords = String.format("XYZ: %.1f / %.1f / %.1f", 
            player.getX(), player.getY(), player.getZ());
        
        int textWidth = client.textRenderer.getWidth(coords);
        int textHeight = client.textRenderer.fontHeight;
        applyDefaultTopLeft(client, 24, 22);
        drawLiquidGlassTextPanel(context, textWidth, textHeight);

        context.drawText(client.textRenderer, coords, x, y, 0xFFFFFFFF, false);
        this.width = textWidth;
        this.height = textHeight;
    }
}
