package com.dragonclient.module.visual;

import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;

public class TimeChangerModule extends Module {
    public static boolean enabled = false;
    public static long customTime = 18000; // Midnight for a clearly visible effect

    public TimeChangerModule() {
        super("Time Changer", "Change time client-side", ModuleCategory.VISUAL);
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
