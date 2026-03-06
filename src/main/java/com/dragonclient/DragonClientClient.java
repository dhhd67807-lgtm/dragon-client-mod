package com.dragonclient;

import com.dragonclient.gui.hud.HudRenderer;
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

        // Register keybindings
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.dragonclient.open_gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "category.dragonclient"
        ));

        hudEditorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.dragonclient.hud_editor",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "category.dragonclient"
        ));

        // Initialize HUD renderer
        hudRenderer = new HudRenderer();

        // Register HUD rendering
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            float tickDelta = tickCounter.getTickDelta(false);
            hudRenderer.render(drawContext, tickDelta);
        });

        // Register tick event for keybind checking
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                // Open settings GUI
                if (client.currentScreen == null) {
                    // client.setScreen(new SettingsScreen());
                }
            }

            while (hudEditorKey.wasPressed()) {
                // Open HUD editor
                if (client.currentScreen == null) {
                    // client.setScreen(new HudEditorScreen());
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
