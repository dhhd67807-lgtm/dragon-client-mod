package com.dragonclient.mixin;

import com.dragonclient.DragonClientMod;
import com.dragonclient.module.movement.NoFallModule;
import com.dragonclient.module.movement.SprintModule;
import com.dragonclient.module.player.AutoRespawnModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        DragonClientMod mod = DragonClientMod.getInstance();
        if (mod == null || mod.getModuleManager() == null) {
            return;
        }

        SprintModule sprintModule = (SprintModule) mod.getModuleManager().getModuleByName("Auto Sprint");
        if (sprintModule != null && sprintModule.isEnabled()) {
            sprintModule.tick();
        }

        if (NoFallModule.enabled) {
            ClientPlayerEntity self = (ClientPlayerEntity) (Object) this;
            self.fallDistance = 0.0F;
        }

        if (AutoRespawnModule.enabled) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null
                && client.player != null
                && client.player.isDead()
                && client.currentScreen instanceof DeathScreen) {
                client.player.requestRespawn();
                client.setScreen(null);
            }
        }
    }
}
