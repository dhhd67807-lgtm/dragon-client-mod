package com.dragonclient.gui.hud;

import com.dragonclient.DragonClientMod;
import com.dragonclient.module.Module;
import com.dragonclient.module.hud.HudModule;
import net.minecraft.client.gui.DrawContext;

public class HudRenderer {
    
    public void render(DrawContext context, float tickDelta) {
        for (Module module : DragonClientMod.getInstance().getModuleManager().getEnabledModules()) {
            if (module instanceof HudModule) {
                ((HudModule) module).render(context, tickDelta);
            }
        }
    }
}
