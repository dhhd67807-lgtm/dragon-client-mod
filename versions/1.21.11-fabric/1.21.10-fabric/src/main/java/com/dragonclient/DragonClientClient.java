package com.dragonclient;

import com.dragonclient.gui.hud.HudRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class DragonClientClient implements ClientModInitializer {
    
    private static KeyBinding openGuiKey;
    private static KeyBinding hudEditorKey;
    private HudRenderer hudRenderer;

    @Override
    public void onInitializeClient() {
        DragonClientMod.LOGGER.info("Initializing Dragon Client (Client-side)");

        // Register keybindings - 1.21.10: Use same approach as 1.21.9
        try {
            // Try to get the Category class (unmapped in 1.21.10)
            Class<?> categoryClass = null;
            Object category = null;
            
            try {
                categoryClass = Class.forName("net.minecraft.class_304$class_11900");
                java.lang.reflect.Method ofMethod = categoryClass.getMethod("method_74698", net.minecraft.util.Identifier.class);
                category = ofMethod.invoke(null, Identifier.of("dragonclient", "general"));
                DragonClientMod.LOGGER.info("Found Category class via intermediary mappings");
            } catch (Exception e) {
                DragonClientMod.LOGGER.info("Category class not found: " + e.getMessage());
            }
            
            if (categoryClass != null && category != null) {
                // Use (String, int, Category) constructor like 1.21.9
                java.lang.reflect.Constructor<?> constructor = KeyBinding.class.getConstructor(
                    String.class, int.class, categoryClass
                );
                
                openGuiKey = KeyBindingHelper.registerKeyBinding((KeyBinding) constructor.newInstance(
                    "key.dragonclient.open_gui",
                    GLFW.GLFW_KEY_RIGHT_SHIFT,
                    category
                ));

                hudEditorKey = KeyBindingHelper.registerKeyBinding((KeyBinding) constructor.newInstance(
                    "key.dragonclient.hud_editor",
                    GLFW.GLFW_KEY_H,
                    category
                ));
                
                DragonClientMod.LOGGER.info("Keybindings registered successfully");
            } else {
                DragonClientMod.LOGGER.error("Failed to find Category class - keybindings will not work");
            }
        } catch (Exception e) {
            DragonClientMod.LOGGER.error("Failed to register keybindings", e);
            throw new RuntimeException(e);
        }

        // Initialize HUD renderer
        hudRenderer = new HudRenderer();

        // Register HUD rendering
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            // 1.21.6+: Use getTickProgress(boolean ignoreFreeze) for tick delta
            float tickDelta = tickCounter.getTickProgress(false);
            hudRenderer.render(drawContext, tickDelta);
        });

        // Register tick event for keybind checking
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                // Open Dragon Menu
                if (client.currentScreen == null) {
                    client.setScreen(new com.dragonclient.gui.DragonMenuScreen());
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
