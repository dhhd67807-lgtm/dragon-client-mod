package com.dragonclient.mixin;

import com.dragonclient.module.visual.ZoomModule;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    
    // For MC 1.21+ (method signature: getFov(Camera, float, boolean) -> float)
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true, require = 0)
    private void onGetFov121(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        if (ZoomModule.isZooming) {
            cir.setReturnValue(cir.getReturnValue() * (float)ZoomModule.ZOOM_FACTOR);
        }
    }
    
    // For MC 1.16-1.20 (method signature: getFov(MatrixStack, float, boolean) -> double)
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true, require = 0)
    private void onGetFovLegacy(CallbackInfoReturnable<Double> cir) {
        if (ZoomModule.isZooming) {
            cir.setReturnValue(cir.getReturnValue() * ZoomModule.ZOOM_FACTOR);
        }
    }
}
