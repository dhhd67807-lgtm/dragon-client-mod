package com.dragonclient.mixin;

import com.dragonclient.module.visual.ZoomModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouseZoomScroll {

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$adjustZoomFromScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (!ZoomModule.enabled || !ZoomModule.isZooming || vertical == 0.0) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null || window != client.getWindow().getHandle()) {
            return;
        }

        // Only hijack scrolling while actively zooming in-game.
        if (client.currentScreen != null || client.player == null) {
            return;
        }

        ZoomModule.adjustZoomFromScroll(vertical);
        ci.cancel();
    }
}
