package com.dragonclient.module.visual;

import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;

public class ZoomModule extends Module {
    public static boolean enabled = false;
    public static boolean isZooming = false;
    public static final double DEFAULT_ZOOM_FACTOR = 0.25;
    public static final double ZOOM_FACTOR = DEFAULT_ZOOM_FACTOR;
    public static final double MIN_ZOOM_FACTOR = 0.03;
    public static final double MAX_ZOOM_FACTOR = 1.00;
    private static final double SCROLL_STEP_MULTIPLIER = 0.88;
    private static double zoomFactor = DEFAULT_ZOOM_FACTOR;

    public ZoomModule() {
        super("Zoom", "Hold zoom key to zoom in like OptiFine", ModuleCategory.VISUAL);
    }

    public static void setZooming(boolean zooming) {
        isZooming = zooming;
    }

    public static double getZoomFactor() {
        return zoomFactor;
    }

    public static void adjustZoomFromScroll(double verticalScroll) {
        if (verticalScroll == 0.0) {
            return;
        }

        // Scroll up -> zoom more in (smaller factor). Scroll down -> zoom out.
        if (verticalScroll > 0.0) {
            zoomFactor *= SCROLL_STEP_MULTIPLIER;
        } else {
            zoomFactor /= SCROLL_STEP_MULTIPLIER;
        }

        if (zoomFactor < MIN_ZOOM_FACTOR) {
            zoomFactor = MIN_ZOOM_FACTOR;
        } else if (zoomFactor > MAX_ZOOM_FACTOR) {
            zoomFactor = MAX_ZOOM_FACTOR;
        }
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
        zoomFactor = DEFAULT_ZOOM_FACTOR;
    }
}
