package com.dragonclient.mixin;

import com.dragonclient.cosmetics.GearSkinManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HeldItemRenderer.class)
public class MixinHeldItemRendererGearSkins {

    @Redirect(
        method = "renderFirstPersonItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"
        )
    )
    private void dragonclient$scaleReducedHandSkin(
        HeldItemRenderer instance,
        LivingEntity entity,
        ItemStack stack,
        ModelTransformationMode displayContext,
        boolean leftHanded,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light
    ) {
        float scale = GearSkinManager.getInHandScale(stack);
        if (scale >= 0.999f) {
            instance.renderItem(entity, stack, displayContext, leftHanded, matrices, vertexConsumers, light);
            return;
        }

        matrices.push();
        matrices.scale(scale, scale, scale);
        instance.renderItem(entity, stack, displayContext, leftHanded, matrices, vertexConsumers, light);
        matrices.pop();
    }
}
