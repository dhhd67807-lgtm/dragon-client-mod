package com.dragonclient.mixin;

import com.dragonclient.cosmetics.GearSkinManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HeldItemContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(net.minecraft.client.item.ItemModelManager.class)
public class MixinItemModelManagerGearSkins {

    @ModifyVariable(method = "updateForLivingEntity", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private ItemStack dragonclient$applyGearSkinUpdateForLivingEntity(
        ItemStack stack,
        ItemRenderState renderState,
        ItemStack sourceStack,
        ItemDisplayContext displayContext,
        LivingEntity entity
    ) {
        return GearSkinManager.getRenderStackForEntity(sourceStack, entity);
    }

    @ModifyVariable(method = "updateForNonLivingEntity", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private ItemStack dragonclient$applyGearSkinUpdateForNonLivingEntity(
        ItemStack stack,
        ItemRenderState renderState,
        ItemStack sourceStack,
        ItemDisplayContext displayContext,
        Entity entity
    ) {
        return GearSkinManager.getRenderStackForEntity(sourceStack, entity);
    }

    @ModifyVariable(method = "clearAndUpdate", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private ItemStack dragonclient$applyGearSkinClearAndUpdate(
        ItemStack stack,
        ItemRenderState renderState,
        ItemStack sourceStack,
        ItemDisplayContext displayContext,
        World world,
        HeldItemContext heldContext,
        int seed
    ) {
        Entity holder = heldContext != null ? heldContext.getEntity() : null;
        return GearSkinManager.getRenderStackForEntity(sourceStack, holder);
    }

    @ModifyVariable(method = "update", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private ItemStack dragonclient$applyGearSkinUpdate(
        ItemStack stack,
        ItemRenderState renderState,
        ItemStack sourceStack,
        ItemDisplayContext displayContext,
        World world,
        HeldItemContext heldContext,
        int seed
    ) {
        Entity holder = heldContext != null ? heldContext.getEntity() : null;
        return GearSkinManager.getRenderStackForEntity(sourceStack, holder);
    }
}
