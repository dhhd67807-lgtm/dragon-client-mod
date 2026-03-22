package com.dragonclient.cosmetics;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.nio.file.Files;
import java.nio.file.Path;

public class CapeManager {
    private static CapeManager instance;
    private int equippedCapeIndex = -1;
    private static final Identifier CAPE_1 = Identifier.of("dragonclient", "textures/capes/cape1.png");
    private static final Identifier CAPE_2 = Identifier.of("dragonclient", "textures/capes/cape2.png");
    private static final Identifier CAPE_3 = Identifier.of("dragonclient", "textures/capes/cape3.png");
    private static final Identifier CAPE_4 = Identifier.of("dragonclient", "textures/capes/cape4.png");
    private final Gson gson = new Gson();
    private Path selectedCapePath;
    private long lastLoadedAtMs = 0L;
    private long lastSelectedCapeFileMtime = Long.MIN_VALUE;

    private CapeManager() {
        initializePaths();
        reloadFromLauncherConfig();
    }

    public static CapeManager getInstance() {
        if (instance == null) {
            instance = new CapeManager();
        }
        return instance;
    }

    public void setEquippedCape(int index) {
        this.equippedCapeIndex = index;
    }

    public int getEquippedCapeIndex() {
        refreshIfNeeded();
        return equippedCapeIndex;
    }

    public Identifier getCapeTexture() {
        refreshIfNeeded();
        if (equippedCapeIndex == 0) {
            return CAPE_1;
        } else if (equippedCapeIndex == 1) {
            return CAPE_2;
        } else if (equippedCapeIndex == 2) {
            return CAPE_3;
        } else if (equippedCapeIndex == 3) {
            return CAPE_4;
        } else if (equippedCapeIndex == 4) {
            // Older versions do not ship animated cape5.gif support; fallback to cape4.
            return CAPE_4;
        }
        return null;
    }

    public boolean hasCapeEquipped() {
        refreshIfNeeded();
        return equippedCapeIndex >= 0;
    }

    private void initializePaths() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) {
                return;
            }
            Path runDir = client.runDirectory.toPath();
            Path lapetusDir = runDir.toString().contains("instances")
                ? runDir.getParent().getParent()
                : runDir;
            selectedCapePath = lapetusDir.resolve("DragonSkins").resolve("selected_cape.json");
        } catch (Exception e) {
            System.err.println("[DragonClient] Failed to initialize cape path: " + e.getMessage());
        }
    }

    private void refreshIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastLoadedAtMs < 1500L) {
            return;
        }
        lastLoadedAtMs = now;
        reloadFromLauncherConfig();
    }

    private void reloadFromLauncherConfig() {
        if (selectedCapePath == null) {
            initializePaths();
        }
        if (selectedCapePath == null) {
            return;
        }
        try {
            if (!Files.exists(selectedCapePath)) {
                if (equippedCapeIndex != -1) {
                    equippedCapeIndex = -1;
                }
                lastSelectedCapeFileMtime = Long.MIN_VALUE;
                return;
            }
            long fileMtime = Files.getLastModifiedTime(selectedCapePath).toMillis();
            if (fileMtime == lastSelectedCapeFileMtime) {
                return;
            }
            String json = Files.readString(selectedCapePath);
            JsonObject data = gson.fromJson(json, JsonObject.class);
            int parsedIndex = -1;
            if (data != null && data.has("selected_cape_index") && !data.get("selected_cape_index").isJsonNull()) {
                parsedIndex = data.get("selected_cape_index").getAsInt();
            }
            this.equippedCapeIndex = parsedIndex;
            this.lastSelectedCapeFileMtime = fileMtime;
            System.out.println("[DragonClient] Loaded launcher cape index: " + parsedIndex);
        } catch (Exception e) {
            System.err.println("[DragonClient] Failed to load selected_cape.json: " + e.getMessage());
        }
    }
}
