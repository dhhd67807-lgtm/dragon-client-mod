package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

public class CoordinatesHud extends HudModule {
    
    public CoordinatesHud() {
        super("Coordinates", "Displays player coordinates");
        this.x = 5;
        this.y = 35;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        
        if (player == null) return;
        
        String coords = String.format("XYZ: %.1f / %.1f / %.1f", 
            player.getX(), player.getY(), player.getZ());
        
        context.drawText(client.textRenderer, coords, x, y, 0xFFFFFF, true);
        this.width = client.textRenderer.getWidth(coords);
        this.height = client.textRenderer.fontHeight;
    }
}
