package com.dragonclient.mixin;

import com.dragonclient.module.visual.ItemPhysicsModule;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public class MixinItemEntityRendererPhysics {

    @Inject(
        method = "render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("HEAD"),
        require = 0
    )
    private void dragonclient$applyItemPhysicsLegacy(
        ItemEntity entity,
        float yaw,
        float tickDelta,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        CallbackInfo ci
    ) {
        if (!ItemPhysicsModule.enabled) {
            return;
        }
        matrices.translate(0.0f, ItemPhysicsModule.GROUND_OFFSET, 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(ItemPhysicsModule.TILT_DEGREES));
    }

    @ModifyArg(
        method = "render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/RotationAxis;rotation(F)Lorg/joml/Quaternionf;"
        ),
        index = 0,
        require = 0
    )
    private float dragonclient$disableLegacySpin(float angle) {
        return ItemPhysicsModule.enabled ? 0.0f : angle;
    }

    @Redirect(
        method = "render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/MathHelper;sin(F)F"
        ),
        require = 0
    )
    private float dragonclient$disableLegacyBobbing(float value) {
        return ItemPhysicsModule.enabled ? 0.0f : MathHelper.sin(value);
    }
}
