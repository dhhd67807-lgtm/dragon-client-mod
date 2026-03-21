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
        this.x = 24;
        this.y = 118;
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
        
        int textWidth = client.textRenderer.getWidth(cps);
        int textHeight = client.textRenderer.fontHeight;
        applyDefaultTopRight(client, textWidth + (PANEL_PADDING_X * 2), 118);
        drawLiquidGlassTextPanel(context, textWidth, textHeight);

        context.drawText(client.textRenderer, cps, x, y, 0xFFFFFFFF, false);
        this.width = textWidth;
        this.height = textHeight;
    }
}
