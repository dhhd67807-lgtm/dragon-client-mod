package com.dragonclient.module.visual;

import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;

public class MotionBlurModule extends Module {
    public static boolean enabled = false;
    public static float blurAmount = 2.0f;

    public MotionBlurModule() {
        super("Motion Blur", "Adds motion blur effect", ModuleCategory.VISUAL);
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
