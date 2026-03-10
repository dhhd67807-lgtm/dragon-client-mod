package com.dragonclient.cosmetica;

import cc.cosmetica.core.api.CachedImage;
import cc.cosmetica.core.api.CosmeticManagers;
import cc.cosmetica.core.api.ImageCosmetic;
import cc.cosmetica.core.api.NametagConfig;
import cc.cosmetica.core.api.PlayerCosmetics;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Optional;

public final class DragonCosmeticaIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger("DragonClient-Cosmetica");

    private static final Identifier MOON_WIZARD_CLOAK_TEXTURE =
        Identifier.of("dragonclient", "textures/cosmetica/moon_wizard_cloak.png");

    private static final String MOON_WIZARD_OUTFIT_ID = "dragonclient:moon_wizard";
    private static final String MOON_WIZARD_OUTFIT_NAME = "Moon Wizard";
    private static final String MOON_WIZARD_CLOAK_ID = "dragonclient:moon_wizard_cloak";
    private static final String MOON_WIZARD_CLOAK_NAME = "Moon Wizard Cloak";

    private static volatile boolean initialised;
    private static volatile boolean moonWizardEquipped;
    private static volatile PlayerCosmetics moonWizardCosmetics;

    private DragonCosmeticaIntegration() {
    }

    public static synchronized void init() {
        if (initialised) {
            return;
        }

        CosmeticManagers.registerCosmeticManager(-100, new MoonWizardCosmeticManager());
        initialised = true;

        LOGGER.info("Cosmetica integration ready (moon-wizard cosmetics are loaded lazily)");
    }

    public static boolean isMoonWizardAvailable() {
        ensureMoonWizardCosmeticsLoaded();
        return moonWizardCosmetics != null;
    }

    public static boolean isMoonWizardEquipped() {
        return moonWizardEquipped && moonWizardCosmetics != null;
    }

    public static void setMoonWizardEquipped(boolean equipped) {
        if (equipped) {
            ensureMoonWizardCosmeticsLoaded();
        }
        moonWizardEquipped = equipped && moonWizardCosmetics != null;
    }

    static PlayerCosmetics getMoonWizardCosmetics() {
        ensureMoonWizardCosmeticsLoaded();
        return moonWizardCosmetics;
    }

    private static synchronized void ensureMoonWizardCosmeticsLoaded() {
        if (moonWizardCosmetics == null) {
            moonWizardCosmetics = createMoonWizardCosmetics();
        }
    }

    private static PlayerCosmetics createMoonWizardCosmetics() {
        CachedImage cloakImage = createLoadedCachedImage(MOON_WIZARD_CLOAK_TEXTURE);
        if (cloakImage == null) {
            LOGGER.warn("Moon Wizard cloak texture was not found: {}", MOON_WIZARD_CLOAK_TEXTURE);
            return null;
        }

        ImageCosmetic cloak = new ImageCosmetic(
            cloakImage,
            MOON_WIZARD_CLOAK_NAME,
            MOON_WIZARD_CLOAK_ID,
            null,
            null,
            0
        );

        return new PlayerCosmetics(
            cloak,
            null,
            Collections.emptyList(),
            MOON_WIZARD_OUTFIT_NAME,
            MOON_WIZARD_OUTFIT_ID,
            NametagConfig.EMPTY,
            null,
            false
        );
    }

    private static CachedImage createLoadedCachedImage(Identifier textureId) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return null;
        }

        var resourceManager = client.getResourceManager();
        if (resourceManager == null) {
            return null;
        }

        Optional<Resource> resource = resourceManager.getResource(textureId);
        if (resource.isEmpty()) {
            return null;
        }

        try {
            Resource value = resource.get();
            try (var inputStream = value.getInputStream()) {
                NativeImage image = NativeImage.read(inputStream);
                CachedImage cachedImage = new CachedImage(textureId, 0);
                cachedImage.setLoaded(image.getWidth(), image.getHeight());
                image.close();
                return cachedImage;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load Moon Wizard texture {}", textureId, e);
            return null;
        }
    }
}
