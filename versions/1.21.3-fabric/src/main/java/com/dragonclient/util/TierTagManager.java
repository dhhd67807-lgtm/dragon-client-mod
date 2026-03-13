package com.dragonclient.util;

import com.dragonclient.module.visual.TierTaggerModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class TierTagManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final long LOCAL_RELOAD_INTERVAL_MS = 1500L;
    private static final long REMOTE_REFRESH_INTERVAL_MS = 60_000L;
    private static final long REMOTE_RETRY_INTERVAL_MS = 15_000L;
    private static final long CRACKED_LIST_REFRESH_INTERVAL_MS = 120_000L;
    private static final int HTTP_TIMEOUT_MS = 3500;
    private static final int MAX_REMOTE_CACHE_SIZE = 4096;
    private static final String CRACKED_API_BASE_URL = "https://totempopper67.pythonanywhere.com";
    private static final String CRACKED_API_KEY_HEADER = "X-TIER-TAGGER-KEY";
    private static final String CRACKED_API_KEY = "TIER-TAGGER-KEY-01";
    private static final String CRACKED_DEFAULT_CATEGORY = "vanilla";
    private static final String DEFAULT_SELF_TIER = "HT1";

    private static final List<String> PROFILE_API_URLS = List.of(
        "https://mctiers.com/api/v2/profile/",
        "https://pvptiers.com/api/profile/",
        "https://subtiers.net/api/profile/",
        "https://api.centraltierlist.com/v2/profile/"
    );

    private static final Set<String> VALID_TIERS = Set.of(
        "HT1", "HT2", "HT3", "HT4", "HT5",
        "LT1", "LT2", "LT3", "LT4", "LT5"
    );

    private static final Map<String, Formatting> TIER_TEXT_COLORS = Map.of(
        "HT1", Formatting.GOLD,
        "HT2", Formatting.RED,
        "HT3", Formatting.LIGHT_PURPLE,
        "HT4", Formatting.BLUE,
        "HT5", Formatting.DARK_PURPLE,
        "LT1", Formatting.GREEN,
        "LT2", Formatting.AQUA,
        "LT3", Formatting.YELLOW,
        "LT4", Formatting.DARK_AQUA,
        "LT5", Formatting.GRAY
    );

    private static final Map<String, String> LOCAL_PLAYER_TIERS = new ConcurrentHashMap<>();
    private static final Map<String, String> CRACKED_PLAYER_TIERS = new ConcurrentHashMap<>();
    private static final Map<String, CachedTierResult> REMOTE_PLAYER_TIERS = new ConcurrentHashMap<>();
    private static final Set<String> LOOKUPS_IN_FLIGHT = ConcurrentHashMap.newKeySet();

    private static final ExecutorService LOOKUP_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "dragonclient-tier-fetch");
        t.setDaemon(true);
        return t;
    });

    private static volatile Path tierTagsPath;
    private static volatile long lastReloadAttemptMs = 0L;
    private static volatile long lastLoadedMtime = Long.MIN_VALUE;
    private static volatile long lastCrackedListingFetchMs = 0L;
    private static volatile boolean crackedListingFetchInFlight = false;

    private TierTagManager() {}

    public static String getTierForPlayer(String playerName) {
        if (!TierTaggerModule.enabled || playerName == null || playerName.isBlank()) {
            return null;
        }

        String key = playerName.toLowerCase(Locale.ROOT);
        refreshLocalIfNeeded();
        scheduleCrackedListingFetchIfNeeded();
        scheduleRemoteLookupIfNeeded(key, playerName);

        CachedTierResult remote = REMOTE_PLAYER_TIERS.get(key);
        if (remote != null && remote.tier != null) {
            return remote.tier;
        }

        String crackedTier = CRACKED_PLAYER_TIERS.get(key);
        if (crackedTier != null) {
            return crackedTier;
        }

        String localTier = LOCAL_PLAYER_TIERS.get(key);
        if (localTier != null) {
            return localTier;
        }

        // Keep local cracked nametag usable even when external APIs don't return this player.
        // Optional override in tier_tags.json:
        // {
        //   "players": { "_self": "LT2" }
        // }
        if (isCurrentPlayer(playerName)) {
            String selfTier = LOCAL_PLAYER_TIERS.get("_self");
            if (selfTier != null) {
                return selfTier;
            }
            return DEFAULT_SELF_TIER;
        }

        return null;
    }

    public static Text buildTierPrefix(String tier) {
        if (tier == null || tier.isBlank()) {
            return Text.empty();
        }

        MutableText prefix = Text.empty();
        Formatting color = TIER_TEXT_COLORS.getOrDefault(tier, Formatting.AQUA);
        prefix.append(Text.literal("[" + tier + "] ").formatted(color));
        return prefix;
    }

    public static Text decorateName(Text baseName, String playerName) {
        String tier = getTierForPlayer(playerName);
        if (tier == null) {
            return baseName;
        }

        return Text.empty().append(buildTierPrefix(tier)).append(baseName.copy());
    }

    private static void refreshLocalIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastReloadAttemptMs < LOCAL_RELOAD_INTERVAL_MS) {
            return;
        }
        lastReloadAttemptMs = now;

        if (!ensurePaths()) {
            return;
        }

        createTemplateIfMissing();
        if (!Files.exists(tierTagsPath)) {
            return;
        }

        try {
            long mtime = Files.getLastModifiedTime(tierTagsPath).toMillis();
            if (mtime == lastLoadedMtime) {
                return;
            }

            String raw = Files.readString(tierTagsPath);
            JsonElement parsed = JsonParser.parseString(raw);
            if (!parsed.isJsonObject()) {
                return;
            }

            JsonObject root = parsed.getAsJsonObject();
            JsonObject playersObj = root.has("players") && root.get("players").isJsonObject()
                ? root.getAsJsonObject("players")
                : root;

            Map<String, String> next = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : playersObj.entrySet()) {
                if (entry.getValue() == null || !entry.getValue().isJsonPrimitive()) {
                    continue;
                }
                String tier = normalizeTier(entry.getValue().getAsString());
                if (tier == null) {
                    continue;
                }
                String username = entry.getKey() == null ? "" : entry.getKey().trim();
                if (!username.isEmpty()) {
                    next.put(username.toLowerCase(Locale.ROOT), tier);
                }
            }
            LOCAL_PLAYER_TIERS.clear();
            LOCAL_PLAYER_TIERS.putAll(next);
            lastLoadedMtime = mtime;
        } catch (Exception e) {
            System.err.println("[DragonClient] Failed to load tier_tags.json: " + e.getMessage());
        }
    }

    private static void scheduleRemoteLookupIfNeeded(String key, String playerName) {
        long now = System.currentTimeMillis();
        CachedTierResult cached = REMOTE_PLAYER_TIERS.get(key);

        if (cached != null) {
            long age = now - cached.fetchedAtMs;
            if (cached.success && age < REMOTE_REFRESH_INTERVAL_MS) {
                return;
            }
            if (!cached.success && age < REMOTE_RETRY_INTERVAL_MS) {
                return;
            }
        }

        if (!LOOKUPS_IN_FLIGHT.add(key)) {
            return;
        }

        LOOKUP_EXECUTOR.execute(() -> {
            try {
                String tier = fetchRemoteTier(playerName);
                REMOTE_PLAYER_TIERS.put(key, new CachedTierResult(tier, System.currentTimeMillis(), tier != null));
                if (REMOTE_PLAYER_TIERS.size() > MAX_REMOTE_CACHE_SIZE) {
                    REMOTE_PLAYER_TIERS.clear();
                }
            } catch (Exception ignored) {
                REMOTE_PLAYER_TIERS.put(key, new CachedTierResult(null, System.currentTimeMillis(), false));
            } finally {
                LOOKUPS_IN_FLIGHT.remove(key);
            }
        });
    }

    private static String fetchRemoteTier(String playerName) {
        String cleanName = sanitizePlayerName(playerName);
        if (cleanName.isEmpty()) {
            return null;
        }

        String crackedInfoTier = fetchCrackedInfoTier(cleanName);
        if (crackedInfoTier != null) {
            return crackedInfoTier;
        }

        List<String> identifiers = new ArrayList<>();
        String mojangUuid = fetchMojangUuid(cleanName);
        if (mojangUuid != null) {
            identifiers.add(mojangUuid);
            String dashed = dashUuid(mojangUuid);
            if (dashed != null) {
                identifiers.add(dashed);
            }
        }
        identifiers.add(cleanName);

        for (String baseUrl : PROFILE_API_URLS) {
            for (String id : identifiers) {
                String endpoint = baseUrl + urlEncode(id);
                String tier = resolveTierFromEndpoint(endpoint);
                if (tier != null) {
                    return tier;
                }

                if (baseUrl.contains("mctiers.com")) {
                    String rankingsTier = resolveTierFromEndpoint(endpoint + "/rankings");
                    if (rankingsTier != null) {
                        return rankingsTier;
                    }
                }
            }
        }

        return null;
    }

    private static void scheduleCrackedListingFetchIfNeeded() {
        long now = System.currentTimeMillis();
        synchronized (TierTagManager.class) {
            if (crackedListingFetchInFlight || now - lastCrackedListingFetchMs < CRACKED_LIST_REFRESH_INTERVAL_MS) {
                return;
            }
            crackedListingFetchInFlight = true;
            lastCrackedListingFetchMs = now;
        }

        LOOKUP_EXECUTOR.execute(() -> {
            try {
                Map<String, String> latest = fetchCrackedListing(CRACKED_DEFAULT_CATEGORY);
                if (latest != null) {
                    CRACKED_PLAYER_TIERS.clear();
                    CRACKED_PLAYER_TIERS.putAll(latest);
                }
            } catch (Exception ignored) {
            } finally {
                synchronized (TierTagManager.class) {
                    crackedListingFetchInFlight = false;
                }
            }
        });
    }

    private static String fetchCrackedInfoTier(String playerName) {
        JsonElement root = fetchJson(
            CRACKED_API_BASE_URL + "/api/info/" + urlEncode(playerName),
            true
        );
        if (root == null || !root.isJsonObject()) {
            return null;
        }

        JsonObject dataObj = extractDataObject(root.getAsJsonObject());
        if (dataObj == null) {
            return null;
        }

        String selectedTier = normalizeTier(asString(dataObj, "tier"));
        String fallbackTier = selectedTier;

        JsonElement categoriesElement = dataObj.get("categories");
        if (categoriesElement != null && categoriesElement.isJsonArray()) {
            JsonArray categories = categoriesElement.getAsJsonArray();
            for (JsonElement element : categories) {
                if (element == null || !element.isJsonObject()) {
                    continue;
                }

                JsonObject categoryObj = element.getAsJsonObject();
                String category = asString(categoryObj, "category");
                String tier = normalizeTier(asString(categoryObj, "tier"));
                if (tier == null) {
                    continue;
                }

                if (fallbackTier == null) {
                    fallbackTier = tier;
                }
                if (category != null && category.equalsIgnoreCase(CRACKED_DEFAULT_CATEGORY)) {
                    return tier;
                }
            }
        }

        return fallbackTier;
    }

    private static Map<String, String> fetchCrackedListing(String category) {
        JsonElement root = fetchJson(
            CRACKED_API_BASE_URL + "/api/tiertagger/fetch/" + urlEncode(category),
            true
        );
        if (root == null || !root.isJsonObject()) {
            return null;
        }

        JsonObject dataObj = extractDataObject(root.getAsJsonObject());
        if (dataObj == null) {
            return null;
        }

        JsonElement playersElement = dataObj.get("players");
        if (playersElement == null || !playersElement.isJsonArray()) {
            return null;
        }

        Map<String, String> next = new HashMap<>();
        for (JsonElement playerElement : playersElement.getAsJsonArray()) {
            if (playerElement == null || !playerElement.isJsonObject()) {
                continue;
            }

            JsonObject playerObj = playerElement.getAsJsonObject();
            String username = sanitizePlayerName(asString(playerObj, "username"));
            String tier = normalizeTier(asString(playerObj, "tier"));
            if (!username.isEmpty() && tier != null) {
                next.put(username.toLowerCase(Locale.ROOT), tier);
            }
        }
        return next;
    }

    private static String resolveTierFromEndpoint(String endpoint) {
        JsonElement data = fetchJson(endpoint);
        if (data == null) {
            return null;
        }

        List<TierCandidate> candidates = new ArrayList<>();
        collectTierCandidates(data, candidates);
        if (candidates.isEmpty()) {
            return null;
        }

        TierCandidate best = candidates.get(0);
        for (TierCandidate candidate : candidates) {
            if (candidate.score < best.score) {
                best = candidate;
            }
        }
        return best.tier;
    }

    private static void collectTierCandidates(JsonElement element, List<TierCandidate> out) {
        if (element == null || element.isJsonNull()) {
            return;
        }

        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            TierCandidate candidate = parseTierCandidate(obj);
            if (candidate != null) {
                out.add(candidate);
            }
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                collectTierCandidates(entry.getValue(), out);
            }
            return;
        }

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement item : array) {
                collectTierCandidates(item, out);
            }
        }
    }

    private static TierCandidate parseTierCandidate(JsonObject obj) {
        String direct = parseDirectTier(obj.get("tier"));
        if (direct == null) direct = parseDirectTier(obj.get("rank"));
        if (direct == null) direct = parseDirectTier(obj.get("display"));
        if (direct == null) direct = parseDirectTier(obj.get("peakTier"));
        if (direct != null) {
            return new TierCandidate(direct, scoreFromTier(direct));
        }

        Integer tierNumber = parseTierNumber(obj.get("tier"));
        if (tierNumber == null) tierNumber = parseTierNumber(obj.get("tierNumber"));
        if (tierNumber == null) tierNumber = parseTierNumber(obj.get("value"));
        if (tierNumber == null) {
            return null;
        }

        int pos = parsePos(obj);
        if (pos == Integer.MIN_VALUE) {
            if (looksLikeRankingObject(obj)) {
                pos = 0;
            } else {
                return null;
            }
        }

        String tier = normalizeTier((pos <= 0 ? "HT" : "LT") + tierNumber);
        if (tier == null) {
            return null;
        }

        return new TierCandidate(tier, scoreFromTier(tier));
    }

    private static String parseDirectTier(JsonElement element) {
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            return null;
        }
        return normalizeTier(element.getAsString());
    }

    private static Integer parseTierNumber(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) {
            return null;
        }

        try {
            if (element.getAsJsonPrimitive().isNumber()) {
                int value = element.getAsInt();
                return value >= 1 && value <= 5 ? value : null;
            }
            if (element.getAsJsonPrimitive().isString()) {
                String raw = element.getAsString().trim();
                String normalized = normalizeTier(raw);
                if (normalized != null) {
                    return normalized.charAt(2) - '0';
                }
                int value = Integer.parseInt(raw);
                return value >= 1 && value <= 5 ? value : null;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private static int parsePos(JsonObject obj) {
        int fromPos = parsePosValue(obj.get("pos"));
        if (fromPos != Integer.MIN_VALUE) return fromPos;

        int fromPosition = parsePosValue(obj.get("position"));
        if (fromPosition != Integer.MIN_VALUE) return fromPosition;

        int fromSide = parsePosValue(obj.get("side"));
        if (fromSide != Integer.MIN_VALUE) return fromSide;

        int fromIsHigh = parseBooleanSide(obj.get("isHigh"));
        if (fromIsHigh != Integer.MIN_VALUE) return fromIsHigh;

        int fromHigh = parseBooleanSide(obj.get("high"));
        if (fromHigh != Integer.MIN_VALUE) return fromHigh;

        return Integer.MIN_VALUE;
    }

    private static int parseBooleanSide(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) {
            return Integer.MIN_VALUE;
        }

        try {
            if (element.getAsJsonPrimitive().isBoolean()) {
                return element.getAsBoolean() ? 0 : 1;
            }
        } catch (Exception ignored) {
        }
        return Integer.MIN_VALUE;
    }

    private static boolean looksLikeRankingObject(JsonObject obj) {
        return obj.has("mode")
            || obj.has("gamemode")
            || obj.has("category")
            || obj.has("queue")
            || obj.has("region")
            || obj.has("retired")
            || obj.has("elo")
            || obj.has("mmr")
            || obj.has("wins")
            || obj.has("losses")
            || obj.has("ranked");
    }

    private static int parsePosValue(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) {
            return Integer.MIN_VALUE;
        }

        try {
            if (element.getAsJsonPrimitive().isNumber()) {
                return element.getAsInt() <= 0 ? 0 : 1;
            }

            if (element.getAsJsonPrimitive().isString()) {
                String raw = element.getAsString().trim().toLowerCase(Locale.ROOT);
                if (raw.isEmpty()) return Integer.MIN_VALUE;
                if (raw.equals("0") || raw.equals("h") || raw.equals("high") || raw.equals("top") || raw.equals("upper")) return 0;
                if (raw.equals("1") || raw.equals("l") || raw.equals("low") || raw.equals("bottom") || raw.equals("lower")) return 1;
            }
        } catch (Exception ignored) {
        }

        return Integer.MIN_VALUE;
    }

    private static int scoreFromTier(String tier) {
        if (tier == null || tier.length() < 3) {
            return Integer.MAX_VALUE;
        }
        int tierNum = tier.charAt(2) - '0';
        int pos = tier.startsWith("HT") ? 0 : 1;
        return (tierNum * 2) + pos;
    }

    private static JsonElement fetchJson(String endpoint) {
        return fetchJson(endpoint, false);
    }

    private static JsonElement fetchJson(String endpoint, boolean crackedApi) {
        HttpURLConnection conn = null;
        try {
            URI uri = URI.create(endpoint);
            conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(HTTP_TIMEOUT_MS);
            conn.setReadTimeout(HTTP_TIMEOUT_MS);
            conn.setUseCaches(false);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "DragonClient-TierTagger/2.0");
            if (crackedApi) {
                conn.setRequestProperty(CRACKED_API_KEY_HEADER, CRACKED_API_KEY);
            }

            int status = conn.getResponseCode();
            if (status < 200 || status >= 300) {
                return null;
            }

            try (InputStream stream = conn.getInputStream()) {
                String raw = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                if (raw.isBlank()) {
                    return null;
                }
                return JsonParser.parseString(raw);
            }
        } catch (Exception ignored) {
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String fetchMojangUuid(String playerName) {
        String endpoint = "https://api.mojang.com/users/profiles/minecraft/" + urlEncode(playerName);
        JsonElement response = fetchJson(endpoint);
        if (response == null || !response.isJsonObject()) {
            return null;
        }

        JsonObject obj = response.getAsJsonObject();
        if (!obj.has("id") || !obj.get("id").isJsonPrimitive()) {
            return null;
        }

        String uuid = obj.get("id").getAsString();
        if (uuid == null) {
            return null;
        }

        String normalized = uuid.trim().replace("-", "");
        if (normalized.length() != 32) {
            return null;
        }

        return normalized;
    }

    private static String dashUuid(String compactUuid) {
        if (compactUuid == null || compactUuid.length() != 32) {
            return null;
        }

        return compactUuid.substring(0, 8) + "-"
            + compactUuid.substring(8, 12) + "-"
            + compactUuid.substring(12, 16) + "-"
            + compactUuid.substring(16, 20) + "-"
            + compactUuid.substring(20);
    }

    private static String sanitizePlayerName(String playerName) {
        String cleaned = playerName == null ? "" : playerName.trim();
        cleaned = cleaned.replaceAll("[^A-Za-z0-9_]", "");
        if (cleaned.length() > 16) {
            cleaned = cleaned.substring(0, 16);
        }
        return cleaned;
    }

    private static boolean isCurrentPlayer(String playerName) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            return client != null
                && client.player != null
                && playerName != null
                && playerName.equalsIgnoreCase(client.player.getName().getString());
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean ensurePaths() {
        if (tierTagsPath != null) {
            return true;
        }
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) {
                return false;
            }
            Path runDir = client.runDirectory.toPath();
            Path lapetusDir = runDir.toString().contains("instances")
                ? runDir.getParent().getParent()
                : runDir;
            tierTagsPath = lapetusDir.resolve("DragonSkins").resolve("tier_tags.json");
            return true;
        } catch (Exception e) {
            System.err.println("[DragonClient] Failed to initialize tier tag path: " + e.getMessage());
            return false;
        }
    }

    private static void createTemplateIfMissing() {
        if (tierTagsPath == null || Files.exists(tierTagsPath)) {
            return;
        }
        try {
            Files.createDirectories(tierTagsPath.getParent());

            JsonObject root = new JsonObject();
            JsonObject players = new JsonObject();
            root.add("players", players);

            Files.writeString(
                tierTagsPath,
                GSON.toJson(root),
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE
            );
        } catch (Exception e) {
            System.err.println("[DragonClient] Failed to create tier_tags.json template: " + e.getMessage());
        }
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            return value;
        }
    }

    private static String asString(JsonObject object, String key) {
        if (object == null || key == null || !object.has(key)) {
            return null;
        }

        JsonElement value = object.get(key);
        if (value == null || value.isJsonNull() || !value.isJsonPrimitive()) {
            return null;
        }

        try {
            return value.getAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static JsonObject extractDataObject(JsonObject root) {
        if (root == null) {
            return null;
        }

        JsonElement data = root.get("data");
        if (data != null && data.isJsonObject()) {
            return data.getAsJsonObject();
        }
        return root;
    }

    private static String normalizeTier(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT)
            .replace(" ", "")
            .replace("_", "")
            .replace("-", "");

        if (VALID_TIERS.contains(normalized)) {
            return normalized;
        }

        if (normalized.matches("[HL]T?[1-5]")) {
            char side = normalized.charAt(0);
            char tier = normalized.charAt(normalized.length() - 1);
            return (side == 'H' ? "HT" : "LT") + tier;
        }

        if (normalized.matches("T[1-5][HL]")) {
            char tier = normalized.charAt(1);
            char side = normalized.charAt(2);
            return (side == 'H' ? "HT" : "LT") + tier;
        }

        return null;
    }

    private static final class CachedTierResult {
        private final String tier;
        private final long fetchedAtMs;
        private final boolean success;

        private CachedTierResult(String tier, long fetchedAtMs, boolean success) {
            this.tier = tier;
            this.fetchedAtMs = fetchedAtMs;
            this.success = success;
        }
    }

    private static final class TierCandidate {
        private final String tier;
        private final int score;

        private TierCandidate(String tier, int score) {
            this.tier = tier;
            this.score = score;
        }
    }
}
