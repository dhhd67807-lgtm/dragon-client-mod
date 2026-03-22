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

    @Shadow
    @Nullable
    public abstract PlayerListEntry getPlayerListEntry();

    private static String dragonclient$getLookupName(AbstractClientPlayerEntity player) {
        if (player != null) {
            Object profile = player.getGameProfile();
            if (profile != null) {
                try {
                    Object value = profile.getClass().getMethod("name").invoke(profile);
                    if (value instanceof String s && !s.isBlank()) {
                        return s;
                    }
                } catch (Exception ignored) {
                }
                try {
                    Object value = profile.getClass().getMethod("getName").invoke(profile);
                    if (value instanceof String s && !s.isBlank()) {
                        return s;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return player == null ? "" : player.getName().getString();
    }

    /**
     * @author DragonClient
     * @reason Force custom skin/cape application for every player render context.
     */
    @Overwrite
    public SkinTextures getSkin() {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;

        PlayerListEntry entry = getPlayerListEntry();
        SkinTextures vanilla = entry != null
            ? entry.getSkinTextures()
            : DefaultSkinHelper.getSkinTextures(player.getGameProfile());

        String playerName = dragonclient$getLookupName(player);
        SkinManager skinManager = SkinManager.getInstance();
        Identifier customSkin = skinManager.getCustomSkin(playerName);
        Identifier customCape = skinManager.getCustomCape(playerName);

        MinecraftClient client = MinecraftClient.getInstance();
        boolean isLocalPlayer = client != null && player == client.player;
        if (customCape == null && isLocalPlayer) {
            CapeManager capeManager = CapeManager.getInstance();
            if (capeManager.hasCapeEquipped()) {
                customCape = capeManager.getCapeTexture();
            }
        }

        if (customSkin == null && customCape == null) {
            return vanilla;
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
            String skinModel = skinManager.getSkinModel(playerName);
            model = "slim".equalsIgnoreCase(skinModel)
                ? PlayerSkinType.SLIM
                : PlayerSkinType.WIDE;
        }

        return new SkinTextures(body, cape, elytra, model, vanilla.secure());
    }
}
