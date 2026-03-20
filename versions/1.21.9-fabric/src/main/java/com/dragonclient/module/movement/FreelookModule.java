package com.dragonclient.module.movement;

import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;

public class FreelookModule extends Module {
    public static boolean enabled = false;
    public static boolean isFreelooking = false;
    public static float cameraYaw = 0;
    public static float cameraPitch = 0;
    private static float lockedYaw = 0.0f;
    private static float lockedPitch = 0.0f;
    private static Perspective previousPerspective = null;

    public FreelookModule() {
        super("Freelook", "Look around without moving", ModuleCategory.VISUAL);
    }

    @Override
    protected void onEnable() {
        enabled = true;
    }

    @Override
    protected void onDisable() {
        enabled = false;
        stopFreelook(MinecraftClient.getInstance());
    }

    public static void startFreelook(MinecraftClient client) {
        if (!enabled || isFreelooking || client == null || client.player == null) {
            return;
        }

        isFreelooking = true;
        cameraYaw = client.player.getYaw();
        cameraPitch = MathHelper.clamp(client.player.getPitch(), -90.0f, 90.0f);
        lockedYaw = client.player.getYaw();
        lockedPitch = MathHelper.clamp(client.player.getPitch(), -90.0f, 90.0f);

        if (client.options != null) {
            previousPerspective = client.options.getPerspective();
            client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
        }
    }

    public static void stopFreelook(MinecraftClient client) {
        if (!isFreelooking) {
            return;
        }

        isFreelooking = false;

        if (client != null && client.options != null && previousPerspective != null) {
            client.options.setPerspective(previousPerspective);
        }
        previousPerspective = null;
    }

    public static void toggleFreelook(MinecraftClient client) {
        if (isFreelooking) {
            stopFreelook(client);
            return;
        }

        startFreelook(client);
    }

    public static void applyLookDelta(double deltaX, double deltaY) {
        float pitchDelta = (float) deltaY * 0.15f;
        float yawDelta = (float) deltaX * 0.15f;
        cameraPitch = MathHelper.clamp(cameraPitch + pitchDelta, -90.0f, 90.0f);
        cameraYaw += yawDelta;
    }

    public static void enforceLockedRotation(MinecraftClient client) {
        if (!isFreelooking || client == null || client.player == null) {
            return;
        }

        client.player.setYaw(lockedYaw);
        client.player.setPitch(lockedPitch);
        client.player.setHeadYaw(lockedYaw);
    }
}
