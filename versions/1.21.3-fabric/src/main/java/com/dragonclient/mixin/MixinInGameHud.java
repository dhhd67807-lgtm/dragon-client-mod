package com.dragonclient.mixin;

import com.dragonclient.DragonClientMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {
    
    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        try {
            DragonClientMod mod = DragonClientMod.getInstance();
            if (mod != null && mod.getHudRenderer() != null) {
                MinecraftClient client = MinecraftClient.getInstance();
                
                System.out.println("MixinInGameHud.onRender called - GUI scale: " + client.getWindow().getScaleFactor());
                
                // Save current matrix state (1.21.1-1.21.10 use push/pop)
                context.getMatrices().push();
                
                // Reset to window coordinates - scale will be applied per-module
                double guiScale = client.getWindow().getScaleFactor();
                float baseScale = 4.0f;  // Base 4x scale
                // 1.21.1-1.21.10 use 3-parameter scale
                context.getMatrices().scale(baseScale / (float)guiScale, baseScale / (float)guiScale, 1.0f);
                
                // Render HUD
                mod.getHudRenderer().render(context, 1.0f);
                
                // Restore matrix (1.21.1-1.21.10 use push/pop)
                context.getMatrices().pop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
