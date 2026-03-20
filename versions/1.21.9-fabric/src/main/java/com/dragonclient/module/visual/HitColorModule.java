package com.dragonclient.module.visual;

import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;

public class HitColorModule extends Module {
    public static boolean enabled = false;
    public static final int HIT_COLOR = 0xFFFF5555;

    public HitColorModule() {
        super("Hit Color", "Colors custom crosshair when targeting entities", ModuleCategory.VISUAL);
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
