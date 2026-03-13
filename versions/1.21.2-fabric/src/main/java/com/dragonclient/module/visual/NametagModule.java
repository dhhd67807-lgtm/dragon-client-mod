package com.dragonclient.module.visual;

import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;

public class NametagModule extends Module {
    public static boolean enabled = true;

    public NametagModule() {
        super("Nametag", "Always show nametag above your player", ModuleCategory.VISUAL);
        setEnabled(true);
    }

    @Override
    protected void onEnable() {
        enabled = true;
    }

    @Override
    protected void onDisable() {
        enabled = false;
    }
}
