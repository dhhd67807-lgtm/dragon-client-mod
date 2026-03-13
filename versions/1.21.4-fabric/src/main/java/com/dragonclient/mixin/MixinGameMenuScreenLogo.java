package com.dragonclient.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public class MixinGameMenuScreenLogo {
    private static final Identifier DRAGONCLIENT_TRAPCODE_LOGO =
        Identifier.of("dragonclient", "textures/gui/title/trapcode.png");
    private static final float DRAGONCLIENT_TRAPCODE_ASPECT = 1280.0f / 1043.0f;

    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void dragonclient$renderSmallTrapcodeLogo(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        int logoHeight = Math.max(30, Math.round(Math.min(screenWidth * 0.05f, screenHeight * 0.06f)));
        int logoWidth = Math.max(36, Math.round(logoHeight * DRAGONCLIENT_TRAPCODE_ASPECT));
        int x = (screenWidth - logoWidth) / 2;
        int y = 12;

        context.drawTexture(
            net.minecraft.client.render.RenderLayer::getGuiTextured,
            DRAGONCLIENT_TRAPCODE_LOGO,
            x,
            y,
            0.0f,
            0.0f,
            logoWidth,
            logoHeight,
            logoWidth,
            logoHeight
        );
    }
}
