package com.dragonclient.mixin;

import com.dragonclient.DragonClientClient;
import com.dragonclient.module.movement.FreelookModule;
import com.dragonclient.module.visual.ZoomModule;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboardZoom {

    @Inject(method = "onKey", at = @At("HEAD"))
    private void dragonclient$handleVisualKeys(
        long window,
        int key,
        int scanCode,
        int action,
        int modifiers,
        CallbackInfo ci
    ) {
        boolean zoomKey = dragonclient$isZoomKey(key, scanCode);
        boolean freelookAltKey = key == GLFW.GLFW_KEY_LEFT_ALT || key == GLFW.GLFW_KEY_RIGHT_ALT;
        if (!zoomKey && !freelookAltKey) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null || window != client.getWindow().getHandle()) {
            return;
        }

        if (zoomKey) {
            if (action == GLFW.GLFW_RELEASE) {
                ZoomModule.setZooming(false);
                return;
            }

            if (action != GLFW.GLFW_PRESS && action != GLFW.GLFW_REPEAT) {
                return;
            }

            if (!ZoomModule.enabled || client.player == null || client.currentScreen != null) {
                return;
            }

            ZoomModule.setZooming(true);
            return;
        }

        if (freelookAltKey && action == GLFW.GLFW_PRESS) {
            if (FreelookModule.enabled
                && client.player != null
                && client.currentScreen == null) {
                FreelookModule.toggleFreelook(client);
            }
        }
    }

    private static boolean dragonclient$isZoomKey(int key, int scanCode) {
        KeyBinding zoomBinding = DragonClientClient.getZoomKey();
        if (zoomBinding != null) {
            try {
                if (zoomBinding.matchesKey(key, scanCode)) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return key == GLFW.GLFW_KEY_C;
    }
}
