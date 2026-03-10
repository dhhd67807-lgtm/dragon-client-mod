package com.dragonclient.mixin;

import com.dragonclient.cosmetics.CapeManager;
import com.dragonclient.gui.DummyPlayerEntity;
import com.dragonclient.util.CosmeticsDebugLogger;
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
            SkinTextures expected = dummy.getDummySkinTextures();
            SkinTextures before = state.skinTextures;
            String dummyId = Integer.toHexString(System.identityHashCode(dummy));

            if (before == null || !Objects.equals(before.capeTexture(), expected.capeTexture())) {
                CosmeticsDebugLogger.logEvery(
                        "mixin-renderer-dummy-mismatch-" + dummyId,
                        600,
                        "RendererState mismatch dummy=" + dummyId
                                + " state=" + Integer.toHexString(System.identityHashCode(state))
                                + " beforeCape=" + (before == null ? "null" : before.capeTexture())
                                + " expectedCape=" + expected.capeTexture());
            }

            state.skinTextures = expected;
            state.capeVisible = true;

            CosmeticsDebugLogger.logEvery(
                    "mixin-renderer-dummy-" + dummyId,
                    1200,
                    "RendererState applied dummy=" + dummyId
                            + " state=" + Integer.toHexString(System.identityHashCode(state))
                            + " skinTextures=" + Integer.toHexString(System.identityHashCode(state.skinTextures))
                            + " finalCape=" + state.skinTextures.capeTexture());
            return;
        }

        // Ensure equipped custom cape is visible on the real player too, not only preview dummies.
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
        CosmeticsDebugLogger.logEvery(
                "mixin-renderer-real-" + player.getUuidAsString(),
                1500,
                "RendererState realPlayer=" + player.getName().getString()
                        + " state=" + Integer.toHexString(System.identityHashCode(state))
                        + " skinTextures=" + (state.skinTextures == null ? "null" : Integer.toHexString(System.identityHashCode(state.skinTextures)))
                        + " finalCape=" + (state.skinTextures == null ? "null" : state.skinTextures.capeTexture())
                        + " capeVisible=" + state.capeVisible);
    }
}
