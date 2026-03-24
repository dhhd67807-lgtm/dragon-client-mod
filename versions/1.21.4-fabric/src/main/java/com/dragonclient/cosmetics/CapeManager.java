package com.dragonclient.cosmetics;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class CapeManager {
    private static final String ANIMATED_CAPE_5_RESOURCE = "assets/dragonclient/textures/capes/cape5.gif";
    private static final String ANIMATED_CAPE_14_RESOURCE = "assets/dragonclient/textures/capes/cape14.gif";
    private static final String ANIMATED_CAPE_15_RESOURCE = "assets/dragonclient/textures/capes/cape15.gif";
    private static final String ANIMATED_CAPE_16_RESOURCE = "assets/dragonclient/textures/capes/cape16.gif";
    private static final String ANIMATED_CAPE_18_RESOURCE = "assets/dragonclient/textures/capes/cape18.gif";
    private static final String ANIMATED_CAPE_DO_RESOURCE = "assets/dragonclient/textures/capes/do.gif";

    private static final Identifier CAPE_1 = Identifier.of("dragonclient", "textures/capes/cape1.png");
    private static final Identifier CAPE_2 = Identifier.of("dragonclient", "textures/capes/cape2.png");
    private static final Identifier CAPE_3 = Identifier.of("dragonclient", "textures/capes/cape3.png");
    private static final Identifier CAPE_4 = Identifier.of("dragonclient", "textures/capes/cape4.png");
    private static final Identifier CAPE_10 = Identifier.of("dragonclient", "textures/capes/cape10.png");
    private static final Identifier CAPE_11 = Identifier.of("dragonclient", "textures/capes/cape11.png");
    private static final Identifier CAPE_12 = Identifier.of("dragonclient", "textures/capes/cape12.png");
    private static final Identifier CAPE_13 = Identifier.of("dragonclient", "textures/capes/cape13.png");
    private static final Identifier CAPE_17 = Identifier.of("dragonclient", "textures/capes/cape17.png");
    private static final Identifier CAPE_6 = Identifier.of("dragonclient", "textures/capes/cape6.png");
    private static final Identifier CAPE_7 = Identifier.of("dragonclient", "textures/capes/cape7.png");
    private static final Identifier CAPE_8 = Identifier.of("dragonclient", "textures/capes/cape8.png");
    private static final Identifier CAPE_9 = Identifier.of("dragonclient", "textures/capes/cape9.png");

    private static final Identifier CAPE_5_ANIMATED = Identifier.of("dragonclient", "dynamic/capes/cape5_animated");
    private static final Identifier CAPE_14_ANIMATED = Identifier.of("dragonclient", "dynamic/capes/cape14_animated");
    private static final Identifier CAPE_15_ANIMATED = Identifier.of("dragonclient", "dynamic/capes/cape15_animated");
    private static final Identifier CAPE_16_ANIMATED = Identifier.of("dragonclient", "dynamic/capes/cape16_animated");
    private static final Identifier CAPE_18_ANIMATED = Identifier.of("dragonclient", "dynamic/capes/cape18_animated");
    private static final Identifier CAPE_DO_ANIMATED = Identifier.of("dragonclient", "dynamic/capes/do_animated");

    private static CapeManager instance;
    private int equippedCapeIndex = -1;
    private final Gson gson = new Gson();
    private final Map<Integer, AnimatedCapeTexture> animatedCapeTextures = new HashMap<>();
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
        switch (equippedCapeIndex) {
            case 0:
                return CAPE_1;
            case 1:
                return CAPE_2;
            case 2:
                return CAPE_3;
            case 3:
                return CAPE_4;
            case 4:
                return ensureAnimatedCapeTexture(4, CAPE_5_ANIMATED, "dragonclient_animated_cape_5", ANIMATED_CAPE_5_RESOURCE);
            case 5:
                return CAPE_10;
            case 6:
                return CAPE_11;
            case 7:
                return CAPE_12;
            case 8:
                return CAPE_13;
            case 9:
                return ensureAnimatedCapeTexture(9, CAPE_14_ANIMATED, "dragonclient_animated_cape_14", ANIMATED_CAPE_14_RESOURCE);
            case 10:
                return ensureAnimatedCapeTexture(10, CAPE_15_ANIMATED, "dragonclient_animated_cape_15", ANIMATED_CAPE_15_RESOURCE);
            case 11:
                return ensureAnimatedCapeTexture(11, CAPE_16_ANIMATED, "dragonclient_animated_cape_16", ANIMATED_CAPE_16_RESOURCE);
            case 12:
                return CAPE_17;
            case 13:
                return ensureAnimatedCapeTexture(13, CAPE_18_ANIMATED, "dragonclient_animated_cape_18", ANIMATED_CAPE_18_RESOURCE);
            case 14:
                return CAPE_6;
            case 15:
                return CAPE_7;
            case 16:
                return CAPE_8;
            case 17:
                return CAPE_9;
            case 18:
                return ensureAnimatedCapeTexture(18, CAPE_DO_ANIMATED, "dragonclient_animated_cape_do", ANIMATED_CAPE_DO_RESOURCE);
            default:
                return null;
        }
    }

    public boolean hasCapeEquipped() {
        refreshIfNeeded();
        return equippedCapeIndex >= 0;
    }

    public void tick() {
        refreshIfNeeded();
        AnimatedCapeTexture animatedCapeTexture = animatedCapeTextures.get(equippedCapeIndex);
        if (animatedCapeTexture != null) {
            animatedCapeTexture.tick();
        }
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

    private Identifier ensureAnimatedCapeTexture(int capeIndex, Identifier textureId, String debugName, String resourcePath) {
        AnimatedCapeTexture animatedCapeTexture = animatedCapeTextures.get(capeIndex);
        if (animatedCapeTexture == null) {
            animatedCapeTexture = AnimatedCapeTexture.load(textureId, debugName, resourcePath);
            if (animatedCapeTexture == null) {
                return null;
            }
            animatedCapeTextures.put(capeIndex, animatedCapeTexture);
        }
        return textureId;
    }
}
