package com.dragonclient.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
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
    private static final StyleSpriteSource DRAGONCLIENT_ICON_FONT = new StyleSpriteSource.Font(
        Identifier.of("dragonclient", "cs_star")
    );
    private static final Text DRAGONCLIENT_STAR_PREFIX = Text.literal("\ue000")
        .setStyle(Style.EMPTY.withFont(DRAGONCLIENT_ICON_FONT));

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

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/entity/state/EntityRenderState;F)V",
            at = @At("TAIL"), require = 0)
    private void dragonclient$forcePlayerLabelState(Entity entity, EntityRenderState state, float tickDelta, CallbackInfo ci) {
        if (!dragonclient$shouldForceNameTag(entity)) {
            return;
        }

        // Keep one clean nametag line, with a CS-style star prefix, a little lower above the head.
        state.displayName = null;
        state.nameLabelPos = new Vec3d(0.0, entity.getHeight() + 0.2, 0.0);

        if (state instanceof PlayerEntityRenderState playerState) {
            Text displayName = entity.getDisplayName();
            playerState.playerName = Text.empty()
                .append(DRAGONCLIENT_STAR_PREFIX.copy())
                .append(Text.literal(" "))
                .append(displayName.copy().fillStyle(displayName.getStyle().withFont(StyleSpriteSource.DEFAULT)));
        }
    }
}
