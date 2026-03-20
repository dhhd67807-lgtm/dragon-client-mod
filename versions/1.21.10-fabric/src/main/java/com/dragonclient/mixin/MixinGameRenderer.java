package com.dragonclient.mixin;

import com.dragonclient.module.visual.MotionBlurModule;
import com.dragonclient.module.visual.OutlinesModule;
import com.dragonclient.module.movement.FreelookModule;
import com.dragonclient.module.visual.ZoomModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
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
    private static final Identifier DRAGONCLIENT_WORLD_OUTLINE = Identifier.of("dragonclient", "world_outline");

    @Inject(method = "renderWorld", at = @At("HEAD"), require = 0)
    private void dragonclient$forceWorldOutlinePostEffect(RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            return;
        }

        MixinGameRendererPostEffectAccessor accessor = (MixinGameRendererPostEffectAccessor) (Object) this;
        if (!OutlinesModule.enabled) {
            accessor.dragonclient$setPostProcessorEnabled(false);
            return;
        }

        accessor.dragonclient$setPostProcessorId(DRAGONCLIENT_WORLD_OUTLINE);
        accessor.dragonclient$setPostProcessorEnabled(true);
    }

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
        cir.setReturnValue(Math.max(1.0f, baseFov * (float) ZoomModule.ZOOM_FACTOR));
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
        float speedFactor = Math.min(1.0f, speed * 3.0f);
        float turnFactor = Math.min(1.0f, yawDelta / 12.0f);
        float targetStrength = (speedFactor * 0.8f + turnFactor * 0.7f) * (0.35f + intensity * 0.30f);
        targetStrength = MathHelper.clamp(targetStrength, 0.0f, 1.0f);

        float smoothing = 0.22f + (0.18f * Math.min(1.0f, intensity));
        dragonclient$motionBlurStrength += (targetStrength - dragonclient$motionBlurStrength) * smoothing;

        if (dragonclient$motionBlurStrength < 0.12f) {
            return;
        }

        ((GameRenderer) (Object) this).renderBlur();
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
