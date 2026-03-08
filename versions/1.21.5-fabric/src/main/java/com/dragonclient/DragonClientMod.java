package com.dragonclient;

import com.dragonclient.core.ModuleManager;
import com.dragonclient.core.EventBus;
import com.dragonclient.core.ConfigManager;
import com.dragonclient.core.CommandManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DragonClientMod implements ModInitializer {
    public static final String MOD_ID = "dragonclient";
    public static final String MOD_NAME = "Dragon Client";
    public static final String VERSION = "1.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private static DragonClientMod instance;
    private ModuleManager moduleManager;
    private EventBus eventBus;
    private ConfigManager configManager;
    private CommandManager commandManager;

    @Override
    public void onInitialize() {
        instance = this;
        LOGGER.info("Initializing {} v{}", MOD_NAME, VERSION);

        // Initialize core systems
        eventBus = new EventBus();
        configManager = new ConfigManager();
        moduleManager = new ModuleManager();
        commandManager = new CommandManager();

        // Load configuration
        configManager.loadConfig();

        LOGGER.info("{} initialized successfully!", MOD_NAME);
    }

    public static DragonClientMod getInstance() {
        return instance;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}
