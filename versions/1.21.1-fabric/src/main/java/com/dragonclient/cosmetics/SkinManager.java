package com.dragonclient.cosmetics;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import java.util.Map;

public class SkinManager {
    private static SkinManager instance;
    private final Map<String, Identifier> customSkins = new HashMap<>();
    private final Map<String, String> skinModels = new HashMap<>();
    private final Gson gson = new Gson();
    private Path skinsDir;
    private Path configPath;
    
    private SkinManager() {
        try {
            // Get Minecraft directory - need to go up from instance directory to lapetus root
            MinecraftClient client = MinecraftClient.getInstance();
            Path runDir = client.runDirectory.toPath();
            
            // If running in an instance (e.g., instances/dragon-1.21.1), go up to lapetus root
            Path lapetusDir;
            if (runDir.toString().contains("instances")) {
                // Go up from instances/dragon-1.21.1 to lapetus root
                lapetusDir = runDir.getParent().getParent();
            } else {
                // Already at lapetus root
                lapetusDir = runDir;
            }
            
            Path dragonSkinsDir = lapetusDir.resolve("DragonSkins");
            this.skinsDir = dragonSkinsDir.resolve("skins");
            this.configPath = dragonSkinsDir.resolve("skins.json");
            
            System.out.println("[DragonClient] SkinManager initialized");
            System.out.println("[DragonClient] Run directory: " + runDir);
            System.out.println("[DragonClient] Lapetus directory: " + lapetusDir);
            System.out.println("[DragonClient] DragonSkins directory: " + dragonSkinsDir);
            System.out.println("[DragonClient] Config path: " + configPath);
            
            loadCustomSkins();
        } catch (Exception e) {
            System.err.println("[DragonClient] Failed to initialize SkinManager: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static SkinManager getInstance() {
        if (instance == null) {
            instance = new SkinManager();
        }
        return instance;
    }
    
    private void loadCustomSkins() {
        try {
            if (!Files.exists(configPath)) {
                System.out.println("[DragonClient] No custom skins config found");
                return;
            }
            
            String json = Files.readString(configPath);
            JsonObject config = gson.fromJson(json, JsonObject.class);
            JsonArray skins = config.getAsJsonArray("skins");
            
            if (skins == null) {
                return;
            }
            
            for (int i = 0; i < skins.size(); i++) {
                JsonObject skin = skins.get(i).getAsJsonObject();
                String playerName = skin.get("player_name").getAsString();
                String skinPath = skin.get("skin_path").getAsString();
                String model = skin.get("model").getAsString();
                
                // Load and register skin texture
                Path skinFile = Paths.get(skinPath);
                if (Files.exists(skinFile)) {
                    Identifier skinId = loadSkinTexture(playerName, skinFile);
                    if (skinId != null) {
                        customSkins.put(playerName.toLowerCase(), skinId);
                        skinModels.put(playerName.toLowerCase(), model);
                        System.out.println("[DragonClient] Loaded custom skin for: " + playerName);
                    }
                }
            }
            
            System.out.println("[DragonClient] Loaded " + customSkins.size() + " custom skins");
        } catch (Exception e) {
            System.err.println("[DragonClient] Failed to load custom skins: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Identifier loadSkinTexture(String playerName, Path skinFile) {
        try {
            InputStream stream = new FileInputStream(skinFile.toFile());
            NativeImage image = NativeImage.read(stream);
            stream.close();
            
            NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
            Identifier identifier = Identifier.of("dragonclient", "skins/" + playerName.toLowerCase());
            
            MinecraftClient.getInstance().execute(() -> {
                MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, texture);
            });
            
            return identifier;
        } catch (Exception e) {
            System.err.println("[DragonClient] Failed to load skin texture for " + playerName + ": " + e.getMessage());
            return null;
        }
    }
    
    public Identifier getCustomSkin(String playerName) {
        return customSkins.get(playerName.toLowerCase());
    }
    
    public String getSkinModel(String playerName) {
        return skinModels.getOrDefault(playerName.toLowerCase(), "default");
    }
    
    public boolean hasCustomSkin(String playerName) {
        return customSkins.containsKey(playerName.toLowerCase());
    }
    
    public void reload() {
        customSkins.clear();
        skinModels.clear();
        loadCustomSkins();
    }
}
