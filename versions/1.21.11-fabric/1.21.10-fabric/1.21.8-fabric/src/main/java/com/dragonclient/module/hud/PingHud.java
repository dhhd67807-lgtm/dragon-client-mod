package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

public class PingHud extends HudModule {
    
    public PingHud() {
        super("Ping", "Displays network latency");
        this.x = 5;
        this.y = 50;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.getNetworkHandler() == null) return;
        
        PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        int ping = entry != null ? entry.getLatency() : 0;
        
        String text = ping + " ms";
        
        context.drawText(client.textRenderer, text, x, y, 0xFFFFFF, true);
        this.width = client.textRenderer.getWidth(text);
        this.height = client.textRenderer.fontHeight;
    }
}
