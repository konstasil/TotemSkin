package com.konstasiil.totemskin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class TotemSkinConfig {

    public enum TotemMode {
        CUSTOM,
        VANILLA
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("totemskin.json");

    private static TotemSkinConfig INSTANCE;

    public TotemMode globalMode = TotemMode.CUSTOM;
    public TotemMode mobsMode = TotemMode.CUSTOM;
    public boolean showInInventory = true;
    public Map<String, TotemMode> playerOverrides = new LinkedHashMap<>();

    public static TotemSkinConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public TotemMode getModeForPlayer(UUID playerUUID) {
        String key = playerUUID.toString();
        TotemMode override = playerOverrides.get(key);
        if (override != null) {
            return override;
        }
        return globalMode;
    }

    public TotemMode getModeForMob() {
        return mobsMode;
    }

    public void setPlayerOverride(UUID playerUUID, TotemMode mode) {
        playerOverrides.put(playerUUID.toString(), mode);
    }

    public void removePlayerOverride(UUID playerUUID) {
        playerOverrides.remove(playerUUID.toString());
    }

    public void setGlobalMode(TotemMode mode) {
        this.globalMode = mode;
    }

    public void setMobsMode(TotemMode mode) {
        this.mobsMode = mode;
    }

    public void setShowInInventory(boolean show) {
        this.showInInventory = show;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            TotemSkin.LOGGER.warn("TotemSkin: failed to save config: {}", e.getMessage());
        }
    }

    private static TotemSkinConfig load() {
        if (!Files.exists(CONFIG_PATH)) {
            return new TotemSkinConfig();
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            TotemSkinConfig config = GSON.fromJson(reader, TotemSkinConfig.class);
            if (config == null) return new TotemSkinConfig();
            if (config.playerOverrides == null) config.playerOverrides = new LinkedHashMap<>();
            return config;
        } catch (Exception e) {
            TotemSkin.LOGGER.warn("TotemSkin: failed to load config, using defaults: {}", e.getMessage());
            return new TotemSkinConfig();
        }
    }

    public static void reload() {
        INSTANCE = load();
    }
}
