package com.dragonclient.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {
    private static final Identifier DRAGONCLIENT_TRAPCODE_LOGO =
        Identifier.of("dragonclient", "textures/gui/title/new-dragon-hq.png");
    private static final float DRAGONCLIENT_TRAPCODE_ASPECT = 1280.0f / 1043.0f;

    @Redirect(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/LogoDrawer;draw(Lnet/minecraft/client/gui/DrawContext;IF)V"
        ),
        require = 0
    )
    private void dragonclient$hideVanillaTitleLogo(LogoDrawer drawer, DrawContext context, int screenWidth, float alpha) {
        // Hide vanilla Minecraft logo/edition. We render our custom trapcode logo below.
    }

    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void dragonclient$renderTrapcodeLogo(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int screenWidth = context.getScaledWindowWidth();
        MinecraftClient client = MinecraftClient.getInstance();
        int screenHeight = client != null && client.currentScreen != null
            ? client.currentScreen.height
            : context.getScaledWindowHeight();

        float baseHeight = (float) Math.min(screenWidth * 0.14f, screenHeight * 0.13f);
        int logoHeight = Math.max(72, Math.round(baseHeight));
        int logoWidth = Math.max(88, Math.round(logoHeight * DRAGONCLIENT_TRAPCODE_ASPECT));

        int x = (screenWidth - logoWidth) / 2;
        int firstButtonY = (screenHeight / 4) + 48;
        int y = Math.max(10, firstButtonY - logoHeight - 10);

        context.drawTexture(
            DRAGONCLIENT_TRAPCODE_LOGO,
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

    @Redirect(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/SplashTextRenderer;render(Lnet/minecraft/client/gui/DrawContext;ILnet/minecraft/client/font/TextRenderer;I)V"
        ),
        require = 0
    )
    private void dragonclient$hideYellowSplashText(
        SplashTextRenderer splashTextRenderer,
        DrawContext context,
        int width,
        TextRenderer textRenderer,
        int alpha
    ) {
        // Hide vanilla splash text (yellow rotating text).
    }

    @Redirect(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)V"
        ),
        require = 0
    )
    private void dragonclient$hideBottomLeftVersionText(
        DrawContext context,
        TextRenderer textRenderer,
        String text,
        int x,
        int y,
        int color
    ) {
        // Hide version/mod/fabric text in bottom-left.
    }
}
