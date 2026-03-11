package com.dragonclient.mixin;

import com.dragonclient.cosmetics.CapeManager;
import com.dragonclient.cosmetics.SkinManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
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
    public SkinTextures getSkin() {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        boolean isLocalPlayer = client != null && player == client.player;
        String playerName = player.getName().getString();

        PlayerListEntry entry = getPlayerListEntry();
        SkinTextures vanilla = entry != null ? entry.getSkinTextures() : DefaultSkinHelper.getSkinTextures(player.getGameProfile());

        Identifier customSkin = SkinManager.getInstance().getCustomSkin(playerName);
        Identifier customCape = null;
        if (isLocalPlayer) {
            CapeManager capeManager = CapeManager.getInstance();
            if (capeManager.hasCapeEquipped()) {
                customCape = capeManager.getCapeTexture();
            }
        }

        AssetInfo.TextureAsset body = customSkin != null
            ? new AssetInfo.TextureAssetInfo(customSkin, customSkin)
            : vanilla.body();
        AssetInfo.TextureAsset cape = customCape != null
            ? new AssetInfo.TextureAssetInfo(customCape, customCape)
            : vanilla.cape();
        AssetInfo.TextureAsset elytra = customCape != null
            ? new AssetInfo.TextureAssetInfo(customCape, customCape)
            : vanilla.elytra();

        PlayerSkinType model = vanilla.model();
        if (customSkin != null) {
            String skinModel = SkinManager.getInstance().getSkinModel(playerName);
            model = "slim".equalsIgnoreCase(skinModel) ? PlayerSkinType.SLIM : PlayerSkinType.WIDE;
        }

        return new SkinTextures(body, cape, elytra, model, vanilla.secure());
    }
}
