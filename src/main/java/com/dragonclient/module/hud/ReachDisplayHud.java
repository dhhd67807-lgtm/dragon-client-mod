package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class ReachDisplayHud extends HudModule {
    
    public ReachDisplayHud() {
        super("Reach Display", "Displays reach distance");
        this.x = 5;
        this.y = 155;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        
        // Default reach is 3.0 blocks in survival, 4.5 in creative
        double reach = client.interactionManager != null && client.interactionManager.getCurrentGameMode().isCreative() ? 4.5 : 3.0;
        
        String text = String.format("Reach: %.1f", reach);
        
        context.drawText(client.textRenderer, text, x, y, 0xFFFFFF, true);
        this.width = client.textRenderer.getWidth(text);
        this.height = client.textRenderer.fontHeight;
    }
}
