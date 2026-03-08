package com.dragonclient.mixin;

import com.dragonclient.cosmetics.SkinManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public class MixinPlayerEntityModel<T extends LivingEntity> {
    
    @Inject(
        method = "renderArm",
        at = @At(value = "INVOKE", 
            target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V",
            ordinal = 0
        )
    )
    private void onRenderArm(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, 
                            float red, float green, float blue, float alpha, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        
        if (player != null) {
            String playerName = player.getName().getString();
            Identifier customSkin = SkinManager.getInstance().getCustomSkin(playerName);
            
            if (customSkin != null) {
                // Bind custom skin texture for first-person arms
                RenderSystem.setShaderTexture(0, customSkin);
            }
        }
    }
}
