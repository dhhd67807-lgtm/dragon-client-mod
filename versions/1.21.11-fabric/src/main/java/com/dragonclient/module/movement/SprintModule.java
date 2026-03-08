package com.dragonclient.module.movement;

import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;

public class SprintModule extends Module {
    
    public SprintModule() {
        super("Auto Sprint", "Automatically sprint", ModuleCategory.MOVEMENT);
    }

    public void tick() {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.player.forwardSpeed > 0) {
            client.player.setSprinting(true);
        }
    }
}
