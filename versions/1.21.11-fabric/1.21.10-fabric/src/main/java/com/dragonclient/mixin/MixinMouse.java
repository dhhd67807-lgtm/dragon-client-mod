package com.dragonclient.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouse {
    
    // Inject BEFORE Screen.mouseClicked is called - at this point we still have raw double x, y, int button
    // This is Essential's approach - intercept at Mouse level before Click wrapper is created
    @Inject(method = "onMouseButton", 
            at = @At(value = "INVOKE", 
                     target = "Lnet/minecraft/client/gui/screen/Screen;mouseClicked(Lnet/minecraft/client/gui/Click;Z)Z"),
            cancellable = true)
    private void onMouseButton(CallbackInfo ci,
                              @Local(ordinal = 0) Screen screen,
                              @Local(ordinal = 0) double mouseX,
                              @Local(ordinal = 1) double mouseY,
                              @Local(ordinal = 0, argsOnly = true) int mouseButton) {
        
        System.out.println("[DragonClient] MixinMouse.onMouseButton - Screen: " + screen.getClass().getName() + 
                          ", x=" + mouseX + ", y=" + mouseY + ", button=" + mouseButton);
        
        // Only handle our custom screens
        if (screen.getClass().getName().startsWith("com.dragonclient.gui.")) {
            try {
                // Call handleMouseClick method via reflection
                java.lang.reflect.Method handleMethod = screen.getClass().getMethod("handleMouseClick", 
                                                                                    double.class, double.class, int.class);
                boolean handled = (boolean) handleMethod.invoke(screen, mouseX, mouseY, mouseButton);
                
                System.out.println("[DragonClient] Click handled by custom screen: " + handled);
                
                if (handled) {
                    ci.cancel(); // Cancel the event - don't pass to Screen.mouseClicked
                }
            } catch (Exception e) {
                System.err.println("[DragonClient] Error in MixinMouse: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
