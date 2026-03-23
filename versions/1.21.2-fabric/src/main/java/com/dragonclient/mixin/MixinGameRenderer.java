package com.dragonclient.mixin;

import com.dragonclient.module.visual.MotionBlurModule;
import com.dragonclient.module.movement.FreelookModule;
import com.dragonclient.module.visual.ZoomModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    private static float dragonclient$motionBlurStrength = 0.0f;
    private static float dragonclient$lastYaw = Float.NaN;

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true, require = 0)
    private void dragonclient$applyZoomFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        if (!ZoomModule.enabled || !ZoomModule.isZooming) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        float baseFov = 70.0f;
        if (client != null && client.options != null) {
            baseFov = client.options.getFov().getValue();
        }

        // Keep zoom independent from sprint/running dynamic FOV.
        cir.setReturnValue(Math.max(1.0f, baseFov * (float) ZoomModule.getZoomFactor()));
    }

    @Inject(method = "renderWorld", at = @At("TAIL"), require = 0)
    private void dragonclient$applyMotionBlurPass(RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!MotionBlurModule.enabled || ZoomModule.isZooming) {
            dragonclient$resetMotionBlurState();
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null || client.currentScreen != null) {
            dragonclient$resetMotionBlurState();
            return;
        }

        float speed = (float) client.player.getVelocity().horizontalLength();
        float yaw = client.player.getYaw();
        float yawDelta = Float.isNaN(dragonclient$lastYaw)
            ? 0.0f
            : Math.abs(MathHelper.wrapDegrees(yaw - dragonclient$lastYaw));
        dragonclient$lastYaw = yaw;

        float intensity = MathHelper.clamp(MotionBlurModule.blurAmount, 0.0f, 2.0f);
        float speedFactor = Math.min(1.0f, speed * 1.4f);
        float turnFactor = Math.min(1.0f, yawDelta / 18.0f);

        // Prioritize turning. Walking alone should not create full-screen blur.
        float targetStrength = (turnFactor * 0.92f + speedFactor * 0.08f) * (0.20f + intensity * 0.14f);
        if (turnFactor < 0.05f) {
            targetStrength *= 0.35f;
        }
        targetStrength = MathHelper.clamp(targetStrength, 0.0f, 1.0f);

        float smoothing = 0.16f + (0.10f * Math.min(1.0f, intensity));
        dragonclient$motionBlurStrength += (targetStrength - dragonclient$motionBlurStrength) * smoothing;

        if (dragonclient$motionBlurStrength < 0.10f) {
            return;
        }

        float blurStrength = MathHelper.clamp(dragonclient$motionBlurStrength * 0.28f, 0.03f, 0.14f);
        ((GameRenderer) (Object) this).renderBlur(blurStrength);
    }

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$hideHandDuringFreelook(CallbackInfo ci) {
        if (FreelookModule.isFreelooking) {
            ci.cancel();
        }
    }

    private static void dragonclient$resetMotionBlurState() {
        dragonclient$motionBlurStrength = 0.0f;
        dragonclient$lastYaw = Float.NaN;
    }
}
