package com.dragonclient.mixin;

import com.dragonclient.module.visual.ClearWaterModule;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public class MixinInGameOverlayRendererClearWater {

    @Inject(method = "renderUnderwaterOverlay", at = @At("HEAD"), cancellable = true, require = 0)
    private static void dragonclient$hideUnderwaterOverlay(CallbackInfo ci) {
        if (ClearWaterModule.enabled) {
            ci.cancel();
        }
    }
}
