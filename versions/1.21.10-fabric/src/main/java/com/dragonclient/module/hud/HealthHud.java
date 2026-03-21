package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class HealthHud extends HudModule {
    
    public HealthHud() {
        super("Health", "Displays health");
        this.x = 24;
        this.y = 166;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        float health = client.player.getHealth();
        float maxHealth = client.player.getMaxHealth();
        
        String text = String.format("❤ %.1f/%.1f", health, maxHealth);
        
        int textWidth = client.textRenderer.getWidth(text);
        int textHeight = client.textRenderer.fontHeight;
        applyDefaultTopLeft(client, 24, 166);
        drawLiquidGlassTextPanel(context, textWidth, textHeight);

        context.drawText(client.textRenderer, text, x, y, 0xFFFFFFFF, false);
        this.width = textWidth;
        this.height = textHeight;
    }
}
