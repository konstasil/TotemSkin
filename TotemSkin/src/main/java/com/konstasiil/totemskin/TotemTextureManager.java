package com.konstasiil.totemskin;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.player.PlayerSkin;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TotemTextureManager {

    private static final Identifier TOTEM_ATLAS_ID = Identifier.fromNamespaceAndPath("minecraft", "item/totem_of_undying");
    private static final Identifier TOTEM_TEXTURE_LOCATION = Identifier.fromNamespaceAndPath("minecraft", "item/totem_of_undying");
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Map<UUID, NativeImage> skinCache = new ConcurrentHashMap<>();
    private static final Map<UUID, NativeImage> totemCache = new ConcurrentHashMap<>();
    private static NativeImage vanillaTotemPixels;
    private static boolean initialized = false;

    public static void init() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getTextureManager() == null) return;

        try {
            Optional<Resource> resource = mc.getResourceManager().getResource(TOTEM_TEXTURE_LOCATION);
            if (resource.isPresent()) {
                try (InputStream is = resource.get().open()) {
                    vanillaTotemPixels = NativeImage.read(is);
                }
            }
        } catch (Exception e) {
            TotemSkin.LOGGER.warn("Failed to load vanilla totem texture: {}", e.getMessage());
            return;
        }

        if (vanillaTotemPixels == null) return;

        TextureAtlas itemsAtlas = getItemsAtlas();
        if (itemsAtlas == null) return;

        TextureAtlasSprite totemSprite = itemsAtlas.getSprite(TOTEM_ATLAS_ID);
        if (totemSprite == null) return;

        initialized = true;
        TotemSkin.LOGGER.info("TotemSkin texture manager initialized");
    }

    public static TextureAtlas getItemsAtlas() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getTextureManager() == null) return null;
        var tex = mc.getTextureManager().getTexture(TextureAtlas.LOCATION_ITEMS);
        return tex instanceof TextureAtlas ta ? ta : null;
    }

    public static void swapTotemTexture(UUID playerUUID) {
        if (!initialized) return;
        if (vanillaTotemPixels == null) return;

        NativeImage totemImage = totemCache.get(playerUUID);
        if (totemImage == null) return;

        TextureAtlas itemsAtlas = getItemsAtlas();
        if (itemsAtlas == null) return;

        TextureAtlasSprite totemSprite = itemsAtlas.getSprite(TOTEM_ATLAS_ID);
        if (totemSprite == null) return;

        GpuTexture atlasTexture = ((AbstractTexture) itemsAtlas).getTexture();
        if (atlasTexture == null) return;

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.writeToTexture(atlasTexture, totemImage, 0,
                totemSprite.getX(), totemSprite.getY(), 0,
                totemImage.getWidth(), totemImage.getHeight(), 0, 0);
    }

    public static void restoreTotemTexture() {
        if (!initialized) return;
        if (vanillaTotemPixels == null) return;

        TextureAtlas itemsAtlas = getItemsAtlas();
        if (itemsAtlas == null) return;

        TextureAtlasSprite totemSprite = itemsAtlas.getSprite(TOTEM_ATLAS_ID);
        if (totemSprite == null) return;

        GpuTexture atlasTexture = ((AbstractTexture) itemsAtlas).getTexture();
        if (atlasTexture == null) return;

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.writeToTexture(atlasTexture, vanillaTotemPixels, 0,
                totemSprite.getX(), totemSprite.getY(), 0,
                vanillaTotemPixels.getWidth(), vanillaTotemPixels.getHeight(), 0, 0);
    }

    public static void prepareTotemTexture(UUID playerUUID, PlayerSkin skin, boolean slim) {
        if (totemCache.containsKey(playerUUID)) return;

        NativeImage skinImage = skinCache.get(playerUUID);
        if (skinImage == null) {
            ClientAsset.Texture bodyTexture = skin.body();
            if (bodyTexture instanceof ClientAsset.DownloadedTexture downloaded) {
                skinImage = downloadSkin(downloaded.url());
            }
            if (skinImage == null) {
                skinImage = createFallbackSkin();
            }
            skinCache.put(playerUUID, skinImage);
        }

        NativeImage totemImage = TotemTextureGenerator.generate(skinImage, slim);
        totemCache.put(playerUUID, totemImage);
    }

    private static NativeImage downloadSkin(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            byte[] data = response.body();
            return NativeImage.read(data);
        } catch (Exception e) {
            TotemSkin.LOGGER.warn("Failed to download skin from {}: {}", url, e.getMessage());
            return null;
        }
    }

    private static NativeImage createFallbackSkin() {
        NativeImage image = new NativeImage(64, 64, true);
        int skinColor = 0xFFA07850;
        int shirtColor = 0xFF00A0A0;
        int pantsColor = 0xFF303060;

        for (int x = 8; x < 16; x++) {
            for (int y = 8; y < 16; y++) {
                image.setPixel(x, y, skinColor);
            }
        }

        for (int x = 20; x < 28; x++) {
            for (int y = 20; y < 32; y++) {
                image.setPixel(x, y, shirtColor);
            }
        }

        for (int x = 4; x < 8; x++) {
            for (int y = 20; y < 32; y++) {
                image.setPixel(x, y, pantsColor);
            }
        }
        for (int x = 20; x < 24; x++) {
            for (int y = 20; y < 32; y++) {
                image.setPixel(x, y, pantsColor);
            }
        }

        for (int x = 44; x < 48; x++) {
            for (int y = 20; y < 32; y++) {
                image.setPixel(x, y, skinColor);
            }
        }
        for (int x = 36; x < 40; x++) {
            for (int y = 20; y < 32; y++) {
                image.setPixel(x, y, skinColor);
            }
        }

        return image;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static void deinit() {
        skinCache.clear();
        totemCache.clear();
        vanillaTotemPixels = null;
        initialized = false;
    }
}
