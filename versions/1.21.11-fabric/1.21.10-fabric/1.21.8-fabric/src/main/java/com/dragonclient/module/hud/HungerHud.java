package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class HungerHud extends HudModule {
    
    public HungerHud() {
        super("Hunger", "Displays hunger level");
        this.x = 5;
        this.y = 125;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        int hunger = client.player.getHungerManager().getFoodLevel();
        
        String text = "🍖 " + hunger;
        
        context.drawText(client.textRenderer, text, x, y, 0xFFAA00, true);
        this.width = client.textRenderer.getWidth(text);
        this.height = client.textRenderer.fontHeight;
    }
}
