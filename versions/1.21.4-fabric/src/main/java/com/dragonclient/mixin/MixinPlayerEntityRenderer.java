package com.dragonclient.mixin;

import com.dragonclient.cosmetics.SkinManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRenderer {
    
    @Inject(method = "getTexture(Lnet/minecraft/client/network/AbstractClientPlayerEntity;)Lnet/minecraft/util/Identifier;", at = @At("HEAD"), cancellable = true)
    private void onGetTexture(AbstractClientPlayerEntity player, CallbackInfoReturnable<Identifier> cir) {
        String playerName = player.getName().getString();
        Identifier customSkin = SkinManager.getInstance().getCustomSkin(playerName);
        
        if (customSkin != null) {
            cir.setReturnValue(customSkin);
        }
    }
}
