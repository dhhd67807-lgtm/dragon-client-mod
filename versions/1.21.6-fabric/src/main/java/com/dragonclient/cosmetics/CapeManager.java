package com.dragonclient.cosmetics;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.nio.file.Files;
import java.nio.file.Path;

public class CapeManager {
    private static final long REFRESH_INTERVAL_MS = 1000L;
    private static CapeManager instance;
    private int equippedCapeIndex = -1;
    private static final Identifier CAPE_1 = Identifier.of("dragonclient", "textures/capes/cape1.png");
    private static final Identifier CAPE_2 = Identifier.of("dragonclient", "textures/capes/cape2.png");
    private static final Identifier CAPE_3 = Identifier.of("dragonclient", "textures/capes/cape3.png");
    private static final Identifier CAPE_4 = Identifier.of("dragonclient", "textures/capes/cape4.png");
    private Path selectedCapePath;
    private long lastRefreshCheck = 0L;
    private long lastKnownFileTimestamp = Long.MIN_VALUE;

    private CapeManager() {
        resolveSelectedCapePath();
        refreshFromDisk(true);
    }

    public static CapeManager getInstance() {
        if (instance == null) {
            instance = new CapeManager();
        }
        return instance;
    }

    private void resolveSelectedCapePath() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) {
                return;
            }

            Path runDir = client.runDirectory.toPath();
            Path lapetusDir;

            if (runDir.toString().contains("instances")
                    && runDir.getParent() != null
                    && runDir.getParent().getParent() != null) {
                lapetusDir = runDir.getParent().getParent();
            } else {
                lapetusDir = runDir;
            }

            selectedCapePath = lapetusDir.resolve("DragonSkins").resolve("selected_cape.json");
        } catch (Exception ignored) {
            // Keep previous value; caller will fall back to in-memory state.
        }
    }

    private int normalizeCapeIndex(int index) {
        return index >= 0 && index <= 3 ? index : -1;
    }

    private void refreshFromDiskIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastRefreshCheck < REFRESH_INTERVAL_MS) {
            return;
        }
        lastRefreshCheck = now;
        refreshFromDisk(false);
    }

    private void refreshFromDisk(boolean force) {
        if (selectedCapePath == null) {
            resolveSelectedCapePath();
        }

        if (selectedCapePath == null) {
            return;
        }

        try {
            if (!Files.exists(selectedCapePath)) {
                if (lastKnownFileTimestamp != Long.MIN_VALUE) {
                    equippedCapeIndex = -1;
                    lastKnownFileTimestamp = Long.MIN_VALUE;
                }
                return;
            }

            long modified = Files.getLastModifiedTime(selectedCapePath).toMillis();
            if (!force && modified == lastKnownFileTimestamp) {
                return;
            }

            lastKnownFileTimestamp = modified;
            String content = Files.readString(selectedCapePath).trim();
            if (content.isEmpty()) {
                equippedCapeIndex = -1;
                return;
            }

            JsonObject root = JsonParser.parseString(content).getAsJsonObject();
            JsonElement selectedCapeElement = root.get("selected_cape_index");
            if (selectedCapeElement == null || selectedCapeElement.isJsonNull()) {
                equippedCapeIndex = -1;
                return;
            }

            equippedCapeIndex = normalizeCapeIndex(selectedCapeElement.getAsInt());
        } catch (Exception ignored) {
            // Keep last known in-memory value if parsing/IO fails.
        }
    }

    public void setEquippedCape(int index) {
        this.equippedCapeIndex = normalizeCapeIndex(index);
    }

    public int getEquippedCapeIndex() {
        refreshFromDiskIfNeeded();
        return equippedCapeIndex;
    }

    public Identifier getCapeTexture() {
        refreshFromDiskIfNeeded();
        if (equippedCapeIndex == 0) {
            return CAPE_1;
        } else if (equippedCapeIndex == 1) {
            return CAPE_2;
        } else if (equippedCapeIndex == 2) {
            return CAPE_3;
        } else if (equippedCapeIndex == 3) {
            return CAPE_4;
        }
        return null;
    }

    public boolean hasCapeEquipped() {
        refreshFromDiskIfNeeded();
        return equippedCapeIndex >= 0;
    }
}
