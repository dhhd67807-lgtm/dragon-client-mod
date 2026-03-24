package com.dragonclient;

import com.dragonclient.cosmetics.CapeManager;
import com.dragonclient.cosmetics.GearSkinManager;
import com.dragonclient.gui.hud.HudRenderer;
import com.dragonclient.module.movement.FreelookModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class DragonClientClient implements ClientModInitializer {

    private static KeyBinding openGuiKey;
    private static KeyBinding hudEditorKey;
    private static KeyBinding zoomKey;
    private HudRenderer hudRenderer;

    @Override
    public void onInitializeClient() {
        DragonClientMod.LOGGER.info("Initializing Dragon Client (Client-side)");

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

        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.dragonclient.zoom",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.categories.misc"
        ));

        hudRenderer = new HudRenderer();
        GearSkinManager.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (FreelookModule.isFreelooking) {
                if (client == null || client.player == null || client.currentScreen != null) {
                    FreelookModule.stopFreelook(client);
                } else {
                    FreelookModule.enforceLockedRotation(client);
                }
            }
            CapeManager.getInstance().tick();

            if (openGuiKey != null) {
                while (openGuiKey.wasPressed()) {
                    if (client.currentScreen == null) {
                        client.setScreen(new com.dragonclient.gui.DragonMenuScreen());
                    }
                }
            }

            if (hudEditorKey != null) {
                while (hudEditorKey.wasPressed()) {
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

    public static KeyBinding getZoomKey() {
        return zoomKey;
    }
}
