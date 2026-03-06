package com.dragonclient.mixin;

import com.dragonclient.module.visual.ZoomModule;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void onGetFov(CallbackInfoReturnable<Double> cir) {
        if (ZoomModule.isZooming) {
            cir.setReturnValue(cir.getReturnValue() * ZoomModule.ZOOM_FACTOR);
        }
    }
}
