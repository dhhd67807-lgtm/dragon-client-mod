package com.dragonclient.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
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

    private boolean dragonclient$shouldRenderOwnNameTag(PlayerEntityRenderState state) {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null && state.id == client.player.getId();
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

    @Inject(method = "hasLabel(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$hasLabelPlayerState(PlayerEntityRenderState state, CallbackInfoReturnable<Boolean> cir) {
        if (dragonclient$shouldRenderOwnNameTag(state)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hasLabel(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;D)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$hasLabelPlayerStateDistance(PlayerEntityRenderState state, double distance, CallbackInfoReturnable<Boolean> cir) {
        if (dragonclient$shouldRenderOwnNameTag(state)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hasLabel(Lnet/minecraft/client/render/entity/state/EntityRenderState;)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$hasLabelEntityState(EntityRenderState state, CallbackInfoReturnable<Boolean> cir) {
        if (state instanceof PlayerEntityRenderState playerState && dragonclient$shouldRenderOwnNameTag(playerState)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hasLabel(Lnet/minecraft/client/render/entity/state/EntityRenderState;D)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonclient$hasLabelEntityStateDistance(EntityRenderState state, double distance, CallbackInfoReturnable<Boolean> cir) {
        if (state instanceof PlayerEntityRenderState playerState && dragonclient$shouldRenderOwnNameTag(playerState)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
        method = "updateRenderState(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/entity/state/EntityRenderState;F)V",
        at = @At("TAIL"),
        require = 0
    )
    private void dragonclient$forcePlayerLabelState(Entity entity, EntityRenderState state, float tickDelta, CallbackInfo ci) {
        if (!(state instanceof PlayerEntityRenderState playerState) || !dragonclient$shouldForceNameTag(entity)) {
            return;
        }

        state.displayName = null;
        state.nameLabelPos = new Vec3d(0.0, entity.getHeight() + 0.2, 0.0);
        playerState.playerName = entity.getDisplayName().copy();
    }

    @Inject(
        method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
        at = @At("TAIL"),
        require = 0
    )
    private void dragonclient$renderNameTagIcon(
        PlayerEntityRenderState state,
        MatrixStack matrices,
        OrderedRenderCommandQueue queue,
        CameraRenderState cameraState,
        CallbackInfo ci
    ) {
        if (!dragonclient$shouldRenderOwnNameTag(state) || state.playerName == null || state.nameLabelPos == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) {
            return;
        }

        float nameWidth = client.textRenderer.getWidth(state.playerName);
        float textLeft = -nameWidth / 2.0f;
        float iconLeft = textLeft - DRAGONCLIENT_NAME_TAG_ICON_GAP - DRAGONCLIENT_NAME_TAG_ICON_WIDTH;
        float iconTop = 1.0f;

        matrices.push();
        matrices.translate(state.nameLabelPos.x, state.nameLabelPos.y + 0.5, state.nameLabelPos.z);
        matrices.multiply(cameraState.orientation);
        matrices.scale(0.025f, -0.025f, 0.025f);

        queue.submitCustom(
            matrices,
            RenderLayer.getTextSeeThrough(DRAGONCLIENT_NAME_TAG_ICON),
            (entry, vertexConsumer) -> dragonclient$drawIcon(
                entry,
                vertexConsumer,
                iconLeft,
                iconTop,
                DRAGONCLIENT_NAME_TAG_ICON_WIDTH,
                DRAGONCLIENT_NAME_TAG_ICON_HEIGHT,
                state.light,
                0xA0FFFFFF
            )
        );
        queue.submitCustom(
            matrices,
            RenderLayer.getText(DRAGONCLIENT_NAME_TAG_ICON),
            (entry, vertexConsumer) -> dragonclient$drawIcon(
                entry,
                vertexConsumer,
                iconLeft,
                iconTop,
                DRAGONCLIENT_NAME_TAG_ICON_WIDTH,
                DRAGONCLIENT_NAME_TAG_ICON_HEIGHT,
                state.light,
                0xFFFFFFFF
            )
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
