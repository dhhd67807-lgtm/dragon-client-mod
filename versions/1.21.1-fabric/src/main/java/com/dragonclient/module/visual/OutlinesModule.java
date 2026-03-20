package com.dragonclient.module.visual;

import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;

public class OutlinesModule extends Module {
    public static boolean enabled = true;

    public OutlinesModule() {
        super("Outlines", "Draws smooth world outlines without shaders", ModuleCategory.VISUAL);
        setEnabled(true);
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
