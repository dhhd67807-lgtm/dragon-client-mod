package com.dragonclient.mixin;

import com.dragonclient.cosmetics.CapeManager;
import com.dragonclient.cosmetics.SkinManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class MixinAbstractClientPlayerEntity {

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true, require = 0)
    private void dragonclient$injectCustomTextures(CallbackInfoReturnable<SkinTextures> cir) {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
        SkinTextures vanilla = cir.getReturnValue();
        if (vanilla == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        boolean isLocalPlayer = client != null && player == client.player;

        String playerName = player.getName().getString();

        SkinManager skinManager = SkinManager.getInstance();
        Identifier customSkin = skinManager.getCustomSkin(playerName);
        Identifier customCape = skinManager.getCustomCape(playerName);

        if (customCape == null && isLocalPlayer) {
            CapeManager capeManager = CapeManager.getInstance();
            if (capeManager.hasCapeEquipped()) {
                customCape = capeManager.getCapeTexture();
            }
        }

        if (customSkin == null && customCape == null) {
            return;
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

        cir.setReturnValue(new SkinTextures(body, cape, elytra, model, vanilla.secure()));
    }
}
