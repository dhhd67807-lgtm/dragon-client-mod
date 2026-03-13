package com.dragonclient.mixin;

import com.dragonclient.module.visual.NametagModule;
import com.dragonclient.util.NametagDebugLogger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class MixinEntityRendererNameTag {

    private boolean dragonclient$shouldForceNameTag(LivingEntity entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean shouldForce = NametagModule.enabled && entity instanceof PlayerEntity && client != null && entity == client.player;
        NametagDebugLogger.logEvery(
            "livingrenderer-should-force",
            2000,
            "LivingEntityRenderer.shouldForceNameTag: " + shouldForce +
                ", entityClass=" + (entity == null ? "null" : entity.getClass().getName())
        );
        return shouldForce;
    }

    @Inject(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$hasLabel(LivingEntity entity, double distance, CallbackInfoReturnable<Boolean> cir) {
        if (dragonclient$shouldForceNameTag(entity)) {
            cir.setReturnValue(true);
            NametagDebugLogger.logEvery(
                "livingrenderer-haslabel-force",
                1000,
                "LivingEntityRenderer.hasLabel forced true for local player at distance=" + distance
            );
        }
    }
}
