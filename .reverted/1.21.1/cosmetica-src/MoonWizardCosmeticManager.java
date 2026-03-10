package com.dragonclient.cosmetica;

import cc.cosmetica.core.api.CosmeticManager;
import cc.cosmetica.core.api.Cosmetics;
import cc.cosmetica.core.api.NoneCosmetics;
import net.minecraft.client.MinecraftClient;

final class MoonWizardCosmeticManager implements CosmeticManager {
    @Override
    public boolean canManage(Either entity) {
        if (!DragonCosmeticaIntegration.isMoonWizardEquipped()) {
            return false;
        }

        if (entity.entity == null || entity.remotePlayerInfo != null) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null && entity.entity == client.player;
    }

    @Override
    public Cosmetics getCosmetics(Either entity) {
        var moonWizardCosmetics = DragonCosmeticaIntegration.getMoonWizardCosmetics();
        return moonWizardCosmetics != null ? moonWizardCosmetics : NoneCosmetics.NONE;
    }
}
