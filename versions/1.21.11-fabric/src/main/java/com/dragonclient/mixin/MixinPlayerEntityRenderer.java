package com.dragonclient.mixin;

import com.dragonclient.cosmetics.SkinManager;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRenderer {
    
    /**
     * @author DragonClient
     * @reason Custom skin texture support for 1.21.11
     */
    @Overwrite
    public Identifier getTexture(PlayerEntityRenderState state) {
        try {
            // Try to get player name from displayName
            if (state.displayName != null) {
                String playerName = state.displayName.getString();
                System.out.println("[DragonClient] getTexture called for: " + playerName);
                
                if (playerName != null && !playerName.isEmpty()) {
                    Identifier customSkin = SkinManager.getInstance().getCustomSkin(playerName);
                    
                    if (customSkin != null) {
                        System.out.println("[DragonClient] Applying custom skin: " + customSkin);
                        return customSkin;
                    }
                }
            }
            
            // Fallback - use reflection to get texture from skinTextures
            if (state.skinTextures != null) {
                try {
                    // Try to call texture() method via reflection
                    java.lang.reflect.Method textureMethod = state.skinTextures.getClass().getMethod("texture");
                    return (Identifier) textureMethod.invoke(state.skinTextures);
                } catch (Exception e) {
                    System.err.println("[DragonClient] Could not get texture from skinTextures: " + e.getMessage());
                }
            }
            
            // Ultimate fallback - default Steve skin
            System.out.println("[DragonClient] Using default Steve skin");
            return Identifier.of("minecraft", "textures/entity/player/wide/steve.png");
        } catch (Exception e) {
            System.err.println("[DragonClient] Error in getTexture: " + e.getMessage());
            e.printStackTrace();
            // Return default on error
            return Identifier.of("minecraft", "textures/entity/player/wide/steve.png");
        }
    }
}
