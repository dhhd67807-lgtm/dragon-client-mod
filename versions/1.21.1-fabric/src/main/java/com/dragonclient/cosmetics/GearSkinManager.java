package com.dragonclient.cosmetics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import org.jetbrains.annotations.Nullable;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;

public final class GearSkinManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = Paths.get(System.getProperty("user.home"), ".dragonclient");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("gear_skins.json");

    public enum Category {
        SWORD("SWORDS", "Applies to all sword tiers"),
        PICKAXE("PICKAXES", "Applies to all pickaxe tiers"),
        AXE("AXES", "Applies to all axe tiers");

        private final String title;
        private final String description;

        Category(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public String title() {
            return title;
        }

        public String description() {
            return description;
        }
    }

    private static final class SkinOption {
        private final String label;
        private final Item carrier;

        private SkinOption(String label, Item carrier) {
            this.label = label;
            this.carrier = carrier;
        }
    }

    private static final class CategoryState {
        boolean enabled;
        int selectedIndex;
    }

    private static final EnumMap<Category, CategoryState> STATES = new EnumMap<>(Category.class);
    private static final EnumMap<Category, SkinOption[]> OPTIONS = new EnumMap<>(Category.class);
    private static boolean loaded = false;

    static {
        OPTIONS.put(Category.SWORD, new SkinOption[] {
            new SkinOption("Straight Sword", Items.COMMAND_BLOCK),
            new SkinOption("Bloodhound", Items.REPEATING_COMMAND_BLOCK),
            new SkinOption("Euphoria", Items.CHAIN_COMMAND_BLOCK),
            new SkinOption("Red Lightning", Items.BARRIER),
            new SkinOption("Great Sword", Items.STRUCTURE_BLOCK),
            new SkinOption("Winged Scythe", Items.STRUCTURE_VOID),
            new SkinOption("Heartflame Sword", Items.DEBUG_STICK),
            new SkinOption("Void Colossus Broadsword", Items.LIGHT)
        });

        OPTIONS.put(Category.PICKAXE, new SkinOption[] {
            new SkinOption("Ice Axe", Items.JIGSAW),
            new SkinOption("Heartflame Pickaxe", Items.KNOWLEDGE_BOOK),
            new SkinOption("Void Colossus Pickaxe", Items.BUNDLE)
        });

        OPTIONS.put(Category.AXE, new SkinOption[] {
            new SkinOption("Ice Axe", Items.COMMAND_BLOCK_MINECART),
            new SkinOption("Heartflame Axe", Items.MINECART),
            new SkinOption("Void Colossus Axe", Items.CHEST_MINECART)
        });

        resetDefaults();
    }

    private GearSkinManager() {}

    private static void resetDefaults() {
        STATES.clear();
        for (Category category : Category.values()) {
            CategoryState state = new CategoryState();
            state.enabled = false;
            state.selectedIndex = 0;
            STATES.put(category, state);
        }
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        resetDefaults();

        try {
            if (!Files.exists(CONFIG_FILE)) {
                save();
                return;
            }

            try (FileReader reader = new FileReader(CONFIG_FILE.toFile())) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                for (Category category : Category.values()) {
                    if (!root.has(category.name())) {
                        continue;
                    }

                    JsonObject obj = root.getAsJsonObject(category.name());
                    if (obj == null) {
                        continue;
                    }

                    CategoryState state = STATES.get(category);
                    if (obj.has("enabled")) {
                        state.enabled = obj.get("enabled").getAsBoolean();
                    }
                    if (obj.has("selectedIndex")) {
                        int selected = obj.get("selectedIndex").getAsInt();
                        int size = getOptions(category).length;
                        state.selectedIndex = size == 0 ? 0 : Math.floorMod(selected, size);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[DragonClient] Failed to load gear skins config: " + e.getMessage());
            e.printStackTrace();
            resetDefaults();
        }
    }

    public static synchronized void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            JsonObject root = new JsonObject();

            for (Category category : Category.values()) {
                CategoryState state = STATES.get(category);
                JsonObject obj = new JsonObject();
                obj.addProperty("enabled", state.enabled);
                obj.addProperty("selectedIndex", state.selectedIndex);
                root.add(category.name(), obj);
            }

            try (FileWriter writer = new FileWriter(CONFIG_FILE.toFile())) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            System.err.println("[DragonClient] Failed to save gear skins config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized boolean isEnabled(Category category) {
        load();
        return STATES.get(category).enabled;
    }

    public static synchronized void toggle(Category category) {
        load();
        CategoryState state = STATES.get(category);
        state.enabled = !state.enabled;
        save();
    }

    public static synchronized void setEnabled(Category category, boolean enabled) {
        load();
        CategoryState state = STATES.get(category);
        state.enabled = enabled;
        save();
    }

    public static synchronized void nextSkin(Category category) {
        load();
        SkinOption[] options = getOptions(category);
        if (options.length == 0) {
            return;
        }
        CategoryState state = STATES.get(category);
        state.selectedIndex = (state.selectedIndex + 1) % options.length;
        save();
    }

    public static synchronized void selectSkin(Category category, int index) {
        load();
        SkinOption[] options = getOptions(category);
        if (options.length == 0) {
            return;
        }
        CategoryState state = STATES.get(category);
        state.selectedIndex = Math.floorMod(index, options.length);
        save();
    }

    public static synchronized String getCurrentSkinLabel(Category category) {
        load();
        SkinOption[] options = getOptions(category);
        if (options.length == 0) {
            return "None";
        }
        int index = Math.floorMod(STATES.get(category).selectedIndex, options.length);
        return options[index].label;
    }

    public static synchronized int getSkinCount(Category category) {
        return getOptions(category).length;
    }

    public static synchronized ItemStack getPreviewStack(Category category) {
        load();
        SkinOption[] options = getOptions(category);
        if (options.length == 0) {
            return ItemStack.EMPTY;
        }
        int index = Math.floorMod(STATES.get(category).selectedIndex, options.length);
        return new ItemStack(options[index].carrier);
    }

    public static synchronized ItemStack getPreviewStack(Category category, int index) {
        load();
        SkinOption[] options = getOptions(category);
        if (options.length == 0) {
            return ItemStack.EMPTY;
        }
        int safeIndex = Math.floorMod(index, options.length);
        return new ItemStack(options[safeIndex].carrier);
    }

    public static synchronized String getSkinLabel(Category category, int index) {
        load();
        SkinOption[] options = getOptions(category);
        if (options.length == 0) {
            return "None";
        }
        int safeIndex = Math.floorMod(index, options.length);
        return options[safeIndex].label;
    }

    public static synchronized boolean isSelected(Category category, int index) {
        load();
        SkinOption[] options = getOptions(category);
        if (options.length == 0) {
            return false;
        }
        int safeIndex = Math.floorMod(index, options.length);
        return STATES.get(category).selectedIndex == safeIndex;
    }

    public static synchronized ItemStack getRenderStack(ItemStack original) {
        load();
        if (original == null || original.isEmpty()) {
            return original;
        }

        Category category = resolveCategory(original);
        if (category == null) {
            return original;
        }

        CategoryState state = STATES.get(category);
        if (state == null || !state.enabled) {
            return original;
        }

        SkinOption[] options = getOptions(category);
        if (options.length == 0) {
            return original;
        }

        int index = Math.floorMod(state.selectedIndex, options.length);
        Item carrier = options[index].carrier;

        try {
            return original.copyComponentsToNewStack(carrier, original.getCount());
        } catch (Exception e) {
            return original;
        }
    }

    /**
     * Applies configured Dragon gear skin for player-held items.
     */
    public static synchronized ItemStack getRenderStackForEntity(ItemStack original, @Nullable Entity holder) {
        if (holder == null) {
            return getRenderStack(original);
        }
        if (dragonclient$isPreviewDummy(holder) || !(holder instanceof PlayerEntity player)) {
            return original;
        }
        if (!dragonclient$shouldApplyToPlayer(player)) {
            return original;
        }

        return getRenderStack(original);
    }

    private static boolean dragonclient$isPreviewDummy(Entity holder) {
        return "com.dragonclient.gui.DummyPlayerEntity".equals(holder.getClass().getName());
    }

    private static boolean dragonclient$shouldApplyToPlayer(PlayerEntity player) {
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        if (client != null && client.player != null && player.getUuid().equals(client.player.getUuid())) {
            return true;
        }

        String playerName = player.getName().getString();
        SkinManager skinManager = SkinManager.getInstance();
        return skinManager.hasCustomSkin(playerName) || skinManager.hasCustomCape(playerName);
    }

    private static SkinOption[] getOptions(Category category) {
        SkinOption[] options = OPTIONS.get(category);
        return options == null ? new SkinOption[0] : options;
    }

    private static Category resolveCategory(ItemStack stack) {
        if (stack.isIn(ItemTags.SWORDS)) {
            return Category.SWORD;
        }
        if (stack.isIn(ItemTags.PICKAXES)) {
            return Category.PICKAXE;
        }
        if (stack.isIn(ItemTags.AXES)) {
            return Category.AXE;
        }
        return null;
    }
}
