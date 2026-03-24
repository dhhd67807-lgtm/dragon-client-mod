package com.dragonclient.mixin;

import com.dragonclient.module.visual.OutlineModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClientOutlines {

    @Inject(method = "hasOutline(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$forcePlayerOutlines(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (OutlineModule.enabled && entity instanceof PlayerEntity) {
            cir.setReturnValue(true);
        }
    }
}
