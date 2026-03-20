package com.dragonclient.gui;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class DummyPlayerEntity extends OtherClientPlayerEntity {

    private static final Identifier MANNEQUIN_SKIN = Identifier.of("dragonclient", "textures/mannequin.png");
    private Identifier customCapeTexture = null;

    private double lastX = 0;
    private double lastY = 0;
    private double lastZ = 0;

    private double capeXPos = 0;
    private double capeYPos = 0;
    private double capeZPos = 0;
    private double prevCapeXPos = 0;
    private double prevCapeYPos = 0;
    private double prevCapeZPos = 0;

    public DummyPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);

        this.setOnGround(true);
        this.setVelocity(Vec3d.ZERO);

        this.lastX = this.getX();
        this.lastY = this.getY();
        this.lastZ = this.getZ();

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
        this.prevCapeXPos = this.capeXPos;
        this.prevCapeYPos = this.capeYPos;
        this.prevCapeZPos = this.capeZPos;

        double deltaX = this.getX() - this.lastX;
        double deltaZ = this.getZ() - this.lastZ;
        double deltaY = this.getY() - this.lastY;

        double targetCapeX = deltaX * 10.0;
        double targetCapeZ = deltaZ * 10.0;

        this.capeXPos += (targetCapeX - this.capeXPos) * 0.1;
        this.capeYPos += (deltaY * 10.0 - this.capeYPos) * 0.1;
        this.capeZPos += (targetCapeZ - this.capeZPos) * 0.1;

        this.lastX = this.getX();
        this.lastY = this.getY();
        this.lastZ = this.getZ();

        this.handSwingProgress = 0;
        this.lastHandSwingProgress = 0;
        this.handSwinging = false;
        this.limbAnimator.setSpeed(0);
        this.limbAnimator.updateLimbs(0, 0, 0);
    }

    @Override
    public SkinTextures getSkin() {
        AssetInfo.TextureAsset body = new AssetInfo.TextureAssetInfo(MANNEQUIN_SKIN, MANNEQUIN_SKIN);
        AssetInfo.TextureAsset cape = customCapeTexture != null
            ? new AssetInfo.TextureAssetInfo(customCapeTexture, customCapeTexture)
            : null;
        AssetInfo.TextureAsset elytra = customCapeTexture != null
            ? new AssetInfo.TextureAssetInfo(customCapeTexture, customCapeTexture)
            : null;
        return new SkinTextures(body, cape, elytra, PlayerSkinType.WIDE, true);
    }

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
