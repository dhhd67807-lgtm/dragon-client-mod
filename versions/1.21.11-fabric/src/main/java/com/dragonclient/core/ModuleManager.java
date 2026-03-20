package com.dragonclient.core;

import com.dragonclient.config.ModuleConfig;
import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;
import com.dragonclient.module.hud.*;
import com.dragonclient.module.visual.*;
import com.dragonclient.module.movement.*;
import com.dragonclient.module.player.*;
import com.dragonclient.module.misc.*;

import java.util.*;
import java.util.stream.Collectors;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    private final Map<ModuleCategory, List<Module>> modulesByCategory = new HashMap<>();

    public ModuleManager() {
        // Register HUD modules (disabled by default, will be loaded from config)
        registerModule(new FpsCounterHud());
        registerModule(new CpsCounterHud());
        registerModule(new PingHud());
        registerModule(new CoordinatesHud());
        registerModule(new DirectionHud());
        registerModule(new ArmorStatusHud());
        registerModule(new PotionEffectsHud());
        registerModule(new KeystrokesHud());
        registerModule(new SpeedHud());
        registerModule(new HealthHud());
        registerModule(new HungerHud());
        registerModule(new ClockHud());
        registerModule(new ReachDisplayHud());

        // Register Visual modules
        registerModule(new ZoomModule());
        registerModule(new MotionBlurModule());
        registerModule(new CustomCrosshairModule());
        registerModule(new HitColorModule());
        registerModule(new FullbrightModule());
        registerModule(new WeatherChangerModule());
        registerModule(new TimeChangerModule());
        registerModule(new ClearWaterModule());
        registerModule(new OutlinesModule());
        registerModule(new ItemPhysicsModule());
        registerModule(new NametagModule());
        registerModule(new TierTaggerModule());

        // Register Movement modules
        registerModule(new FreelookModule());
        registerModule(new NoFallModule());
        registerModule(new SprintModule());

        // Register Player modules
        registerModule(new AutoRespawnModule());
        registerModule(new ArrowCountModule());

        // Register Misc modules
        registerModule(new ChatTimestampsModule());
        registerModule(new ChatSearchModule());
        registerModule(new FPSLimiterModule());
        registerModule(new PacketLoggerModule());
        registerModule(new TablistModule());
        
        // Load saved module states from config
        ModuleConfig.loadModules(modules);
    }

    private void registerModule(Module module) {
        modules.add(module);
        modulesByCategory.computeIfAbsent(module.getCategory(), k -> new ArrayList<>()).add(module);
    }
    
    public void saveConfig() {
        ModuleConfig.saveModules(modules);
    }

    public List<Module> getModules() {
        return new ArrayList<>(modules);
    }

    public List<Module> getModulesByCategory(ModuleCategory category) {
        return new ArrayList<>(modulesByCategory.getOrDefault(category, Collections.emptyList()));
    }

    public Module getModuleByName(String name) {
        return modules.stream()
            .filter(m -> m.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }

    public List<Module> getEnabledModules() {
        return modules.stream()
            .filter(Module::isEnabled)
            .collect(Collectors.toList());
    }

    public void toggleModule(String name) {
        Module module = getModuleByName(name);
        if (module != null) {
            module.toggle();
        }
    }

    public void enableModule(String name) {
        Module module = getModuleByName(name);
        if (module != null && !module.isEnabled()) {
            module.enable();
        }
    }

    public void disableModule(String name) {
        Module module = getModuleByName(name);
        if (module != null && module.isEnabled()) {
            module.disable();
        }
    }
}
