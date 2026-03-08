package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;

public class PotionEffectsHud extends HudModule {
    
    public PotionEffectsHud() {
        super("Potion Effects", "Displays active potion effects");
        this.x = 5;
        this.y = 120;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        int yOffset = 0;
        for (StatusEffectInstance effect : client.player.getStatusEffects()) {
            String name = effect.getEffectType().value().getName().getString();
            int duration = effect.getDuration() / 20; // Convert ticks to seconds
            String text = name + " " + formatTime(duration);
            
            context.drawText(client.textRenderer, text, x, y + yOffset, 0xFFFFFF, true);
            yOffset += client.textRenderer.fontHeight + 2;
        }
        
        this.height = yOffset;
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }
}
