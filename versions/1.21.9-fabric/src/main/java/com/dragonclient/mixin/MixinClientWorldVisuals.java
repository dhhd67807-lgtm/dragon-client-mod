package com.dragonclient.mixin;

import com.dragonclient.module.visual.TimeChangerModule;
import com.dragonclient.module.visual.WeatherChangerModule;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class MixinClientWorldVisuals {

    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$overrideTime(CallbackInfoReturnable<Long> cir) {
        if ((Object) this instanceof ClientWorld && TimeChangerModule.enabled) {
            cir.setReturnValue(TimeChangerModule.customTime);
        }
    }

    @Inject(method = "getRainGradient", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$overrideRain(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof ClientWorld && WeatherChangerModule.clearWeather) {
            cir.setReturnValue(0.0F);
        }
    }

    @Inject(method = "getThunderGradient", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$overrideThunder(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof ClientWorld && WeatherChangerModule.clearWeather) {
            cir.setReturnValue(0.0F);
        }
    }

    @Inject(method = "isRaining", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$overrideIsRaining(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ClientWorld && WeatherChangerModule.clearWeather) {
            cir.setReturnValue(false);
        }
    }
}
