package com.dragonclient.gui;

import com.dragonclient.DragonClientMod;
import com.dragonclient.util.CosmeticsDebugLogger;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;
import java.util.UUID;

public class DummyPlayerEntity extends OtherClientPlayerEntity {

    private static final Identifier MANNEQUIN_SKIN = Identifier.of("dragonclient", "textures/mannequin.png");
    private Identifier customCapeTexture = null;
    private Identifier baseSkinTexture = MANNEQUIN_SKIN;
    private SkinTextures.Model baseSkinModel = SkinTextures.Model.WIDE;
    private float yawOffset = 0; // Added rotation offset for back view

    // Store previous positions manually for 1.21.6+
    private double lastX = 0;
    private double lastY = 0;
    private double lastZ = 0;

    // Store cape positions manually for 1.21.6+
    private double capeXPos = 0;
    private double capeYPos = 0;
    private double capeZPos = 0;
    private double prevCapeXPos = 0;
    private double prevCapeYPos = 0;
    private double prevCapeZPos = 0;

    public DummyPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
        // Enable cape rendering
        this.getDataTracker().set(PLAYER_MODEL_PARTS, (byte) 0xFF);

        // Initialize entity state for proper cape physics
        this.setOnGround(true);
        this.setVelocity(Vec3d.ZERO);

        // Store initial position
        this.lastX = this.getX();
        this.lastY = this.getY();
        this.lastZ = this.getZ();

        // Keep hands completely still - 1.21.3+ requires 3 parameters
        this.handSwingProgress = 0;
        this.lastHandSwingProgress = 0;
        this.handSwinging = false;
        this.limbAnimator.setSpeed(0);
        this.limbAnimator.updateLimbs(0, 0, 0);
    }

    public void setCustomCape(Identifier capeTexture) {
        if (!Objects.equals(this.customCapeTexture, capeTexture)) {
            CosmeticsDebugLogger.log(
                    "dummy=" + Integer.toHexString(System.identityHashCode(this))
                            + " setCustomCape old=" + this.customCapeTexture
                            + " new=" + capeTexture);
            DragonClientMod.LOGGER.info(
                    "[1.21.6-cosmetics] dummy={} setCustomCape {}",
                    Integer.toHexString(System.identityHashCode(this)),
                    capeTexture);
        }
        this.customCapeTexture = capeTexture;
    }

    public void setBaseSkin(Identifier skinTexture, SkinTextures.Model model) {
        if (skinTexture != null) {
            this.baseSkinTexture = skinTexture;
        }
        this.baseSkinModel = model != null ? model : SkinTextures.Model.WIDE;
    }

    public void setYawOffset(float offset) {
        this.yawOffset = offset;
    }

    @Override
    public float getBodyYaw() {
        return super.getBodyYaw() + yawOffset;
    }

    @Override
    public float getYaw() {
        return super.getYaw() + yawOffset;
    }

    @Override
    public float getHeadYaw() {
        return super.getHeadYaw() + yawOffset;
    }

    public void updateCapePhysics() {
        // Update cape physics to make it hang naturally
        this.prevCapeXPos = this.capeXPos;
        this.prevCapeYPos = this.capeYPos;
        this.prevCapeZPos = this.capeZPos;

        double deltaX = this.getX() - this.lastX;
        double deltaZ = this.getZ() - this.lastZ;
        double deltaY = this.getY() - this.lastY;

        double motion = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double targetCapeX = deltaX * 10.0;
        double targetCapeZ = deltaZ * 10.0;

        // Make cape hang down when stationary
        this.capeXPos += (targetCapeX - this.capeXPos) * 0.1;
        this.capeYPos += (deltaY * 10.0 - this.capeYPos) * 0.1;
        this.capeZPos += (targetCapeZ - this.capeZPos) * 0.1;

        // Update last position
        this.lastX = this.getX();
        this.lastY = this.getY();
        this.lastZ = this.getZ();

        // Force hands to stay still - 1.21.3+ requires 3 parameters
        this.handSwingProgress = 0;
        this.lastHandSwingProgress = 0;
        this.handSwinging = false;
        this.limbAnimator.setSpeed(0);
        this.limbAnimator.updateLimbs(0, 0, 0);
    }

    /**
     * Returns skin textures specific to this dummy player instance.
     * Called by MixinAbstractClientPlayerEntity to bypass the global CapeManager
     * logic.
     */
    public SkinTextures getDummySkinTextures() {
        if (customCapeTexture != null) {
            return new SkinTextures(
                    baseSkinTexture,
                    null,
                    customCapeTexture,
                    null,
                    baseSkinModel,
                    false);
        }
        return new SkinTextures(
                baseSkinTexture,
                null,
                null,
                null,
                baseSkinModel,
                false);
    }

    @Override
    public SkinTextures getSkinTextures() {
        // Force per-dummy textures to be used even if mixin ordering changes.
        return getDummySkinTextures();
    }

    @Override
    public boolean isPartVisible(PlayerModelPart modelPart) {
        return true; // Show all parts including cape
    }

    @Override
    public void tick() {
        // Update cape physics but keep entity still
        updateCapePhysics();

        // Force limbs to stay still
        this.limbAnimator.setSpeed(0);
    }

    @Override
    public void swingHand(net.minecraft.util.Hand hand) {
        // Do nothing - prevent hand swinging
    }

    public static DummyPlayerEntity create(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            return null;
        }

        GameProfile profile = new GameProfile(UUID.randomUUID(), "Mannequin");
        DummyPlayerEntity dummy = new DummyPlayerEntity(client.world, profile);
        return dummy;
    }
}
