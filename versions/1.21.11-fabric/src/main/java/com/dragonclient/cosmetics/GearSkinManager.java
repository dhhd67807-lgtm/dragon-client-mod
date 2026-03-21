package com.dragonclient.cosmetics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
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
        private final Identifier modelId;

        private SkinOption(String label, String modelPath) {
            this.label = label;
            // Support both namespaced (void:void/model) and non-namespaced (model) paths
            if (modelPath.contains(":")) {
                this.modelId = Identifier.of(modelPath);
            } else {
                this.modelId = Identifier.of("minecraft", modelPath);
            }
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
            new SkinOption("Dark Sword", "dc_dark_sword"),
            new SkinOption("Cursed Sword", "dc_cursed_sword"),
            new SkinOption("Demonic Blade", "dc_demonic_blade"),
            new SkinOption("Holy Spear", "dc_holy_spear"),
            new SkinOption("Kaz Scythe", "dc_kaz_sc"),
            new SkinOption("Magnetic Blade", "dc_magnetic_blade"),
            new SkinOption("Pox Spreader", "dc_pox_spreader"),
            new SkinOption("Red Hammer", "dc_red_hammer"),
            new SkinOption("Straight Sword", "dc_straight_sword"),
            new SkinOption("Bloodhound", "dc_bloodhound"),
            new SkinOption("Euphoria", "dc_euphoria"),
            new SkinOption("Great Sword", "dc_great_sword"),
            new SkinOption("Winged Scythe", "dc_winged_scythe"),
            new SkinOption("Red Lightning", "dc_red_lightning"),
            new SkinOption("Heartflame Sword", "heartflame_sword_dc"),
            new SkinOption("Void Colossus Broadsword", "void_colossus_broadsword")
        });

        OPTIONS.put(Category.PICKAXE, new SkinOption[] {
            new SkinOption("Heartflame Pickaxe", "heartflame_pickaxe_dc"),
            new SkinOption("Mana Pickaxe", "dc_mana_pickaxe"),
            new SkinOption("Void Colossus Pickaxe", "void_colossus_pickaxe")
        });

        OPTIONS.put(Category.AXE, new SkinOption[] {
            new SkinOption("Heartflame Axe", "heartflame_axe_dc"),
            new SkinOption("Mana Axe", "dc_mana_axe"),
            new SkinOption("Ice Axe", "dc_ice_axe"),
            new SkinOption("Void Colossus Axe", "void_colossus_axe")
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
        return createPreviewStack(category, options[index]);
    }

    public static synchronized ItemStack getPreviewStack(Category category, int index) {
        load();
        SkinOption[] options = getOptions(category);
        if (options.length == 0) {
            return ItemStack.EMPTY;
        }
        int safeIndex = Math.floorMod(index, options.length);
        return createPreviewStack(category, options[safeIndex]);
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
        SkinOption option = options[index];

        try {
            ItemStack renderStack = original.copy();
            renderStack.set(DataComponentTypes.ITEM_MODEL, option.modelId);
            return renderStack;
        } catch (Exception e) {
            return original;
        }
    }

    /**
     * Owner-aware render replacement.
     * Local player keeps selected Dragon gear skin, remote entities stay vanilla.
     */
    public static synchronized ItemStack getRenderStackForEntity(ItemStack original, @Nullable Entity holder) {
        if (holder != null) {
            if (holder instanceof PlayerEntity player) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client == null || client.player == null) {
                    return original;
                }
                if (!player.getUuid().equals(client.player.getUuid())) {
                    return original;
                }
            } else {
                return original;
            }
        }

        return getRenderStack(original);
    }

    private static ItemStack createPreviewStack(Category category, SkinOption option) {
        ItemStack stack = new ItemStack(getPreviewBaseItem(category));
        try {
            stack.set(DataComponentTypes.ITEM_MODEL, option.modelId);
        } catch (Exception ignored) {
        }
        return stack;
    }

    private static Item getPreviewBaseItem(Category category) {
        return switch (category) {
            case SWORD -> Items.DIAMOND_SWORD;
            case PICKAXE -> Items.DIAMOND_PICKAXE;
            case AXE -> Items.DIAMOND_AXE;
        };
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
