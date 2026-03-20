package com.dragonclient.mixin;

import com.dragonclient.cosmetics.GearSkinManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(net.minecraft.client.item.ItemModelManager.class)
public class MixinItemModelManagerGearSkins {

    @ModifyVariable(
        method = "updateForLivingEntity",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0,
        require = 0
    )
    private ItemStack dragonclient$applyGearSkinUpdateForLivingEntity(
        ItemStack stack,
        ItemRenderState renderState,
        ItemStack sourceStack,
        ModelTransformationMode displayContext,
        boolean leftHanded,
        LivingEntity entity
    ) {
        return GearSkinManager.getRenderStackForEntity(sourceStack, entity);
    }

    @ModifyVariable(
        method = "updateForNonLivingEntity",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0,
        require = 0
    )
    private ItemStack dragonclient$applyGearSkinUpdateForNonLivingEntity(
        ItemStack stack,
        ItemRenderState renderState,
        ItemStack sourceStack,
        ModelTransformationMode displayContext,
        Entity entity
    ) {
        return GearSkinManager.getRenderStackForEntity(sourceStack, entity);
    }

    @ModifyVariable(
        method = "update(Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0,
        require = 0
    )
    private ItemStack dragonclient$applyGearSkinUpdate(
        ItemStack stack,
        ItemRenderState renderState,
        ItemStack sourceStack,
        ModelTransformationMode displayContext,
        boolean leftHanded,
        World world,
        LivingEntity holder,
        int seed
    ) {
        return GearSkinManager.getRenderStackForEntity(sourceStack, holder);
    }

    @ModifyVariable(
        method = "update(Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0,
        require = 0
    )
    private ItemStack dragonclient$applyGearSkinUpdateNoSwap(
        ItemStack stack,
        ItemRenderState renderState,
        ItemStack sourceStack,
        ModelTransformationMode displayContext,
        World world,
        LivingEntity holder,
        int seed
    ) {
        return GearSkinManager.getRenderStackForEntity(sourceStack, holder);
    }
}
