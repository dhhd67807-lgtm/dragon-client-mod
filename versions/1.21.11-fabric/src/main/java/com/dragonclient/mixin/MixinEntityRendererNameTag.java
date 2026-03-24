package com.dragonclient.mixin;

import com.dragonclient.module.visual.NametagModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class MixinEntityRendererNameTag {

    private boolean dragonclient$shouldAlwaysShowNameTag(Entity entity) {
        return NametagModule.enabled && entity instanceof PlayerEntity;
    }

    private boolean dragonclient$shouldAlwaysShowNameTag(EntityRenderState state) {
        return NametagModule.enabled && state instanceof PlayerEntityRenderState;
    }

    @Inject(method = "hasLabel(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$hasLabelOld(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (dragonclient$shouldAlwaysShowNameTag(entity)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hasLabel(Lnet/minecraft/entity/Entity;D)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$hasLabelNew(Entity entity, double distance, CallbackInfoReturnable<Boolean> cir) {
        if (dragonclient$shouldAlwaysShowNameTag(entity)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hasLabel(Lnet/minecraft/client/render/entity/state/EntityRenderState;)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$hasLabelState(EntityRenderState state, CallbackInfoReturnable<Boolean> cir) {
        if (dragonclient$shouldAlwaysShowNameTag(state)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hasLabel(Lnet/minecraft/client/render/entity/state/EntityRenderState;D)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$hasLabelStateDistance(EntityRenderState state, double distance, CallbackInfoReturnable<Boolean> cir) {
        if (dragonclient$shouldAlwaysShowNameTag(state)) {
            cir.setReturnValue(true);
        }
    }
}
