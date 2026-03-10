package com.dragonclient.mixin;

import com.dragonclient.DragonClientMod;
import com.dragonclient.module.Module;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRendererNameTag {

    private boolean dragonclient$shouldForceNameTag(Entity entity) {
        return entity instanceof PlayerEntity;
    }

    @Inject(method = "hasLabel(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$hasLabelOld(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (dragonclient$shouldForceNameTag(entity)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hasLabel(Lnet/minecraft/entity/Entity;D)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$hasLabelNew(Entity entity, double distance, CallbackInfoReturnable<Boolean> cir) {
        if (dragonclient$shouldForceNameTag(entity)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/entity/state/EntityRenderState;F)V",
            at = @At("TAIL"), require = 0)
    private void dragonclient$forcePlayerLabelState(Entity entity, EntityRenderState state, float tickDelta, CallbackInfo ci) {
        if (!dragonclient$shouldForceNameTag(entity)) {
            return;
        }

        // Keep one clean nametag line and force label anchor position above the head.
        state.displayName = null;
        state.nameLabelPos = new Vec3d(0.0, entity.getHeight() + 0.5, 0.0);

        if (state instanceof PlayerEntityRenderState playerState) {
            playerState.playerName = entity.getDisplayName();
        }
    }
}
