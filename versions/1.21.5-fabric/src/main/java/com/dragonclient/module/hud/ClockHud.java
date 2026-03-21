package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClockHud extends HudModule {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public ClockHud() {
        super("Clock", "Displays real-world time");
        this.x = 24;
        this.y = 166;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        String time = LocalTime.now().format(TIME_FORMAT);
        
        int textWidth = client.textRenderer.getWidth(time);
        int textHeight = client.textRenderer.fontHeight;
        applyDefaultTopRight(client, textWidth + (PANEL_PADDING_X * 2), 166);
        drawLiquidGlassTextPanel(context, textWidth, textHeight);

        context.drawText(client.textRenderer, time, x, y, 0xFFFFFFFF, false);
        this.width = textWidth;
        this.height = textHeight;
    }
}
