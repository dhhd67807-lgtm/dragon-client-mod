package com.dragonclient.mixin;

import com.dragonclient.cosmetics.CapeManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public class MixinAbstractClientPlayerEntity {
    
    // 1.21.10: SkinTextures class moved/renamed - cape functionality disabled
    // TODO: Find new SkinTextures location in 1.21.10 mappings
    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true, require = 0)
    private void onGetSkinTextures(CallbackInfoReturnable<Object> cir) {
        // Disabled - SkinTextures unmapped in Yarn 1.21.10+build.1
    }
}
