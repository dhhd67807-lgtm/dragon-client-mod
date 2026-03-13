package com.dragonclient.module.visual;

import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;

public class FullbrightModule extends Module {
    private Double previousGamma;

    public FullbrightModule() {
        super("Fullbright", "See in the dark", ModuleCategory.VISUAL);
    }

    @Override
    protected void onEnable() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) {
            return;
        }

        previousGamma = client.options.getGamma().getValue();
        client.options.getGamma().setValue(16.0);
    }

    @Override
    protected void onDisable() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null || previousGamma == null) {
            return;
        }

        client.options.getGamma().setValue(previousGamma);
    }
}
