package com.dragonclient.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(PressableTextWidget.class)
public class MixinPressableTextWidget {
    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$hideMojangCopyrightText(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Text message = ((PressableTextWidget) (Object) this).getMessage();
        if (message == null) {
            return;
        }

        String text = message.getString();
        if (text == null) {
            return;
        }

        String normalized = text.toLowerCase(Locale.ROOT);
        if (normalized.contains("mojang") || normalized.contains("do not distribute")) {
            ci.cancel();
        }
    }
}
