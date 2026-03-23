package com.dragonclient.module.visual;

import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;

public class FullbrightModule extends Module {
    public static boolean enabled = false;
    private static final double FULLBRIGHT_GAMMA = 16.0;
    private static Double previousGamma;
    private static boolean gammaForced;

    public FullbrightModule() {
        super("Fullbright", "See in the dark", ModuleCategory.VISUAL);
    }

    @Override
    protected void onEnable() {
        enabled = true;
        applyIfPossible();
    }

    @Override
    protected void onDisable() {
        enabled = false;
        restoreIfPossible();
    }

    public static void tick() {
        if (!enabled) {
            return;
        }
        applyIfPossible();
    }

    private static void applyIfPossible() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) {
            return;
        }
        if (!gammaForced) {
            previousGamma = client.options.getGamma().getValue();
            gammaForced = true;
        }
        if (client.options.getGamma().getValue() < FULLBRIGHT_GAMMA) {
            client.options.getGamma().setValue(FULLBRIGHT_GAMMA);
        }
    }

    private static void restoreIfPossible() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!gammaForced) {
            return;
        }
        gammaForced = false;
        if (client == null || client.options == null) {
            return;
        }
        if (previousGamma != null) {
            client.options.getGamma().setValue(previousGamma);
        }
        previousGamma = null;
    }
}
