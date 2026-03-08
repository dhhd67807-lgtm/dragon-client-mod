package com.dragonclient.mixin;

import com.dragonclient.DragonClientMod;
import com.dragonclient.module.movement.SprintModule;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        SprintModule sprintModule = (SprintModule) DragonClientMod.getInstance()
            .getModuleManager()
            .getModuleByName("Auto Sprint");
        
        if (sprintModule != null && sprintModule.isEnabled()) {
            sprintModule.tick();
        }
    }
}
