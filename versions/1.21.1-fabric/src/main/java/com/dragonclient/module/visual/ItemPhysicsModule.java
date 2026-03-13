package com.dragonclient.module.visual;

import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;

public class ItemPhysicsModule extends Module {
    public static boolean enabled = false;
    public static float TILT_DEGREES = 90.0f;
    public static float GROUND_OFFSET = 0.035f;

    public ItemPhysicsModule() {
        super("Item Physics", "Applies a realistic flat item tilt", ModuleCategory.VISUAL);
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
