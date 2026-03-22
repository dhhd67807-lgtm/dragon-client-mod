package com.dragonclient.cosmetics;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SkinManager {
    private static SkinManager instance;
    private final Map<String, Identifier> customSkins = new HashMap<>();
    private final Map<String, Identifier> customCapes = new HashMap<>();
    private final Map<String, String> skinModels = new HashMap<>();
    private final Gson gson = new Gson();
    private Path configPath;
    private long lastLoadedAtMs = 0L;
    private long lastConfigMtime = Long.MIN_VALUE;

    private SkinManager() {
        initializePaths();
        reloadInternal(true);
    }

    public static SkinManager getInstance() {
        if (instance == null) {
            instance = new SkinManager();
        }
        return instance;
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

            Path dragonSkinsDir = lapetusDir.resolve("DragonSkins");
            this.configPath = dragonSkinsDir.resolve("skins.json");

            System.out.println("[DragonClient] SkinManager initialized");
            System.out.println("[DragonClient] Run directory: " + runDir);
            System.out.println("[DragonClient] Lapetus directory: " + lapetusDir);
            System.out.println("[DragonClient] Config path: " + configPath);
        } catch (Exception e) {
            System.err.println("[DragonClient] Failed to initialize SkinManager: " + e.getMessage());
        }
    }

    private static String normalizeKey(String playerName) {
        if (playerName == null) {
            return "";
        }
        return playerName.trim().toLowerCase(Locale.ROOT);
    }

    private void refreshIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastLoadedAtMs < 1500L) {
            return;
        }
        reloadInternal(false);
    }

    private void reloadInternal(boolean force) {
        if (configPath == null) {
            initializePaths();
        }
        if (configPath == null) {
            return;
        }

        lastLoadedAtMs = System.currentTimeMillis();

        try {
            if (!Files.exists(configPath)) {
                customSkins.clear();
                customCapes.clear();
                skinModels.clear();
                lastConfigMtime = Long.MIN_VALUE;
                return;
            }

            long fileMtime = Files.getLastModifiedTime(configPath).toMillis();
            if (!force && fileMtime == lastConfigMtime) {
                return;
            }

            String json = Files.readString(configPath);
            JsonObject config = gson.fromJson(json, JsonObject.class);
            JsonArray skins = config != null ? config.getAsJsonArray("skins") : null;

            customSkins.clear();
            customCapes.clear();
            skinModels.clear();

            if (skins != null) {
                for (int i = 0; i < skins.size(); i++) {
                    JsonObject skin = skins.get(i).getAsJsonObject();
                    String playerName = skin.get("player_name").getAsString();
                    String key = normalizeKey(playerName);

                    JsonElement skinPathElement = skin.get("skin_path");
                    if (skinPathElement != null && !skinPathElement.isJsonNull()) {
                        String skinPath = skinPathElement.getAsString();
                        Path skinFile = Paths.get(skinPath);
                        if (Files.exists(skinFile)) {
                            Identifier skinId = loadSkinTexture(key, skinFile);
                            if (skinId != null) {
                                customSkins.put(key, skinId);
                            }
                        }
                    }

                    JsonElement capePathElement = skin.get("cape_path");
                    if (capePathElement != null && !capePathElement.isJsonNull()) {
                        String capePath = capePathElement.getAsString();
                        if (capePath != null && !capePath.trim().isEmpty()) {
                            Path capeFile = Paths.get(capePath);
                            if (Files.exists(capeFile)) {
                                Identifier capeId = loadCapeTexture(key, capeFile);
                                if (capeId != null) {
                                    customCapes.put(key, capeId);
                                }
                            }
                        }
                    }

                    JsonElement modelElement = skin.get("model");
                    String model = modelElement != null && !modelElement.isJsonNull()
                        ? modelElement.getAsString()
                        : "default";
                    skinModels.put(key, model);
                }
            }

            lastConfigMtime = fileMtime;
            System.out.println("[DragonClient] Loaded " + customSkins.size() + " custom skins and " + customCapes.size() + " custom capes");
        } catch (Exception e) {
            System.err.println("[DragonClient] Failed to load custom skins: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Identifier loadSkinTexture(String key, Path skinFile) {
        try {
            InputStream stream = new FileInputStream(skinFile.toFile());
            NativeImage image = NativeImage.read(stream);
            stream.close();

            NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
            Identifier identifier = Identifier.of("dragonclient", "skins/" + key);

            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                client.execute(() -> client.getTextureManager().registerTexture(identifier, texture));
            }

            return identifier;
        } catch (Exception e) {
            System.err.println("[DragonClient] Failed to load skin texture for " + key + ": " + e.getMessage());
            return null;
        }
    }

    private Identifier loadCapeTexture(String key, Path capeFile) {
        try {
            InputStream stream = new FileInputStream(capeFile.toFile());
            NativeImage image = NativeImage.read(stream);
            stream.close();

            NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
            Identifier identifier = Identifier.of("dragonclient", "capes/custom_" + key);

            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                client.execute(() -> client.getTextureManager().registerTexture(identifier, texture));
            }

            return identifier;
        } catch (Exception e) {
            System.err.println("[DragonClient] Failed to load cape texture for " + key + ": " + e.getMessage());
            return null;
        }
    }

    public Identifier getCustomSkin(String playerName) {
        refreshIfNeeded();
        return customSkins.get(normalizeKey(playerName));
    }

    public Identifier getCustomCape(String playerName) {
        refreshIfNeeded();
        return customCapes.get(normalizeKey(playerName));
    }

    public String getSkinModel(String playerName) {
        refreshIfNeeded();
        return skinModels.getOrDefault(normalizeKey(playerName), "default");
    }

    public boolean hasCustomSkin(String playerName) {
        refreshIfNeeded();
        return customSkins.containsKey(normalizeKey(playerName));
    }

    public boolean hasCustomCape(String playerName) {
        refreshIfNeeded();
        return customCapes.containsKey(normalizeKey(playerName));
    }

    public void reload() {
        reloadInternal(true);
    }
}
