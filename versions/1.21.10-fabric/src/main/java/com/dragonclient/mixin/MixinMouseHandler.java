package com.dragonclient.mixin;

import com.dragonclient.module.movement.FreelookModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouseHandler {

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$handleMiddleClickFreelook(long window, MouseInput input, int action, CallbackInfo ci) {
        int button = input.button();
        if (button != GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null || window != client.getWindow().getHandle()) {
            return;
        }

        if (action != GLFW.GLFW_PRESS) {
            return;
        }

        if (FreelookModule.enabled
            && client.player != null
            && client.currentScreen == null) {
            FreelookModule.toggleFreelook(client);
            ci.cancel();
        }
    }
}
