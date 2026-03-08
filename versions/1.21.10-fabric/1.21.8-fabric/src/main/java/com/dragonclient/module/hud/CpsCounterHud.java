package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.LinkedList;
import java.util.Queue;

public class CpsCounterHud extends HudModule {
    private final Queue<Long> clicks = new LinkedList<>();
    private static final long CPS_WINDOW = 1000; // 1 second

    public CpsCounterHud() {
        super("CPS Counter", "Displays clicks per second");
        this.x = 5;
        this.y = 20;
    }

    public void registerClick() {
        clicks.add(System.currentTimeMillis());
    }

    private int getCPS() {
        long currentTime = System.currentTimeMillis();
        clicks.removeIf(time -> currentTime - time > CPS_WINDOW);
        return clicks.size();
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        String cps = getCPS() + " CPS";
        
        context.drawText(client.textRenderer, cps, x, y, 0xFFFFFF, true);
        this.width = client.textRenderer.getWidth(cps);
        this.height = client.textRenderer.fontHeight;
    }
}
