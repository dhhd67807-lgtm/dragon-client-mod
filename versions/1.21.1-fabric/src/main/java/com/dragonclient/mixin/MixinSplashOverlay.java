package com.dragonclient.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;

@Mixin(SplashOverlay.class)
public class MixinSplashOverlay {
    private static final int DRAGONCLIENT_SPLASH_BACKGROUND = 0xFF000000;
    private static final Identifier DRAGONCLIENT_SPLASH_HQ_LOGO =
        Identifier.of("dragonclient", "textures/gui/title/new-dragon-hq.png");
    private static final float DRAGONCLIENT_SPLASH_LOGO_ASPECT = 3152.0f / 2546.0f;
    private static final float DRAGONCLIENT_SPLASH_LOGO_SCALE = 1.18f;
    private static final int DRAGONCLIENT_SPLASH_LOGO_Y_OFFSET = 2;

    @Shadow
    @Final
    private static Identifier LOGO;

    @Inject(method = "init", at = @At("TAIL"), require = 0)
    private static void dragonclient$registerSplashLogo(MinecraftClient client, CallbackInfo ci) {
        try (InputStream stream = MixinSplashOverlay.class.getResourceAsStream(
            "/assets/dragonclient/textures/gui/title/splash-transparent.png")) {
            if (stream == null) {
                return;
            }

            NativeImage image = NativeImage.read(stream);
            NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
            client.getTextureManager().registerTexture(LOGO, texture);
        } catch (Exception ignored) {
        }
    }

    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void dragonclient$renderCustomSplashLogo(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        float baseHeight = (float) (Math.min(screenWidth * 0.75, screenHeight) * 0.25);
        int logoHeight = Math.max(36, Math.round(baseHeight * DRAGONCLIENT_SPLASH_LOGO_SCALE));
        int logoWidth = Math.max(44, Math.round(logoHeight * DRAGONCLIENT_SPLASH_LOGO_ASPECT));

        int x = (screenWidth - logoWidth) / 2;
        int y = (screenHeight / 2) - (logoHeight / 2) + DRAGONCLIENT_SPLASH_LOGO_Y_OFFSET;

        context.drawTexture(
            DRAGONCLIENT_SPLASH_HQ_LOGO,
            x,
            y,
            0,
            0.0f,
            0.0f,
            logoWidth,
            logoHeight,
            logoWidth,
            logoHeight
        );
    }

    @Inject(method = "method_35733", at = @At("HEAD"), cancellable = true, require = 0)
    private static void dragonclient$forceBlackSplashBackground(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(DRAGONCLIENT_SPLASH_BACKGROUND);
    }
}
