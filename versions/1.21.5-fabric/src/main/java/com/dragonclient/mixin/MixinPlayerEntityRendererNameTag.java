package com.dragonclient.mixin;

import com.dragonclient.cosmetics.SkinManager;
import com.dragonclient.module.visual.NametagModule;
import com.dragonclient.module.visual.TierTaggerModule;
import com.dragonclient.util.TierTagManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
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
    private static final float DRAGONCLIENT_TIER_ICON_GAP = 18.0f;
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

    private static String dragonclient$resolveLookupName(AbstractClientPlayerEntity entity) {
        if (entity != null) {
            try {
                if (entity.getGameProfile() != null) {
                    String profileName = entity.getGameProfile().getName();
                    if (profileName != null && !profileName.isBlank()) {
                        return profileName;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return entity != null ? entity.getName().getString() : null;
    }

    private String dragonclient$getTierForDisplay(String playerName, boolean crackedPlayer) {
        if (playerName == null || playerName.isBlank() || !TierTaggerModule.enabled) {
            return null;
        }

        String tier = TierTagManager.getTierForPlayer(playerName, crackedPlayer);
        if (tier != null && !tier.isBlank()) {
            return tier;
        }

        String fallbackTier = TierTagManager.getTierForPlayer(playerName, !crackedPlayer);
        if (fallbackTier != null && !fallbackTier.isBlank()) {
            return fallbackTier;
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
        method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
        at = @At("TAIL"),
        require = 0
    )
    private void dragonclient$forcePlayerLabelState(
        AbstractClientPlayerEntity entity,
        PlayerEntityRenderState state,
        float tickDelta,
        CallbackInfo ci
    ) {
        String playerName = dragonclient$resolveLookupName(entity);
        DRAGONCLIENT_PLAYER_NAMES.put(state.id, playerName);
        DRAGONCLIENT_PLAYER_CRACKED.put(state.id, dragonclient$isLikelyCracked(entity));

        Text baseName = entity.getDisplayName().copy();
        Text decoratedName = TierTagManager.decorateName(baseName.copy(), playerName);
        if (TierTaggerModule.enabled) {
            // Keep both fields populated so vanilla label rendering always
            // has text available across renderer paths in 1.21.5.
            state.displayName = decoratedName.copy();
            state.playerName = decoratedName.copy();
        }

        if (!dragonclient$shouldForceNameTag(entity)) {
            return;
        }

        state.displayName = TierTagManager.decorateName(baseName, playerName);
        state.nameLabelPos = new Vec3d(0.0, entity.getHeight() + 0.2, 0.0);
        state.playerName = null;
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
        String playerName = DRAGONCLIENT_PLAYER_NAMES.get(state.id);
        boolean forceSelf = dragonclient$shouldRenderOwnNameTag(state);
        boolean crackedPlayer = DRAGONCLIENT_PLAYER_CRACKED.getOrDefault(state.id, false);
        if (text == null || (!forceSelf && !dragonclient$shouldShowDecorations(playerName, crackedPlayer))) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) {
            return;
        }

        String tier = dragonclient$getTierForDisplay(playerName, crackedPlayer);
        if ((tier == null || tier.isBlank()) && text != null) {
            tier = TierTagManager.extractTierFromText(text.getString());
        }
        float fullWidth = client.textRenderer.getWidth(text);
        float textLeft = -fullWidth / 2.0f;
        float tierIconLeft = textLeft - DRAGONCLIENT_TIER_ICON_WIDTH - DRAGONCLIENT_TIER_ICON_GAP;
        float starLeft = -DRAGONCLIENT_NAME_TAG_ICON_WIDTH / 2.0f;
        float iconTop = 1.0f;
        float starTop = iconTop - DRAGONCLIENT_NAME_TAG_ICON_HEIGHT - 6.0f;
        int litLight = LightmapTextureManager.applyEmission(light, 2);

        MatrixStack.Entry entry = matrices.peek();
        boolean pushedTransform = false;
        if (!dragonclient$isLabelSpace(entry)) {
            if (state.nameLabelPos == null) {
                return;
            }
            matrices.push();
            matrices.translate(state.nameLabelPos.x, state.nameLabelPos.y + 0.5, state.nameLabelPos.z);
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
            starLeft,
            starTop,
            DRAGONCLIENT_NAME_TAG_ICON_WIDTH,
            DRAGONCLIENT_NAME_TAG_ICON_HEIGHT,
            litLight,
            0xA0FFFFFF
        );

        VertexConsumer normal = vertexConsumers.getBuffer(RenderLayer.getText(DRAGONCLIENT_NAME_TAG_ICON));
        dragonclient$drawIcon(
            entry,
            normal,
            starLeft,
            starTop,
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
        float sx = (float) Math.sqrt(matrix.m00() * matrix.m00() + matrix.m01() * matrix.m01() + matrix.m02() * matrix.m02());
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
