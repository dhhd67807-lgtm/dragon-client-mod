package com.dragonclient.module.movement;

import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;

public class FreelookModule extends Module {
    public static boolean isFreelooking = false;
    public static float cameraYaw = 0;
    public static float cameraPitch = 0;

    public FreelookModule() {
        super("Freelook", "Look around without moving", ModuleCategory.MOVEMENT);
    }

    @Override
    protected void onEnable() {
        isFreelooking = true;
    }

    @Override
    protected void onDisable() {
        isFreelooking = false;
    }
}
