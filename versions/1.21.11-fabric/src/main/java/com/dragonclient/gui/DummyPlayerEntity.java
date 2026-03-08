package com.dragonclient.gui;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
// import net.minecraft.client.util.SkinTextures; // Unmapped in 1.21.10
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class DummyPlayerEntity extends OtherClientPlayerEntity {
    
    private static final Identifier MANNEQUIN_SKIN = Identifier.of("dragonclient", "textures/mannequin.png");
    private Identifier customCapeTexture = null;
    
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
        // this.getDataTracker().set(PLAYER_MODEL_PARTS, (byte) 0xFF); // Disabled for 1.21.10
        
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
        this.customCapeTexture = capeTexture;
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
    
    // SkinTextures unmapped in 1.21.10 - cape functionality disabled
    /*
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
    */
    
    // @Override // Removed for 1.21.10 compatibility
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
