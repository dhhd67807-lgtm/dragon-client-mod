package com.dragonclient.mixin;

import com.dragonclient.module.visual.TimeChangerModule;
import com.dragonclient.module.visual.WeatherChangerModule;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    
    @Inject(method = "hasRain", at = @At("HEAD"), cancellable = true)
    private void onHasRain(CallbackInfoReturnable<Boolean> cir) {
        if (WeatherChangerModule.clearWeather) {
            cir.setReturnValue(false);
        }
    }
    
    @Inject(method = "hasSnow", at = @At("HEAD"), cancellable = true)
    private void onHasSnow(CallbackInfoReturnable<Boolean> cir) {
        if (WeatherChangerModule.clearWeather) {
            cir.setReturnValue(false);
        }
    }
}
