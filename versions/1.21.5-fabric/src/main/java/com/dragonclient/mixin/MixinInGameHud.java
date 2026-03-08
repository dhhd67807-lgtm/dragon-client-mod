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
                
                // Save current matrix state
                context.getMatrices().pushMatrix();
                
                // Reset to window coordinates - scale will be applied per-module
                double guiScale = client.getWindow().getScaleFactor();
                float baseScale = 4.0f;  // Base 4x scale
                context.getMatrices().scale(baseScale / (float)guiScale, baseScale / (float)guiScale);
                
                // Render HUD
                mod.getHudRenderer().render(context, 1.0f);
                
                // Restore matrix
                context.getMatrices().popMatrix();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
