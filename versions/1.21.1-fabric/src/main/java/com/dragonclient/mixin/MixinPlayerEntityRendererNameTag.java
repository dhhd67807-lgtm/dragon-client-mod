package com.dragonclient.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRendererNameTag {
    private static final Identifier DRAGONCLIENT_NAME_TAG_ICON =
        Identifier.of("dragonclient", "textures/gui/cs_star_8.png");
    private static final float DRAGONCLIENT_NAME_TAG_ICON_WIDTH = 7.0f;
    private static final float DRAGONCLIENT_NAME_TAG_ICON_HEIGHT = 7.0f;
    private static final float DRAGONCLIENT_NAME_TAG_ICON_GAP = 2.0f;

    private boolean dragonclient$shouldForceNameTag(Entity entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        return entity instanceof PlayerEntity && client != null && entity == client.player;
    }

    @Inject(method = "hasLabel(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$hasLabelOld(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (dragonclient$shouldForceNameTag(entity)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hasLabel(Lnet/minecraft/entity/Entity;D)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$hasLabelNew(Entity entity, double distance, CallbackInfoReturnable<Boolean> cir) {
        if (dragonclient$shouldForceNameTag(entity)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
        method = "renderLabelIfPresent(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IF)V",
        at = @At("TAIL"),
        require = 0
    )
    private void dragonclient$renderNameTagIcon(
        AbstractClientPlayerEntity player,
        Text text,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        float tickDelta,
        CallbackInfo ci
    ) {
        if (!dragonclient$shouldForceNameTag(player) || text == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) {
            return;
        }

        float nameWidth = client.textRenderer.getWidth(text);
        float iconLeft = -nameWidth / 2.0f - DRAGONCLIENT_NAME_TAG_ICON_GAP - DRAGONCLIENT_NAME_TAG_ICON_WIDTH;
        float iconTop = 1.0f;
        MatrixStack.Entry entry = matrices.peek();

        dragonclient$drawIcon(
            entry,
            vertexConsumers.getBuffer(RenderLayer.getTextSeeThrough(DRAGONCLIENT_NAME_TAG_ICON)),
            iconLeft,
            iconTop,
            DRAGONCLIENT_NAME_TAG_ICON_WIDTH,
            DRAGONCLIENT_NAME_TAG_ICON_HEIGHT,
            light,
            0xA0FFFFFF
        );
        dragonclient$drawIcon(
            entry,
            vertexConsumers.getBuffer(RenderLayer.getText(DRAGONCLIENT_NAME_TAG_ICON)),
            iconLeft,
            iconTop,
            DRAGONCLIENT_NAME_TAG_ICON_WIDTH,
            DRAGONCLIENT_NAME_TAG_ICON_HEIGHT,
            light,
            0xFFFFFFFF
        );
    }

    private static void dragonclient$drawIcon(
        MatrixStack.Entry entry,
        VertexConsumer vertexConsumer,
        float left,
        float top,
        float width,
        float height,
        int light,
        int color
    ) {
        int alpha = color >>> 24;
        int red = (color >>> 16) & 0xFF;
        int green = (color >>> 8) & 0xFF;
        int blue = color & 0xFF;
        float right = left + width;
        float bottom = top + height;

        vertexConsumer.vertex(entry, left, top, 0.0f).color(red, green, blue, alpha).texture(0.0f, 0.0f).light(light);
        vertexConsumer.vertex(entry, left, bottom, 0.0f).color(red, green, blue, alpha).texture(0.0f, 1.0f).light(light);
        vertexConsumer.vertex(entry, right, bottom, 0.0f).color(red, green, blue, alpha).texture(1.0f, 1.0f).light(light);
        vertexConsumer.vertex(entry, right, top, 0.0f).color(red, green, blue, alpha).texture(1.0f, 0.0f).light(light);
    }
}
