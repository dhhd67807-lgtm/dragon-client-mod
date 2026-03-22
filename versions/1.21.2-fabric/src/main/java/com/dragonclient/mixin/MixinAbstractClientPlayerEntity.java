package com.dragonclient.mixin;

import com.dragonclient.cosmetics.CapeManager;
import com.dragonclient.cosmetics.SkinManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class MixinAbstractClientPlayerEntity {

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true, require = 0)
    private void dragonclient$injectCustomTextures(CallbackInfoReturnable<SkinTextures> cir) {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
        SkinTextures vanilla = cir.getReturnValue();
        if (vanilla == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        boolean isLocalPlayer = client != null && player == client.player;

        String playerName = player.getGameProfile() != null ? player.getGameProfile().getName() : null;
        if (playerName == null || playerName.isBlank()) {
            playerName = player.getName().getString();
        }

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

        Identifier skinTexture = customSkin != null ? customSkin : vanilla.texture();
        Identifier capeTexture = customCape != null ? customCape : vanilla.capeTexture();
        Identifier elytraTexture = customCape != null ? customCape : vanilla.elytraTexture();

        SkinTextures.Model model = vanilla.model();
        if (customSkin != null) {
            String skinModel = skinManager.getSkinModel(playerName);
            model = "slim".equalsIgnoreCase(skinModel)
                ? SkinTextures.Model.SLIM
                : SkinTextures.Model.WIDE;
        }

        cir.setReturnValue(new SkinTextures(
            skinTexture,
            vanilla.textureUrl(),
            capeTexture,
            elytraTexture,
            model,
            vanilla.secure()
        ));
    }
}
