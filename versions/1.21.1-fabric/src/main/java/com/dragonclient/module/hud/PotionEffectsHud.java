package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;

public class PotionEffectsHud extends HudModule {
    
    public PotionEffectsHud() {
        super("Potion Effects", "Displays active potion effects");
        this.x = 380;  // Top right, below clock
        this.y = 25;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        int yOffset = 0;
        int maxWidth = 0;
        
        for (StatusEffectInstance effect : client.player.getStatusEffects()) {
            String name = effect.getEffectType().value().getName().getString();
            int duration = effect.getDuration() / 20;
            String text = name + " " + formatTime(duration);
            
            int textWidth = client.textRenderer.getWidth(text);
            int textHeight = client.textRenderer.fontHeight;
            
            if (textWidth > maxWidth) maxWidth = textWidth;
            
            // Draw background - #1D1C1C at 50% opacity
            context.fill(x - 6, y + yOffset - 6, x + textWidth + 6, y + yOffset + textHeight + 6, 0x801D1C1C);
            
            // Draw outer border - #161616 at 100% opacity
            context.fill(x - 6, y + yOffset - 6, x + textWidth + 6, y + yOffset - 5, 0xFF161616); // Top
            context.fill(x - 6, y + yOffset + textHeight + 5, x + textWidth + 6, y + yOffset + textHeight + 6, 0xFF161616); // Bottom
            context.fill(x - 6, y + yOffset - 6, x - 5, y + yOffset + textHeight + 6, 0xFF161616); // Left
            context.fill(x + textWidth + 5, y + yOffset - 6, x + textWidth + 6, y + yOffset + textHeight + 6, 0xFF161616); // Right
            
            // Draw inset shadow - Dark gray for depth
            context.fill(x - 5, y + yOffset - 5, x + textWidth + 5, y + yOffset - 4, 0x80000000); // Top inner shadow
            context.fill(x - 5, y + yOffset - 5, x - 4, y + yOffset + textHeight + 5, 0x80000000); // Left inner shadow
            
            // Draw text - White at 100% opacity without shadow
            context.drawText(client.textRenderer, text, x, y + yOffset, 0xFFFFFFFF, false);
            yOffset += textHeight + 2;
        }
        
        this.width = maxWidth;
        this.height = yOffset;
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }
}
