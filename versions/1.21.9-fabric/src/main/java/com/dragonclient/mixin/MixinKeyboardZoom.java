package com.dragonclient.mixin;

import com.dragonclient.module.movement.FreelookModule;
import com.dragonclient.module.visual.ZoomModule;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboardZoom {

    @Inject(method = "onKey", at = @At("HEAD"))
    private void dragonclient$handleVisualKeys(long window, int action, KeyInput input, CallbackInfo ci) {
        int key = input.key();
        if (key != GLFW.GLFW_KEY_C
            && key != GLFW.GLFW_KEY_LEFT_ALT
            && key != GLFW.GLFW_KEY_RIGHT_ALT) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null || window != client.getWindow().getHandle()) {
            return;
        }

        if (key == GLFW.GLFW_KEY_C) {
            if (action == GLFW.GLFW_RELEASE) {
                ZoomModule.setZooming(false);
                return;
            }

            if (action != GLFW.GLFW_PRESS) {
                return;
            }

            if (!ZoomModule.enabled || client.player == null || client.currentScreen != null) {
                return;
            }

            ZoomModule.setZooming(true);
            return;
        }

        if (key == GLFW.GLFW_KEY_LEFT_ALT || key == GLFW.GLFW_KEY_RIGHT_ALT) {
            if (action != GLFW.GLFW_PRESS) {
                return;
            }

            if (FreelookModule.enabled
                && client.player != null
                && client.currentScreen == null) {
                FreelookModule.toggleFreelook(client);
            }
        }
    }
}
