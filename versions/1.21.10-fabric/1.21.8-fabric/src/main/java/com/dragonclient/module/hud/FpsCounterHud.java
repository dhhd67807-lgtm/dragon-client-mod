package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class FpsCounterHud extends HudModule {
    
    public FpsCounterHud() {
        super("FPS Counter", "Displays current FPS");
        this.x = 5;
        this.y = 5;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        String fps = client.getCurrentFps() + " FPS";
        
        context.drawText(client.textRenderer, fps, x, y, 0xFFFFFF, true);
        this.width = client.textRenderer.getWidth(fps);
        this.height = client.textRenderer.fontHeight;
    }
}
