package com.dragonclient.mixin;

import net.minecraft.text.Text;

import com.dragonclient.util.TierTagManager;

import com.dragonclient.cosmetics.CapeManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public class MixinAbstractClientPlayerEntity {
    
    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true, require = 0)
    private void onGetSkinTextures(CallbackInfoReturnable<SkinTextures> cir) {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
        if (MinecraftClient.getInstance() == null || player != MinecraftClient.getInstance().player) {
            return;
        }
        CapeManager capeManager = CapeManager.getInstance();
        if (capeManager.hasCapeEquipped()) {
            Identifier customCape = capeManager.getCapeTexture();
            if (customCape != null) {
                SkinTextures original = cir.getReturnValue();
                SkinTextures modified = new SkinTextures(
                    original.texture(),
                    original.textureUrl(),
                    customCape, // Custom cape texture
                    original.elytraTexture(),
                    original.model(),
                    original.secure()
                );
                cir.setReturnValue(modified);
            }
        }
    }
}
