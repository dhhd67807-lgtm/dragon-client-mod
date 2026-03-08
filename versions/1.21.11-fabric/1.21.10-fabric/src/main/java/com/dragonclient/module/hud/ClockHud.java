package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClockHud extends HudModule {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public ClockHud() {
        super("Clock", "Displays real-world time");
        this.x = 5;
        this.y = 140;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        String time = LocalTime.now().format(TIME_FORMAT);
        
        context.drawText(client.textRenderer, time, x, y, 0xFFFFFF, true);
        this.width = client.textRenderer.getWidth(time);
        this.height = client.textRenderer.fontHeight;
    }
}
