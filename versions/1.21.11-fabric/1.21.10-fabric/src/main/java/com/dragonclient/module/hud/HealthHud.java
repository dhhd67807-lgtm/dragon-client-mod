package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class HealthHud extends HudModule {
    
    public HealthHud() {
        super("Health", "Displays health");
        this.x = 5;
        this.y = 110;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        float health = client.player.getHealth();
        float maxHealth = client.player.getMaxHealth();
        
        String text = String.format("❤ %.1f/%.1f", health, maxHealth);
        
        context.drawText(client.textRenderer, text, x, y, 0xFF5555, true);
        this.width = client.textRenderer.getWidth(text);
        this.height = client.textRenderer.fontHeight;
    }
}
