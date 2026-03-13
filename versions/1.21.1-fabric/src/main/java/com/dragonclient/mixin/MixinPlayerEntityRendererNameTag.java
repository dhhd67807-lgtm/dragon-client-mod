package com.dragonclient.mixin;

import com.dragonclient.module.visual.NametagModule;
import com.dragonclient.util.TierTagManager;
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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Mixin(PlayerEntityRenderer.class)
public abstract class MixinPlayerEntityRendererNameTag {
    private static final Identifier DRAGONCLIENT_NAME_TAG_ICON =
        Identifier.of("dragonclient", "textures/gui/cs_star_8.png");
    private static final float DRAGONCLIENT_NAME_TAG_ICON_WIDTH = 6.0f;
    private static final float DRAGONCLIENT_NAME_TAG_ICON_HEIGHT = 6.0f;
    private static final int DRAGONCLIENT_STAR_PADDING_SPACES = 2;
    private static final float DRAGONCLIENT_TIER_ICON_WIDTH = 6.0f;
    private static final float DRAGONCLIENT_TIER_ICON_HEIGHT = 6.0f;
    private static final float DRAGONCLIENT_TIER_ICON_GAP = 0.25f;

    @Shadow
    protected abstract void renderLabelIfPresent(
        AbstractClientPlayerEntity player,
        Text text,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        float tickDelta
    );

    private boolean dragonclient$shouldForceNameTag(Entity entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        return NametagModule.enabled && entity instanceof PlayerEntity && client != null && entity == client.player;
    }

    private Text dragonclient$withStarPadding(Text name) {
        return Text.literal(" ".repeat(DRAGONCLIENT_STAR_PADDING_SPACES)).append(name.copy());
    }

    @Inject(method = "hasLabel(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$hasLabelOld(Entity entity, CallbackInfoReturnable<Boolean> cir) {
    }

    @Inject(method = "hasLabel(Lnet/minecraft/entity/Entity;D)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$hasLabelNew(Entity entity, double distance, CallbackInfoReturnable<Boolean> cir) {
    }

    @Inject(
        method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("TAIL"),
        require = 0
    )
    private void dragonclient$renderOwnPlayerNameTag(
        AbstractClientPlayerEntity player,
        float yaw,
        float tickDelta,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        CallbackInfo ci
    ) {
        if (!dragonclient$shouldForceNameTag(player)) {
            return;
        }

        Text baseName = player.getName().copy();
        Text decoratedName = TierTagManager.decorateName(dragonclient$withStarPadding(baseName), player.getName().getString());
        this.renderLabelIfPresent(player, decoratedName, matrices, vertexConsumers, light, tickDelta);
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
        float reservedWidth = client.textRenderer.getWidth(" ") * DRAGONCLIENT_STAR_PADDING_SPACES;
        float tierEnd = nameLeft - reservedWidth;
        float iconLeft = tierEnd + ((reservedWidth - DRAGONCLIENT_NAME_TAG_ICON_WIDTH) * 0.5f);
        float tierIconLeft = textLeft - DRAGONCLIENT_TIER_ICON_WIDTH - DRAGONCLIENT_TIER_ICON_GAP;
        float iconTop = 1.0f;
        int litLight = light;
        boolean pushedTransform = false;
        MatrixStack.Entry entry = matrices.peek();
        if (!dragonclient$isLabelSpace(entry)) {
            matrices.push();
            matrices.translate(0.0, player.getHeight() + 0.5, 0.0);
            matrices.multiply(client.getEntityRenderDispatcher().getRotation());
            matrices.scale(0.025f, -0.025f, 0.025f);
            entry = matrices.peek();
            pushedTransform = true;
        }

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

        if (pushedTransform) {
            matrices.pop();
        }
    }

    private static boolean dragonclient$isLabelSpace(MatrixStack.Entry entry) {
        var matrix = entry.getPositionMatrix();
        float sx = (float)Math.sqrt(matrix.m00() * matrix.m00() + matrix.m01() * matrix.m01() + matrix.m02() * matrix.m02());
        return sx < 0.1f;
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
