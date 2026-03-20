package com.dragonclient.module.visual;

import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;

public class ClearWaterModule extends Module {
    public static boolean enabled = false;

    public ClearWaterModule() {
        super("Clear Water", "Makes water easier to see through", ModuleCategory.VISUAL);
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
