package com.dragonclient.mixin;

import com.dragonclient.cosmetics.CapeManager;
import com.dragonclient.gui.DummyPlayerEntity;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRendererDummyPreview {

    @Inject(
        method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
        at = @At("TAIL")
    )
    private void dragonclient$forceDummyPreviewTextures(
        AbstractClientPlayerEntity player,
        PlayerEntityRenderState state,
        float tickDelta,
        CallbackInfo ci
    ) {
        if (player instanceof DummyPlayerEntity dummy) {
            SkinTextures expected = dummy.getSkinTextures();
            state.skinTextures = expected;
            state.capeVisible = true;
            return;
        }

        CapeManager capeManager = CapeManager.getInstance();
        if (!capeManager.hasCapeEquipped()) {
            return;
        }

        Identifier equippedCape = capeManager.getCapeTexture();
        SkinTextures current = state.skinTextures;
        if (equippedCape != null && current != null && !Objects.equals(current.capeTexture(), equippedCape)) {
            state.skinTextures = new SkinTextures(
                current.texture(),
                current.textureUrl(),
                equippedCape,
                current.elytraTexture(),
                current.model(),
                current.secure()
            );
        }
        state.capeVisible = true;
    }
}
