package com.dragonclient.gui;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class DummyPlayerEntity extends OtherClientPlayerEntity {

    private static final Identifier MANNEQUIN_SKIN = Identifier.of("dragonclient", "textures/mannequin.png");
    private Identifier customCapeTexture = null;

    public DummyPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
        this.getDataTracker().set(PLAYER_MODEL_PARTS, (byte) 0xFF);

        this.setOnGround(true);
        this.setVelocity(Vec3d.ZERO);
        this.prevX = 0;
        this.prevY = 0;
        this.prevZ = 0;

        this.capeX = 0;
        this.capeY = 0;
        this.capeZ = 0;
        this.prevCapeX = 0;
        this.prevCapeY = 0;
        this.prevCapeZ = 0;

        this.handSwingProgress = 0;
        this.lastHandSwingProgress = 0;
        this.handSwinging = false;
        this.limbAnimator.setSpeed(0);
        this.limbAnimator.updateLimbs(0, 0, 0);
    }

    public void setCustomCape(Identifier capeTexture) {
        this.customCapeTexture = capeTexture;
    }

    public void updateCapePhysics() {
        this.prevCapeX = this.capeX;
        this.prevCapeY = this.capeY;
        this.prevCapeZ = this.capeZ;

        double deltaX = this.getX() - this.prevX;
        double deltaZ = this.getZ() - this.prevZ;
        double deltaY = this.getY() - this.prevY;

        double targetCapeX = deltaX * 10.0;
        double targetCapeZ = deltaZ * 10.0;

        this.capeX += (targetCapeX - this.capeX) * 0.1;
        this.capeY += (deltaY * 10.0 - this.capeY) * 0.1;
        this.capeZ += (targetCapeZ - this.capeZ) * 0.1;

        this.handSwingProgress = 0;
        this.lastHandSwingProgress = 0;
        this.handSwinging = false;
        this.limbAnimator.setSpeed(0);
        this.limbAnimator.updateLimbs(0, 0, 0);
    }

    @Override
    public SkinTextures getSkinTextures() {
        if (customCapeTexture != null) {
            return new SkinTextures(
                MANNEQUIN_SKIN,
                null,
                customCapeTexture,
                null,
                SkinTextures.Model.WIDE,
                false
            );
        }
        return new SkinTextures(
            MANNEQUIN_SKIN,
            null,
            null,
            null,
            SkinTextures.Model.WIDE,
            false
        );
    }

    @Override
    public boolean isPartVisible(PlayerModelPart modelPart) {
        return true;
    }

    @Override
    public void tick() {
        updateCapePhysics();
        this.limbAnimator.setSpeed(0);
    }

    @Override
    public void swingHand(net.minecraft.util.Hand hand) {
    }

    public static DummyPlayerEntity create(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            return null;
        }

        GameProfile profile = new GameProfile(UUID.randomUUID(), "Mannequin");
        return new DummyPlayerEntity(client.world, profile);
    }
}
