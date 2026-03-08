package com.dragonclient.module.visual;

import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;

public class WeatherChangerModule extends Module {
    public static boolean clearWeather = false;

    public WeatherChangerModule() {
        super("Weather Changer", "Change weather client-side", ModuleCategory.VISUAL);
    }

    @Override
    protected void onEnable() {
        clearWeather = true;
    }

    @Override
    protected void onDisable() {
        clearWeather = false;
    }
}
