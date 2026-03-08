package com.dragonclient.mixin;

import com.dragonclient.cosmetics.CapeManager;
import com.dragonclient.cosmetics.SkinManager;
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
    
    @Shadow @Nullable
    public abstract PlayerListEntry getPlayerListEntry();
    
    /**
     * @author DragonClient
     * @reason Custom skin and cape textures for all player rendering contexts
     */
    @Overwrite
    public SkinTextures getSkinTextures() {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity)(Object)this;
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
        if (capeManager.hasCapeEquipped()) {
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
            vanilla.secure()
        );
    }
}
