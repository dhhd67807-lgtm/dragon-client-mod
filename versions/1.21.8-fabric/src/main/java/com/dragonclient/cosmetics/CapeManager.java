package com.dragonclient.cosmetics;

import net.minecraft.util.Identifier;

public class CapeManager {
    private static CapeManager instance;
    private int equippedCapeIndex = -1;
    private static final Identifier CAPE_1 = Identifier.of("dragonclient", "textures/capes/cape1.png");
    private static final Identifier CAPE_2 = Identifier.of("dragonclient", "textures/capes/cape2.png");
    private static final Identifier CAPE_3 = Identifier.of("dragonclient", "textures/capes/cape3.png");
    private static final Identifier CAPE_4 = Identifier.of("dragonclient", "textures/capes/cape4.png");
    
    public static CapeManager getInstance() {
        if (instance == null) {
            instance = new CapeManager();
        }
        return instance;
    }
    
    public void setEquippedCape(int index) {
        this.equippedCapeIndex = index;
    }
    
    public int getEquippedCapeIndex() {
        return equippedCapeIndex;
    }
    
    public Identifier getCapeTexture() {
        if (equippedCapeIndex == 0) {
            return CAPE_1;
        } else if (equippedCapeIndex == 1) {
            return CAPE_2;
        } else if (equippedCapeIndex == 2) {
            return CAPE_3;
        } else if (equippedCapeIndex == 3) {
            return CAPE_4;
        }
        return null;
    }
    
    public boolean hasCapeEquipped() {
        return equippedCapeIndex >= 0;
    }
}
