package com.dragonclient.mixin;

import com.dragonclient.module.visual.FullbrightModule;
import com.dragonclient.module.visual.MotionBlurModule;
import com.dragonclient.module.movement.FreelookModule;
import com.dragonclient.module.visual.ZoomModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    private static float dragonclient$motionBlurStrength = 0.0f;
    private static float dragonclient$lastYaw = Float.NaN;
    private static Method dragonclient$renderBlurWithStrengthMethod;
    private static boolean dragonclient$checkedBlurWithStrengthMethod = false;

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
        float speedFactor = Math.min(1.0f, speed * 2.4f);
        float turnFactor = Math.min(1.0f, yawDelta / 18.0f);
        float targetStrength = (speedFactor * 0.72f + turnFactor * 0.48f) * (0.22f + intensity * 0.20f);
        targetStrength = MathHelper.clamp(targetStrength, 0.0f, 1.0f);

        float smoothing = 0.18f + (0.14f * Math.min(1.0f, intensity));
        dragonclient$motionBlurStrength += (targetStrength - dragonclient$motionBlurStrength) * smoothing;

        if (dragonclient$motionBlurStrength < 0.11f) {
            return;
        }

        float blurStrength = MathHelper.clamp(dragonclient$motionBlurStrength * 0.24f, 0.025f, 0.10f);
        dragonclient$renderBlur((GameRenderer) (Object) this, blurStrength);
    }

    @Inject(method = "getNightVisionStrength", at = @At("HEAD"), cancellable = true, require = 0)
    private static void dragonclient$forceFullbright(LivingEntity entity, float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (!FullbrightModule.enabled) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || entity != client.player) {
            return;
        }
        cir.setReturnValue(1.0f);
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

    private static void dragonclient$renderBlur(GameRenderer renderer, float blurStrength) {
        if (!dragonclient$checkedBlurWithStrengthMethod) {
            dragonclient$checkedBlurWithStrengthMethod = true;
            try {
                dragonclient$renderBlurWithStrengthMethod = GameRenderer.class.getDeclaredMethod("renderBlur", float.class);
                dragonclient$renderBlurWithStrengthMethod.setAccessible(true);
            } catch (ReflectiveOperationException ignored) {
                dragonclient$renderBlurWithStrengthMethod = null;
            }
        }

        if (dragonclient$renderBlurWithStrengthMethod != null) {
            try {
                dragonclient$renderBlurWithStrengthMethod.invoke(renderer, blurStrength);
                return;
            } catch (ReflectiveOperationException ignored) {
                dragonclient$renderBlurWithStrengthMethod = null;
            }
        }

        renderer.renderBlur();
    }
}
