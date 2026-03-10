package com.dragonclient;

import com.dragonclient.gui.hud.HudRenderer;
import com.dragonclient.util.CosmeticsDebugLogger;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class DragonClientClient implements ClientModInitializer {
    
    private static KeyBinding openGuiKey;
    private static KeyBinding hudEditorKey;
    private HudRenderer hudRenderer;

    @Override
    public void onInitializeClient() {
        DragonClientMod.LOGGER.info("Initializing Dragon Client (Client-side)");
        CosmeticsDebugLogger.log("DragonClientClient init");
        CosmeticsDebugLogger.log("Cosmetics debug file: " + CosmeticsDebugLogger.getLogFilePath());
        
        // Initialize texture debug logger
        // com.dragonclient.util.TextureDebugLogger.log("=== Dragon Client 1.21.1 Starting ===");
        // com.dragonclient.util.TextureDebugLogger.log("Log file location: " + com.dragonclient.util.TextureDebugLogger.getLogFilePath());

        // Register keybindings (1.21.1: Constructor signature is (String, InputUtil.Type, int, String))
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.dragonclient.open_gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "key.categories.misc"
        ));

        hudEditorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.dragonclient.hud_editor",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.misc"
        ));

        // Register command as alternative
        // net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback.EVENT.register(
        //     (dispatcher, registryAccess) -> com.dragonclient.command.DragonClientCommand.register(dispatcher, registryAccess)
        // );

        // Initialize HUD renderer (rendering handled by MixinInGameHud)
        hudRenderer = new HudRenderer();

        // Register tick event for keybind checking
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // 1.21.11: Keybindings disabled due to API incompatibility
            if (openGuiKey != null) {
                while (openGuiKey.wasPressed()) {
                    // Open Dragon Menu
                    if (client.currentScreen == null) {
                        client.setScreen(new com.dragonclient.gui.DragonMenuScreen());
                    }
                }
            }

            if (hudEditorKey != null) {
                while (hudEditorKey.wasPressed()) {
                    // Open HUD editor
                    if (client.currentScreen == null) {
                        client.setScreen(new com.dragonclient.gui.HudEditorScreen());
                    }
                }
            }
        });

        DragonClientMod.LOGGER.info("Dragon Client (Client-side) initialized!");
    }

    public static KeyBinding getOpenGuiKey() {
        return openGuiKey;
    }

    public static KeyBinding getHudEditorKey() {
        return hudEditorKey;
    }
}
