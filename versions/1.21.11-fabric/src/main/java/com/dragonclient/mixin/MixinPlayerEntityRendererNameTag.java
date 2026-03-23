package com.dragonclient.mixin;

import com.dragonclient.cosmetics.SkinManager;
import com.dragonclient.module.visual.NametagModule;
import com.dragonclient.module.visual.TierTaggerModule;
import com.dragonclient.util.TierTagManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayers;
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

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRendererNameTag {
    private static final Identifier DRAGONCLIENT_NAME_TAG_ICON =
        Identifier.of("dragonclient", "textures/gui/cs_star_8.png");
    private static final float DRAGONCLIENT_NAME_TAG_ICON_WIDTH = 6.0f;
    private static final float DRAGONCLIENT_NAME_TAG_ICON_HEIGHT = 6.0f;
    private static final float DRAGONCLIENT_TIER_ICON_WIDTH = 6.0f;
    private static final float DRAGONCLIENT_TIER_ICON_HEIGHT = 6.0f;
    private static final float DRAGONCLIENT_TIER_ICON_GAP = 0.5f;
    private static final Map<Integer, String> DRAGONCLIENT_PLAYER_NAMES = new ConcurrentHashMap<>();
    private static final Map<Integer, Boolean> DRAGONCLIENT_PLAYER_CRACKED = new ConcurrentHashMap<>();

    private boolean dragonclient$shouldForceNameTag(Entity entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        return NametagModule.enabled && entity instanceof PlayerEntity && client != null && entity == client.player;
    }

    private boolean dragonclient$shouldRenderOwnNameTag(PlayerEntityRenderState state) {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null && state.id == client.player.getId();
    }

    private static boolean dragonclient$isLikelyCracked(PlayerEntity player) {
        if (player == null) {
            return false;
        }
        try {
            return player.getUuid().version() == 3;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static String dragonclient$resolveLookupName(Entity entity) {
        if (entity instanceof PlayerEntity playerEntity) {
            try {
                Object gameProfile = playerEntity.getGameProfile();
                if (gameProfile != null) {
                    String profileName = dragonclient$extractProfileName(gameProfile);
                    if (profileName != null && !profileName.isBlank()) {
                        return profileName;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return entity != null ? entity.getName().getString() : null;
    }

    private static String dragonclient$extractProfileName(Object gameProfile) {
        if (gameProfile == null) {
            return null;
        }

        try {
            Object value = gameProfile.getClass().getMethod("getName").invoke(gameProfile);
            if (value instanceof String name && !name.isBlank()) {
                return name;
            }
        } catch (Exception ignored) {
        }

        try {
            Object value = gameProfile.getClass().getMethod("name").invoke(gameProfile);
            if (value instanceof String name && !name.isBlank()) {
                return name;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private String dragonclient$getTierForDisplay(String playerName, boolean crackedPlayer) {
        if (playerName == null || playerName.isBlank() || !TierTaggerModule.enabled) {
            return null;
        }

        if (!crackedPlayer) {
            String crackedTier = TierTagManager.getTierForPlayer(playerName, true);
            if (crackedTier != null && !crackedTier.isBlank()) {
                return crackedTier;
            }
        }

        String tier = TierTagManager.getTierForPlayer(playerName, crackedPlayer);
        if (tier != null && !tier.isBlank()) {
            return tier;
        }

        return null;
    }

    private boolean dragonclient$shouldShowDecorations(String playerName, boolean crackedPlayer) {
        if (playerName == null || playerName.isBlank()) {
            return false;
        }

        if (dragonclient$getTierForDisplay(playerName, crackedPlayer) != null) {
            return true;
        }

        SkinManager skinManager = SkinManager.getInstance();
        return skinManager.hasCustomSkin(playerName) || skinManager.hasCustomCape(playerName);
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
        method = "updateRenderState(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/entity/state/EntityRenderState;F)V",
        at = @At("TAIL"),
        require = 0
    )
    private void dragonclient$forcePlayerLabelState(Entity entity, EntityRenderState state, float tickDelta, CallbackInfo ci) {
        if (!(state instanceof PlayerEntityRenderState playerState)) {
            return;
        }

        String playerName = dragonclient$resolveLookupName(entity);
        DRAGONCLIENT_PLAYER_NAMES.put(playerState.id, playerName);
        if (entity instanceof PlayerEntity playerEntity) {
            DRAGONCLIENT_PLAYER_CRACKED.put(playerState.id, dragonclient$isLikelyCracked(playerEntity));
        } else {
            DRAGONCLIENT_PLAYER_CRACKED.put(playerState.id, false);
        }

        Text baseName = entity.getDisplayName().copy();
        Text decoratedName = TierTagManager.decorateName(baseName.copy(), playerName);

        if (TierTaggerModule.enabled) {
            state.displayName = null;
            playerState.playerName = decoratedName;
        }

        if (!dragonclient$shouldForceNameTag(entity)) {
            return;
        }

        state.displayName = TierTagManager.decorateName(baseName, playerName);
        state.nameLabelPos = new Vec3d(0.0, entity.getHeight() + 0.2, 0.0);
        playerState.playerName = null;
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
        String playerName = DRAGONCLIENT_PLAYER_NAMES.get(state.id);
        boolean forceSelf = dragonclient$shouldRenderOwnNameTag(state);
        boolean crackedPlayer = DRAGONCLIENT_PLAYER_CRACKED.getOrDefault(state.id, false);
        if (!forceSelf && !dragonclient$shouldShowDecorations(playerName, crackedPlayer)) {
            return;
        }

        Text labelText = state.playerName != null
            ? state.playerName
            : (playerName != null && !playerName.isBlank() ? Text.literal(playerName) : null);
        if (labelText == null || state.nameLabelPos == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) {
            return;
        }

        String tier = dragonclient$getTierForDisplay(playerName, crackedPlayer);
        if ((tier == null || tier.isBlank()) && labelText != null) {
            tier = TierTagManager.extractTierFromText(labelText.getString());
        }
        if ((tier == null || tier.isBlank()) && state.displayName != null) {
            tier = TierTagManager.extractTierFromText(state.displayName.getString());
        }
        float fullWidth = client.textRenderer.getWidth(labelText);
        float textLeft = -fullWidth / 2.0f;
        float tierIconLeft = textLeft - DRAGONCLIENT_TIER_ICON_WIDTH - DRAGONCLIENT_TIER_ICON_GAP;
        float starLeft = -DRAGONCLIENT_NAME_TAG_ICON_WIDTH / 2.0f;
        float iconTop = state.extraEars ? -10.0f : 1.0f;
        float starTop = iconTop - DRAGONCLIENT_NAME_TAG_ICON_HEIGHT - 1.0f;
        int litLight = LightmapTextureManager.applyEmission(state.light, 2);

        matrices.push();
        matrices.translate(state.nameLabelPos.x, state.nameLabelPos.y + 0.5, state.nameLabelPos.z);
        matrices.multiply(cameraState.orientation);
        matrices.scale(0.025f, -0.025f, 0.025f);

        if (tier != null && !tier.isBlank()) {
            Identifier tierIcon = Identifier.of("dragonclient", "textures/tier_tags/" + tier.toLowerCase(Locale.ROOT) + ".png");
            queue.submitCustom(
                matrices,
                RenderLayers.textSeeThrough(tierIcon),
                (entry, vertexConsumer) -> dragonclient$drawIcon(
                    entry,
                    vertexConsumer,
                    tierIconLeft,
                    iconTop,
                    DRAGONCLIENT_TIER_ICON_WIDTH,
                    DRAGONCLIENT_TIER_ICON_HEIGHT,
                    litLight,
                    0xA0FFFFFF
                )
            );
            queue.submitCustom(
                matrices,
                RenderLayers.text(tierIcon),
                (entry, vertexConsumer) -> dragonclient$drawIcon(
                    entry,
                    vertexConsumer,
                    tierIconLeft,
                    iconTop,
                    DRAGONCLIENT_TIER_ICON_WIDTH,
                    DRAGONCLIENT_TIER_ICON_HEIGHT,
                    litLight,
                    0xFFFFFFFF
                )
            );
        }

        queue.submitCustom(
            matrices,
            RenderLayers.textSeeThrough(DRAGONCLIENT_NAME_TAG_ICON),
            (entry, vertexConsumer) -> dragonclient$drawIcon(
                entry,
                vertexConsumer,
                starLeft,
                starTop,
                DRAGONCLIENT_NAME_TAG_ICON_WIDTH,
                DRAGONCLIENT_NAME_TAG_ICON_HEIGHT,
                litLight,
                0xA0FFFFFF
            )
        );

        queue.submitCustom(
            matrices,
            RenderLayers.text(DRAGONCLIENT_NAME_TAG_ICON),
            (entry, vertexConsumer) -> dragonclient$drawIcon(
                entry,
                vertexConsumer,
                starLeft,
                starTop,
                DRAGONCLIENT_NAME_TAG_ICON_WIDTH,
                DRAGONCLIENT_NAME_TAG_ICON_HEIGHT,
                litLight,
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
