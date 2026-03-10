package com.dragonclient.mixin;

import com.dragonclient.cosmetics.CapeManager;
import com.dragonclient.cosmetics.SkinManager;
import com.dragonclient.gui.DummyPlayerEntity;
import com.dragonclient.util.CosmeticsDebugLogger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class MixinAbstractClientPlayerEntity {

    @Shadow
    @Nullable
    public abstract PlayerListEntry getPlayerListEntry();

    /**
     * @author DragonClient
     * @reason Custom skin and cape textures for all player rendering contexts
     */
    @Overwrite
    public SkinTextures getSkinTextures() {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;

        // Skip mixin logic for DummyPlayerEntity — each dummy has its own cape texture
        // that must not be overridden by the global CapeManager state
        if (player instanceof DummyPlayerEntity dummy) {
            SkinTextures textures = dummy.getDummySkinTextures();
            String dummyId = Integer.toHexString(System.identityHashCode(dummy));
            CosmeticsDebugLogger.logEvery(
                    "mixin-abstract-dummy-" + dummyId,
                    1500,
                    "MixinAbstractClientPlayerEntity dummy=" + dummyId
                            + " skin=" + textures.texture()
                            + " cape=" + textures.capeTexture());
            return textures;
        }

        boolean isLocalPlayer = MinecraftClient.getInstance() != null && player == MinecraftClient.getInstance().player;
        String playerName = player.getName().getString();

        // Get vanilla skin textures as fallback
        PlayerListEntry entry = getPlayerListEntry();
        if (entry == null) {
            // Return default Steve skin if no player list entry
            Identifier defaultSkin = Identifier.of("minecraft", "textures/entity/player/wide/steve.png");
            return new SkinTextures(defaultSkin, null, null, null, SkinTextures.Model.WIDE, false);
        }

        SkinTextures vanilla = entry.getSkinTextures();

        // Check for custom skin
        Identifier customSkin = SkinManager.getInstance().getCustomSkin(playerName);
        Identifier skinTexture = customSkin != null ? customSkin : vanilla.texture();

        // Check for custom cape
        CapeManager capeManager = CapeManager.getInstance();
        Identifier capeTexture = vanilla.capeTexture();
        if (isLocalPlayer && capeManager.hasCapeEquipped()) {
            Identifier customCape = capeManager.getCapeTexture();
            if (customCape != null) {
                capeTexture = customCape;
            }
        }

        // Determine model (slim/wide)
        SkinTextures.Model model = vanilla.model();
        if (customSkin != null) {
            // Use model from SkinManager if available
            String skinModel = SkinManager.getInstance().getSkinModel(playerName);
            model = "slim".equals(skinModel) ? SkinTextures.Model.SLIM : SkinTextures.Model.WIDE;
        }

        return new SkinTextures(
                skinTexture,
                vanilla.textureUrl(),
                capeTexture,
                vanilla.elytraTexture(),
                model,
                vanilla.secure());
    }
}
