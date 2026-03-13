package com.dragonclient.mixin;

import com.dragonclient.module.movement.FreelookModule;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinEntityFreelook {

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void dragonclient$freelookChangeLookDirection(double deltaX, double deltaY, CallbackInfo ci) {
        if (!FreelookModule.isFreelooking) {
            return;
        }

        if (!(((Object) this) instanceof ClientPlayerEntity)) {
            return;
        }

        FreelookModule.applyLookDelta(deltaX, deltaY);
        ci.cancel();
    }
}
