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
                    moduleData.addProperty("x", hud.getX());
                    moduleData.addProperty("y", hud.getY());
                    moduleData.addProperty("scale", hud.getScale());
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
            
            for (Module module : modules) {
                if (modulesObj.has(module.getName())) {
                    JsonObject moduleData = modulesObj.getAsJsonObject(module.getName());
                    
                    boolean enabled = moduleData.get("enabled").getAsBoolean();
                    if (enabled) {
                        module.enable();
                    } else {
                        module.disable();
                    }
                    
                    if (module instanceof HudModule && moduleData.has("x")) {
                        HudModule hud = (HudModule) module;
                        hud.setX(moduleData.get("x").getAsInt());
                        hud.setY(moduleData.get("y").getAsInt());
                        hud.setScale(moduleData.get("scale").getAsFloat());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[DragonClient] Failed to load config: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
