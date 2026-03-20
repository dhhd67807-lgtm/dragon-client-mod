package com.dragonclient.module.visual;

import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;

public class CustomCrosshairModule extends Module {
    public static boolean enabled = false;
    public static int CROSSHAIR_SIZE = 6;
    public static int CROSSHAIR_GAP = 2;
    public static int CROSSHAIR_THICKNESS = 1;

    public CustomCrosshairModule() {
        super("Custom Crosshair", "Replaces vanilla crosshair with custom one", ModuleCategory.VISUAL);
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
