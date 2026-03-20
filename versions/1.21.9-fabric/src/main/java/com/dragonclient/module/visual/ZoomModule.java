package com.dragonclient.module.visual;

import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;

public class ZoomModule extends Module {
    public static boolean enabled = false;
    public static boolean isZooming = false;
    public static final double ZOOM_FACTOR = 0.25;

    public ZoomModule() {
        super("Zoom", "Hold zoom key to zoom in like OptiFine", ModuleCategory.VISUAL);
    }

    public static void setZooming(boolean zooming) {
        isZooming = zooming;
    }

    @Override
    protected void onEnable() {
        enabled = true;
        // Zoom is key-held based; module toggle does not lock zoom on.
    }

    @Override
    protected void onDisable() {
        enabled = false;
        isZooming = false;
    }
}
