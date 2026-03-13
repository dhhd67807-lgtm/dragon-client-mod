package com.dragonclient.mixin;

import com.dragonclient.util.TierTagManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRendererNameTag {
    private static final Identifier DRAGONCLIENT_NAME_TAG_ICON =
        Identifier.of("dragonclient", "textures/gui/cs_star_8.png");
    private static final float DRAGONCLIENT_NAME_TAG_ICON_WIDTH = 6.0f;
    private static final float DRAGONCLIENT_NAME_TAG_ICON_HEIGHT = 6.0f;
    private static final float DRAGONCLIENT_NAME_TAG_ICON_GAP = 0.5f;
    private static final float DRAGONCLIENT_TIER_ICON_WIDTH = 6.0f;
    private static final float DRAGONCLIENT_TIER_ICON_HEIGHT = 6.0f;
    private static final float DRAGONCLIENT_TIER_ICON_GAP = 0.5f;

    private boolean dragonclient$shouldForceNameTag(PlayerEntity entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && entity == client.player;
    }

    @Inject(
        method = "renderLabelIfPresent(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V", shift = At.Shift.BEFORE),
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

        String playerName = player.getName().getString();
        String tier = TierTagManager.getTierForPlayer(playerName);
        float fullWidth = client.textRenderer.getWidth(text);
        float nameOnlyWidth = client.textRenderer.getWidth(player.getName());
        float textLeft = -fullWidth / 2.0f;
        float nameLeft = textLeft + (fullWidth - nameOnlyWidth);
        float iconLeft = nameLeft - DRAGONCLIENT_NAME_TAG_ICON_GAP - DRAGONCLIENT_NAME_TAG_ICON_WIDTH;
        float tierIconLeft = textLeft - DRAGONCLIENT_TIER_ICON_WIDTH - DRAGONCLIENT_TIER_ICON_GAP;
        float iconTop = 1.0f;
        int litLight = light;
        MatrixStack.Entry entry = matrices.peek();

        if (tier != null && !tier.isBlank()) {
            Identifier tierIcon = Identifier.of("dragonclient", "textures/tier_tags/" + tier.toLowerCase(Locale.ROOT) + ".png");
            VertexConsumer tierSeeThrough = vertexConsumers.getBuffer(RenderLayer.getTextSeeThrough(tierIcon));
            dragonclient$drawIcon(
                entry,
                tierSeeThrough,
                tierIconLeft,
                iconTop,
                DRAGONCLIENT_TIER_ICON_WIDTH,
                DRAGONCLIENT_TIER_ICON_HEIGHT,
                litLight,
                0xA0FFFFFF
            );

            VertexConsumer tierNormal = vertexConsumers.getBuffer(RenderLayer.getText(tierIcon));
            dragonclient$drawIcon(
                entry,
                tierNormal,
                tierIconLeft,
                iconTop,
                DRAGONCLIENT_TIER_ICON_WIDTH,
                DRAGONCLIENT_TIER_ICON_HEIGHT,
                litLight,
                0xFFFFFFFF
            );
        }

        VertexConsumer seeThrough = vertexConsumers.getBuffer(RenderLayer.getTextSeeThrough(DRAGONCLIENT_NAME_TAG_ICON));
        dragonclient$drawIcon(
            entry,
            seeThrough,
            iconLeft,
            iconTop,
            DRAGONCLIENT_NAME_TAG_ICON_WIDTH,
            DRAGONCLIENT_NAME_TAG_ICON_HEIGHT,
            litLight,
            0xA0FFFFFF
        );
        VertexConsumer normal = vertexConsumers.getBuffer(RenderLayer.getText(DRAGONCLIENT_NAME_TAG_ICON));
        dragonclient$drawIcon(
            entry,
            normal,
            iconLeft,
            iconTop,
            DRAGONCLIENT_NAME_TAG_ICON_WIDTH,
            DRAGONCLIENT_NAME_TAG_ICON_HEIGHT,
            litLight,
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
