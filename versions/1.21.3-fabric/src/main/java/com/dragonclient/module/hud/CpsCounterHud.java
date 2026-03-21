package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.LinkedList;
import java.util.Queue;

public class CpsCounterHud extends HudModule {
    private final Queue<Long> clicks = new LinkedList<>();
    private boolean wasAttackPressed = false;
    private static final long CPS_WINDOW = 1000; // 1 second

    public CpsCounterHud() {
        super("CPS Counter", "Displays clicks per second");
        this.x = 5;  // Top left, below FPS
        this.y = 25;
    }

    public void registerClick() {
        clicks.add(System.currentTimeMillis());
    }

    private int getCPS() {
        long currentTime = System.currentTimeMillis();
        clicks.removeIf(time -> currentTime - time > CPS_WINDOW);
        return clicks.size();
    }

    private void trackClicks(MinecraftClient client) {
        boolean isAttackPressed = client.options.attackKey.isPressed();
        if (isAttackPressed && !wasAttackPressed) {
            registerClick();
        }
        wasAttackPressed = isAttackPressed;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        trackClicks(client);
        String cps = getCPS() + " CPS";
        
        // Draw background - #1D1C1C at 50% opacity
        int textWidth = client.textRenderer.getWidth(cps);
        int textHeight = client.textRenderer.fontHeight;
        context.fill(x - 8, y - 8, x + textWidth + 8, y + textHeight + 8, 0x551D1C1C);
        
        // Draw outer border - #161616 at 100% opacity
        context.fill(x - 8, y - 8, x + textWidth + 8, y - 7, 0xFF161616); // Top
        context.fill(x - 8, y + textHeight + 7, x + textWidth + 8, y + textHeight + 8, 0xFF161616); // Bottom
        context.fill(x - 8, y - 8, x - 7, y + textHeight + 8, 0xFF161616); // Left
        context.fill(x + textWidth + 7, y - 8, x + textWidth + 8, y + textHeight + 8, 0xFF161616); // Right
        
        // Draw inset shadow - Dark gray for depth
        context.fill(x - 7, y - 7, x + textWidth + 7, y - 6, 0x50000000); // Top inner shadow
        context.fill(x - 7, y - 7, x - 6, y + textHeight + 7, 0x50000000); // Left inner shadow
        
        // Draw text - White at 100% opacity without shadow
        context.drawText(client.textRenderer, cps, x, y, 0xFFFFFFFF, false);
        this.width = textWidth;
        this.height = textHeight;
    }
}
