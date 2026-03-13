package com.dragonclient.mixin;

import com.dragonclient.DragonClientMod;
import com.dragonclient.module.movement.FreelookModule;
import com.dragonclient.module.visual.CustomCrosshairModule;
import com.dragonclient.module.visual.HitColorModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void dragonclient$hideHudDuringFreelook(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (FreelookModule.isFreelooking) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.currentScreen != null && client.currentScreen.getClass().getSimpleName().equals("HudEditorScreen")) {
                return;
            }

            DragonClientMod mod = DragonClientMod.getInstance();
            if (mod != null && mod.getHudRenderer() != null) {
                mod.getHudRenderer().render(context, 1.0f);
            }
        } catch (Exception e) {
            DragonClientMod.LOGGER.error("Error rendering HUD", e);
        }
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$renderCustomCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        // Only replace vanilla crosshair when Custom Crosshair is explicitly enabled.
        if (!CustomCrosshairModule.enabled) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.options.hudHidden) {
            return;
        }

        // Hide crosshair in third-person views (F5), render only in first person.
        if (!client.options.getPerspective().isFirstPerson()) {
            ci.cancel();
            return;
        }

        int centerX = client.getWindow().getScaledWidth() / 2;
        int centerY = client.getWindow().getScaledHeight() / 2;

        int color = 0xFFFFFFFF;
        if (HitColorModule.enabled
            && client.crosshairTarget != null
            && client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            color = HitColorModule.HIT_COLOR;
        }

        int size = Math.max(2, CustomCrosshairModule.CROSSHAIR_SIZE);
        int gap = Math.max(1, CustomCrosshairModule.CROSSHAIR_GAP);
        int thickness = Math.max(1, CustomCrosshairModule.CROSSHAIR_THICKNESS);

        drawCrosshair(context, centerX, centerY, size, gap, thickness, color);
        ci.cancel();
    }

    private static void drawCrosshair(DrawContext context, int cx, int cy, int size, int gap, int thickness, int color) {
        context.fill(cx - thickness, cy - gap - size, cx + thickness + 1, cy - gap, color);
        context.fill(cx - thickness, cy + gap, cx + thickness + 1, cy + gap + size, color);
        context.fill(cx - gap - size, cy - thickness, cx - gap, cy + thickness + 1, color);
        context.fill(cx + gap, cy - thickness, cx + gap + size, cy + thickness + 1, color);
    }
}
