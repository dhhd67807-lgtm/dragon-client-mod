package com.dragonclient.module.visual;

import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;

public class TierTaggerModule extends Module {
    public static boolean enabled = true;

    public TierTaggerModule() {
        super("TierTagger", "Shows player tier tags next to nametags", ModuleCategory.VISUAL);
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
