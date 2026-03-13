package com.dragonclient.module.player;

import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;

public class AutoRespawnModule extends Module {
    public static boolean enabled = false;

    public AutoRespawnModule() {
        super("Auto Respawn", "Automatically respawn on death", ModuleCategory.PLAYER);
    }

    @Override
    protected void onEnable() {
        enabled = true;
    }

    @Override
    protected void onDisable() {
        enabled = false;
    }
}
