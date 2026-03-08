package com.dragonclient.core;

import com.dragonclient.DragonClientMod;
import com.dragonclient.config.ClientConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File configDir;
    private final File configFile;
    private ClientConfig config;

    public ConfigManager() {
        this.configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), "dragonclient");
        this.configFile = new File(configDir, "config.json");
        
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
    }

    public void loadConfig() {
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                config = GSON.fromJson(reader, ClientConfig.class);
                DragonClientMod.LOGGER.info("Configuration loaded successfully");
            } catch (IOException e) {
                DragonClientMod.LOGGER.error("Failed to load configuration", e);
                config = new ClientConfig();
            }
        } else {
            config = new ClientConfig();
            saveConfig();
        }
    }

    public void saveConfig() {
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(config, writer);
            DragonClientMod.LOGGER.info("Configuration saved successfully");
        } catch (IOException e) {
            DragonClientMod.LOGGER.error("Failed to save configuration", e);
        }
    }

    public ClientConfig getConfig() {
        return config;
    }
}
