package com.dragonclient.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class MixinEntityRendererNameTag {

    private boolean dragonclient$shouldForceNameTag(Entity entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        return entity instanceof PlayerEntity && client != null && entity == client.player;
    }

    @Inject(method = "hasLabel(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$hasLabel(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (dragonclient$shouldForceNameTag(entity)) {
            cir.setReturnValue(true);
        }
    }
}
