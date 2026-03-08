package com.dragonclient.module.visual;

import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;

public class ZoomModule extends Module {
    public static boolean isZooming = false;
    public static final double ZOOM_FACTOR = 0.25;

    public ZoomModule() {
        super("Zoom", "Zoom in like OptiFine", ModuleCategory.VISUAL);
    }

    @Override
    protected void onEnable() {
        isZooming = true;
    }

    @Override
    protected void onDisable() {
        isZooming = false;
    }
}
