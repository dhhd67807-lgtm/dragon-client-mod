package com.dragonclient.mixin;

import com.dragonclient.module.movement.FreelookModule;
import com.dragonclient.module.visual.MotionBlurModule;
import com.dragonclient.module.visual.ZoomModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    private static float dragonclient$blurEnergy = 0.0f;
    private static float dragonclient$motionStrength = 0.0f;
    private static float dragonclient$lastYaw = Float.NaN;
    private static float dragonclient$lastPitch = Float.NaN;
    private static int dragonclient$idleTicks = 0;

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true, require = 0)
    private void dragonclient$applyZoomFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (ZoomModule.enabled && ZoomModule.isZooming) {
            float baseFov = 70.0f;
            if (client != null && client.options != null) {
                baseFov = client.options.getFov().getValue();
            }

            // Keep zoom independent from sprint/running dynamic FOV.
            cir.setReturnValue(Math.max(1.0f, baseFov * (float) ZoomModule.ZOOM_FACTOR));
            dragonclient$resetMotionBlurState();
            return;
        }
    }

    @Inject(method = "renderWorld", at = @At("TAIL"), require = 0)
    private void dragonclient$renderMotionBlur(RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!MotionBlurModule.enabled || ZoomModule.isZooming || client == null || client.player == null || client.world == null || client.currentScreen != null) {
            dragonclient$resetMotionBlurState();
            return;
        }

        float yaw = client.player.getYaw();
        float pitch = client.player.getPitch();
        float yawDelta = Float.isNaN(dragonclient$lastYaw) ? 0.0f : Math.abs(MathHelper.wrapDegrees(yaw - dragonclient$lastYaw));
        float pitchDelta = Float.isNaN(dragonclient$lastPitch) ? 0.0f : Math.abs(pitch - dragonclient$lastPitch);
        dragonclient$lastYaw = yaw;
        dragonclient$lastPitch = pitch;

        float speed = (float) client.player.getVelocity().horizontalLength();
        float intensity = MathHelper.clamp(MotionBlurModule.blurAmount, 0.0f, 2.0f);

        // Favor camera turning, keep movement contribution low to avoid constant blur.
        float turnFactor = Math.min(1.0f, (yawDelta + pitchDelta * 0.9f) / 24.0f);
        float moveFactor = Math.min(1.0f, speed * 1.8f);
        float targetMotion = turnFactor * 0.88f + moveFactor * 0.12f;

        if (targetMotion < 0.045f) {
            dragonclient$idleTicks++;
        } else {
            dragonclient$idleTicks = 0;
        }

        float smoothing = 0.16f + intensity * 0.06f;
        dragonclient$motionStrength += (targetMotion - dragonclient$motionStrength) * smoothing;
        dragonclient$motionStrength = MathHelper.clamp(dragonclient$motionStrength, 0.0f, 1.0f);

        if (dragonclient$idleTicks > 6) {
            dragonclient$motionStrength *= 0.72f;
        }

        if (dragonclient$motionStrength < 0.10f) {
            dragonclient$blurEnergy *= 0.75f;
            return;
        }

        // Pulse-based blur trigger: smooth + stable, avoids full-screen smear.
        float energyGain = dragonclient$motionStrength * (0.26f + intensity * 0.18f);
        if (turnFactor > 0.62f) {
            energyGain += 0.10f;
        }
        dragonclient$blurEnergy = MathHelper.clamp(dragonclient$blurEnergy + energyGain, 0.0f, 2.0f);

        if (dragonclient$blurEnergy >= 1.0f) {
            ((GameRenderer) (Object) this).renderBlur();
            dragonclient$blurEnergy -= 1.0f;
        }
    }

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$hideHandDuringFreelook(float tickProgress, boolean isPanorama, Matrix4f matrix, CallbackInfo ci) {
        if (FreelookModule.isFreelooking) {
            ci.cancel();
        }
    }

    private static void dragonclient$resetMotionBlurState() {
        dragonclient$blurEnergy = 0.0f;
        dragonclient$motionStrength = 0.0f;
        dragonclient$lastYaw = Float.NaN;
        dragonclient$lastPitch = Float.NaN;
        dragonclient$idleTicks = 0;
    }
}
