package com.dragonclient.mixin;

import com.dragonclient.cosmetics.CapeManager;
import com.dragonclient.cosmetics.SkinManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class MixinAbstractClientPlayerEntity {

    @Shadow @Nullable
    public abstract PlayerListEntry getPlayerListEntry();

    /**
     * @author DragonClient
     * @reason Custom skin and cape textures for all player rendering contexts
     */
    @Overwrite
    public SkinTextures getSkinTextures() {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        boolean isLocalPlayer = client != null && player == client.player;
        String playerName = player.getName().getString();

        // Use vanilla fallback (DefaultSkinHelper) when PlayerListEntry is null.
        // This preserves the authlib skin-loading pipeline in multiplayer.
        PlayerListEntry entry = getPlayerListEntry();
        SkinTextures vanilla = entry != null
            ? entry.getSkinTextures()
            : DefaultSkinHelper.getSkinTextures(player.getGameProfile());

        // Apply DragonClient custom skin if configured
        Identifier customSkin = SkinManager.getInstance().getCustomSkin(playerName);
        Identifier skinTexture = customSkin != null ? customSkin : vanilla.texture();

        // Apply DragonClient custom cape (local player only)
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
            String skinModel = SkinManager.getInstance().getSkinModel(playerName);
            model = "slim".equals(skinModel) ? SkinTextures.Model.SLIM : SkinTextures.Model.WIDE;
        }

        return new SkinTextures(
            skinTexture,
            vanilla.textureUrl(),
            capeTexture,
            vanilla.elytraTexture(),
            model,
            vanilla.secure()
        );
    }
}
