package com.dragonclient.config;

import com.dragonclient.module.Module;
import com.dragonclient.module.hud.HudModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ModuleConfig {
    private static final int HUD_LAYOUT_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = Paths.get(System.getProperty("user.home"), ".dragonclient");
    private static final File CONFIG_FILE = CONFIG_DIR.resolve("modules.json").toFile();

    public static void saveModules(List<Module> modules) {
        try {
            Files.createDirectories(CONFIG_DIR);
            
            JsonObject root = new JsonObject();
            JsonObject modulesObj = new JsonObject();
            
            for (Module module : modules) {
                JsonObject moduleData = new JsonObject();
                moduleData.addProperty("enabled", module.isEnabled());
                
                if (module instanceof HudModule) {
                    HudModule hud = (HudModule) module;
                    moduleData.addProperty("hudLayoutVersion", HUD_LAYOUT_VERSION);
                    moduleData.addProperty("customScale", hud.hasCustomScale());
                    if (hud.hasCustomScale()) {
                        moduleData.addProperty("scale", hud.getScale());
                    }
                    moduleData.addProperty("customPosition", hud.hasCustomPosition());
                    if (hud.hasCustomPosition()) {
                        moduleData.addProperty("x", hud.getX());
                        moduleData.addProperty("y", hud.getY());
                    }
                }
                
                modulesObj.add(module.getName(), moduleData);
            }
            
            root.add("modules", modulesObj);
            
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            System.err.println("[DragonClient] Failed to save config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void loadModules(List<Module> modules) {
        if (!CONFIG_FILE.exists()) {
            return;
        }
        
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject modulesObj = root.getAsJsonObject("modules");
            if (modulesObj == null) {
                return;
            }
            
            for (Module module : modules) {
                if (!modulesObj.has(module.getName())) {
                    continue;
                }

                try {
                    JsonObject moduleData = modulesObj.getAsJsonObject(module.getName());
                    if (moduleData == null || !moduleData.has("enabled")) {
                        continue;
                    }

                    boolean enabled = moduleData.get("enabled").getAsBoolean();
                    if (enabled) {
                        module.enable();
                    } else {
                        module.disable();
                    }

                    if (module instanceof HudModule) {
                        HudModule hud = (HudModule) module;
                        int layoutVersion = moduleData.has("hudLayoutVersion")
                            ? moduleData.get("hudLayoutVersion").getAsInt()
                            : 0;
                        boolean customScale;
                        if (moduleData.has("customScale")) {
                            customScale = moduleData.get("customScale").getAsBoolean();
                        } else {
                            // Migration: old configs without this field should use the new default scale.
                            customScale = false;
                        }
                        hud.setUseDefaultScale(!customScale);
                        if (customScale && moduleData.has("scale")) {
                            hud.setScale(moduleData.get("scale").getAsFloat());
                        }

                        boolean customPosition =
                            moduleData.has("customPosition") && moduleData.get("customPosition").getAsBoolean();
                        if (layoutVersion < HUD_LAYOUT_VERSION) {
                            // Reset legacy HUD placement to the current organized default layout.
                            customPosition = false;
                        }
                        hud.setUseDefaultPosition(!customPosition);

                        if (customPosition && moduleData.has("x") && moduleData.has("y")) {
                            hud.setX(moduleData.get("x").getAsInt());
                            hud.setY(moduleData.get("y").getAsInt());
                        }
                    }
                } catch (Exception moduleError) {
                    System.err.println("[DragonClient] Failed to load module state for '" + module.getName() + "': " + moduleError.getMessage());
                    moduleError.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("[DragonClient] Failed to load config: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
