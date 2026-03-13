package com.dragonclient.module.movement;

import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;

public class NoFallModule extends Module {
    public static boolean enabled = false;

    public NoFallModule() {
        super("No Fall", "Client-side no-fall helper", ModuleCategory.MOVEMENT);
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
