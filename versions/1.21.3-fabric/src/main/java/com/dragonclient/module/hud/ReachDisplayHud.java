package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class ReachDisplayHud extends HudModule {
    
    public ReachDisplayHud() {
        super("Reach Display", "Displays reach distance");
        this.x = 24;
        this.y = 214;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        
        double reach = 0.0;
        if (client.player != null && client.crosshairTarget != null &&
            client.crosshairTarget.getType() != net.minecraft.util.hit.HitResult.Type.MISS) {
            reach = client.player.getEyePos().distanceTo(client.crosshairTarget.getPos());
        }

        String text = String.format("Reach: %.2f", reach);
        
        int textWidth = client.textRenderer.getWidth(text);
        int textHeight = client.textRenderer.fontHeight;
        applyDefaultTopRight(client, textWidth + (PANEL_PADDING_X * 2), 214);
        drawLiquidGlassTextPanel(context, textWidth, textHeight);

        context.drawText(client.textRenderer, text, x, y, 0xFFFFFFFF, false);
        this.width = textWidth;
        this.height = textHeight;
    }
}
