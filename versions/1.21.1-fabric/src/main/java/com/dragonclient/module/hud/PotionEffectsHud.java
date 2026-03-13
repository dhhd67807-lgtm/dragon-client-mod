package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class PotionEffectsHud extends HudModule {

    public PotionEffectsHud() {
        super("Potion Effects", "Displays active potion effects");
        this.x = 420;
        this.y = 30;
        this.width = 110;
        this.height = 18;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.textRenderer == null) {
            return;
        }

        Collection<StatusEffectInstance> active =
            client.player != null ? client.player.getStatusEffects() : List.of();

        List<StatusEffectInstance> effects = new ArrayList<>(active);
        effects.sort(Comparator.comparingInt(StatusEffectInstance::getDuration).reversed());

        int lineHeight = client.textRenderer.fontHeight + 4;
        int yOffset = 0;
        int maxWidth = 90;

        if (effects.isEmpty()) {
            String placeholder = "No Effects";
            int textWidth = client.textRenderer.getWidth(placeholder);
            int boxWidth = textWidth + 12;
            int boxHeight = lineHeight + 4;

            drawEntryBox(context, x, y, boxWidth, boxHeight);
            context.drawText(client.textRenderer, placeholder, x + 6, y + 4, 0xFFAAAAAA, false);

            this.width = boxWidth;
            this.height = boxHeight;
            return;
        }

        for (StatusEffectInstance effect : effects) {
            String effectName = effect.getEffectType().value().getName().getString();
            String amplifier = formatAmplifier(effect.getAmplifier());
            int durationSeconds = Math.max(0, effect.getDuration() / 20);

            String text = effectName + amplifier + " " + formatTime(durationSeconds);
            int textWidth = client.textRenderer.getWidth(text);
            int boxWidth = textWidth + 12;
            int boxHeight = lineHeight + 4;

            int drawY = y + yOffset;
            drawEntryBox(context, x, drawY, boxWidth, boxHeight);
            context.drawText(client.textRenderer, text, x + 6, drawY + 4, 0xFFFFFFFF, false);

            maxWidth = Math.max(maxWidth, boxWidth);
            yOffset += boxHeight + 2;
        }

        this.width = maxWidth;
        this.height = Math.max(18, yOffset - 2);
    }

    private static void drawEntryBox(DrawContext context, int x, int y, int width, int height) {
        // Background: #1D1C1C at 50%
        context.fill(x, y, x + width, y + height, 0x801D1C1C);

        // Border: #161616
        context.fill(x, y, x + width, y + 1, 0xFF161616);
        context.fill(x, y + height - 1, x + width, y + height, 0xFF161616);
        context.fill(x, y, x + 1, y + height, 0xFF161616);
        context.fill(x + width - 1, y, x + width, y + height, 0xFF161616);
    }

    private static String formatAmplifier(int amplifier) {
        int level = amplifier + 1;
        if (level <= 1) {
            return "";
        }

        return switch (level) {
            case 2 -> " II";
            case 3 -> " III";
            case 4 -> " IV";
            case 5 -> " V";
            case 6 -> " VI";
            case 7 -> " VII";
            case 8 -> " VIII";
            case 9 -> " IX";
            case 10 -> " X";
            default -> " " + level;
        };
    }

    private static String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }
}
