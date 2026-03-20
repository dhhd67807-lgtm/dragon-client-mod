package com.dragonclient.mixin;

import com.dragonclient.module.movement.FreelookModule;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class MixinCameraFreelook {

    @Shadow protected abstract void setRotation(float yaw, float pitch);
    @Shadow protected abstract void setPos(double x, double y, double z);
    @Shadow protected abstract void moveBy(float x, float y, float z);
    @Shadow private float clipToSpace(float desiredCameraDistance) {
        throw new AssertionError();
    }
    @Shadow private float cameraY;
    @Shadow private float lastCameraY;

    @Inject(method = "update", at = @At("TAIL"))
    private void dragonclient$applyFreelookRotation(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickProgress, CallbackInfo ci) {
        if (!FreelookModule.isFreelooking) {
            return;
        }

        if (!(focusedEntity instanceof ClientPlayerEntity)) {
            return;
        }

        // Rebuild camera orbit using freelook angles so rotation + position stay in sync.
        var lerpedPos = focusedEntity.getLerpedPos(tickProgress);
        double y = lerpedPos.y + (double) MathHelper.lerp(tickProgress, this.lastCameraY, this.cameraY);

        this.setPos(lerpedPos.x, y, lerpedPos.z);
        this.setRotation(FreelookModule.cameraYaw, FreelookModule.cameraPitch);
        float distance = this.clipToSpace(2.6f);
        this.moveBy(-distance, 0.0f, 0.0f);
    }

    @ModifyVariable(method = "update", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private boolean dragonclient$forceThirdPersonDuringFreelook(boolean thirdPerson) {
        return FreelookModule.isFreelooking || thirdPerson;
    }
}
