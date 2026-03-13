package com.dragonclient.mixin;

import com.dragonclient.module.visual.NametagModule;
import com.dragonclient.module.visual.TierTaggerModule;
import com.dragonclient.util.TierTagManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRendererNameTag {
    private static final Identifier DRAGONCLIENT_NAME_TAG_ICON =
        Identifier.of("dragonclient", "textures/gui/cs_star_8.png");
    private static final float DRAGONCLIENT_NAME_TAG_ICON_WIDTH = 6.0f;
    private static final float DRAGONCLIENT_NAME_TAG_ICON_HEIGHT = 6.0f;
    private static final int DRAGONCLIENT_STAR_PADDING_SPACES = 3;
    private static final float DRAGONCLIENT_TIER_ICON_WIDTH = 6.0f;
    private static final float DRAGONCLIENT_TIER_ICON_HEIGHT = 6.0f;
    private static final float DRAGONCLIENT_TIER_ICON_GAP = 0.5f;

    private boolean dragonclient$shouldForceNameTag(Entity entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        return NametagModule.enabled && entity instanceof PlayerEntity && client != null && entity == client.player;
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

    private boolean dragonclient$shouldRenderOwnNameTag(PlayerEntityRenderState state) {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null && state.id == client.player.getId();
    }

    private Text dragonclient$withStarPadding(Text name) {
        return Text.literal(" ".repeat(DRAGONCLIENT_STAR_PADDING_SPACES)).append(name.copy());
    }

    @Inject(
        method = "updateRenderState(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/entity/state/EntityRenderState;F)V",
        at = @At("TAIL"),
        require = 0
    )
    private void dragonclient$forcePlayerLabelState(
        Entity entity,
        EntityRenderState state,
        float tickDelta,
        CallbackInfo ci
    ) {
        if (!(state instanceof PlayerEntityRenderState playerState)) {
            return;
        }

        Text baseName = entity.getDisplayName().copy();
        Text decoratedName = TierTagManager.decorateName(baseName, entity.getName().getString());
        if (TierTaggerModule.enabled) {
            state.displayName = null;
            playerState.playerName = decoratedName.copy();
        }

        if (!dragonclient$shouldForceNameTag(entity)) {
            return;
        }

        state.displayName = TierTagManager.decorateName(dragonclient$withStarPadding(baseName), entity.getName().getString());
        state.nameLabelPos = new Vec3d(0.0, entity.getHeight() + 0.2, 0.0);
        playerState.playerName = null;
    }

    @Inject(
        method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V", shift = At.Shift.BEFORE),
        require = 0
    )
    private void dragonclient$renderNameTagIcon(
        PlayerEntityRenderState state,
        Text text,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        CallbackInfo ci
    ) {
        if (!dragonclient$shouldRenderOwnNameTag(state) || text == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null || client.player == null) {
            return;
        }

        String tier = TierTagManager.getTierForPlayer(client.player.getName().getString());
        float fullWidth = client.textRenderer.getWidth(text);
        float nameOnlyWidth = client.textRenderer.getWidth(client.player.getName());
        float textLeft = -fullWidth / 2.0f;
        float nameLeft = textLeft + (fullWidth - nameOnlyWidth);
        float reservedWidth = client.textRenderer.getWidth(" ") * DRAGONCLIENT_STAR_PADDING_SPACES;
        float tierEnd = nameLeft - reservedWidth;
        float iconLeft = tierEnd + ((reservedWidth - DRAGONCLIENT_NAME_TAG_ICON_WIDTH) * 0.5f);
        float tierIconLeft = textLeft - DRAGONCLIENT_TIER_ICON_WIDTH - DRAGONCLIENT_TIER_ICON_GAP;
        float iconTop = 1.0f;
        int litLight = LightmapTextureManager.applyEmission(light, 2);
        matrices.push();
        matrices.translate(state.nameLabelPos.x, state.nameLabelPos.y + 0.5, state.nameLabelPos.z);
        matrices.multiply(client.getEntityRenderDispatcher().getRotation());
        matrices.scale(0.025f, -0.025f, 0.025f);
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
        matrices.pop();
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
