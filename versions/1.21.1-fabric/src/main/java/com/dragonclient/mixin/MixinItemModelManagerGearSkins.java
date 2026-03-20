package com.dragonclient.mixin;

import com.dragonclient.cosmetics.GearSkinManager;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public class MixinItemModelManagerGearSkins {

    @ModifyVariable(
        method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private ItemStack dragonclient$applyGearSkinRenderItemLiving(
        ItemStack stack,
        LivingEntity entity,
        ItemStack originalStack,
        ModelTransformationMode mode,
        boolean leftHanded,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        World world,
        int light,
        int overlay,
        int seed
    ) {
        return GearSkinManager.getRenderStackForEntity(stack, entity);
    }

    @ModifyVariable(
        method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;IILnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;I)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private ItemStack dragonclient$applyGearSkinRenderItem(
        ItemStack stack,
        ItemStack originalStack,
        ModelTransformationMode mode,
        int light,
        int overlay,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        World world,
        int seed
    ) {
        return GearSkinManager.getRenderStack(stack);
    }

    @ModifyVariable(method = "getModel", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private ItemStack dragonclient$applyGearSkinGetModel(
        ItemStack stack,
        ItemStack originalStack,
        World world,
        LivingEntity entity,
        int seed
    ) {
        return GearSkinManager.getRenderStackForEntity(stack, entity);
    }
}
