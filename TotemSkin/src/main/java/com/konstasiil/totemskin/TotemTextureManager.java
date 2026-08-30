package com.konstasiil.totemskin;

import com.konstasiil.totemskin.CachedSkinPixels;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.phys.Vec3;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class TotemTextureManager {

    private static final Identifier TOTEM_ATLAS_ID = Identifier.fromNamespaceAndPath("minecraft", "item/totem_of_undying");
    private static final int MAX_PLAYER_SLOTS = 8;
    private static final int TOTEM_TEX_W = 16;
    private static final int TOTEM_TEX_H = 32;

    private static float vanillaU = 0;
    private static float vanillaV = 0;
    private static int atlasWidth = 0;
    private static int atlasHeight = 0;

    private static final Map<UUID, int[]> playerSlots = new ConcurrentHashMap<>();
    private static final Map<UUID, float[]> playerUVOffsets = new ConcurrentHashMap<>();
    private static final Map<UUID, NativeImage> skinCache = new ConcurrentHashMap<>();
    private static final Map<UUID, NativeImage> totemCache = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> pendingDownloads = new ConcurrentHashMap<>();
    private static final List<int[]> freeSlots = new ArrayList<>();

    private static boolean initialized = false;
    private static boolean initAttempted = false;

    private static int logCounter = 0;

    public static void init() {
        if (initialized || initAttempted) return;
        initAttempted = true;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getTextureManager() == null) {
            initAttempted = false;
            return;
        }

        TextureAtlas itemsAtlas = getItemsAtlas();
        if (itemsAtlas == null) {
            initAttempted = false;
            return;
        }

        TextureAtlasSprite totemSprite = itemsAtlas.getSprite(TOTEM_ATLAS_ID);
        if (totemSprite == null) {
            TotemSkin.LOGGER.warn("TotemSkin: totem sprite not found");
            return;
        }

        GpuTexture atlasTex = ((AbstractTexture) itemsAtlas).getTexture();
        if (atlasTex == null) {
            TotemSkin.LOGGER.warn("TotemSkin: atlas GPU texture null");
            return;
        }

        atlasWidth = atlasTex.getWidth(0);
        atlasHeight = atlasTex.getHeight(0);

        vanillaU = (float) totemSprite.getX() / atlasWidth;
        vanillaV = (float) totemSprite.getY() / atlasHeight;

        findFreeSlots(itemsAtlas);

        initialized = true;
        TotemSkin.LOGGER.info("TotemSkin init OK: atlas={}x{} vanilla=({},{}) freeSlots={}",
                atlasWidth, atlasHeight, totemSprite.getX(), totemSprite.getY(), freeSlots.size());
    }

    private static void findFreeSlots(TextureAtlas atlas) {
        freeSlots.clear();
        List<int[]> occupied = new ArrayList<>();

        try {
            var spritesField = TextureAtlas.class.getDeclaredField("sprites");
            spritesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<TextureAtlasSprite> sprites = (List<TextureAtlasSprite>) spritesField.get(atlas);

            for (TextureAtlasSprite sprite : sprites) {
                if (sprite == null) continue;
                int sx = sprite.getX();
                int sy = sprite.getY();
                int sw = sprite.contents().width();
                int sh = sprite.contents().height();
                occupied.add(new int[]{sx, sy, sx + sw, sy + sh});
            }
        } catch (Exception e) {
            TotemSkin.LOGGER.warn("TotemSkin: could not read sprites for slot search");
            return;
        }

        for (int tryY = 0; tryY <= atlasHeight - TOTEM_TEX_H && freeSlots.size() < MAX_PLAYER_SLOTS; tryY += TOTEM_TEX_H) {
            for (int tryX = 0; tryX <= atlasWidth - TOTEM_TEX_W && freeSlots.size() < MAX_PLAYER_SLOTS; tryX += TOTEM_TEX_W) {
                boolean fits = true;
                for (int[] r : occupied) {
                    if (tryX < r[2] && tryX + TOTEM_TEX_W > r[0] && tryY < r[3] && tryY + TOTEM_TEX_H > r[1]) {
                        fits = false;
                        break;
                    }
                }
                if (fits) {
                    freeSlots.add(new int[]{tryX, tryY});
                }
            }
        }
    }

    private static void ensureInitialized() {
        if (!initialized) init();
    }

    public static TextureAtlas getItemsAtlas() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getTextureManager() == null) return null;
        var tex = mc.getTextureManager().getTexture(TextureAtlas.LOCATION_ITEMS);
        return tex instanceof TextureAtlas ta ? ta : null;
    }

    public static void prepareAndWrite(UUID playerUUID, PlayerSkin skin, boolean slim) {
        ensureInitialized();
        if (!initialized) return;

        prepareTotemTexture(playerUUID, skin, slim);

        if (!totemCache.containsKey(playerUUID)) return;

        int[] slot = playerSlots.get(playerUUID);
        if (slot == null) {
            if (freeSlots.isEmpty()) {
                evictFarthestPlayer();
            }
            if (freeSlots.isEmpty()) {
                if (logCounter < 5) {
                    TotemSkin.LOGGER.warn("TotemSkin: no free slots for player={}", playerUUID);
                    logCounter++;
                }
                return;
            }
            slot = freeSlots.remove(0);
            playerSlots.put(playerUUID, slot);

            float offU = (float) slot[0] / atlasWidth - vanillaU;
            float offV = (float) slot[1] / atlasHeight - vanillaV;
            playerUVOffsets.put(playerUUID, new float[]{offU, offV});

            TotemSkin.LOGGER.info("TotemSkin: assigned slot ({},{}) to player={} offset=({},{})",
                    slot[0], slot[1], playerUUID, offU, offV);
        }

        writeCustomToAtlas(playerUUID, slot);
    }

    public static float[] getUVOffset(UUID playerUUID) {
        return playerUVOffsets.get(playerUUID);
    }

    public static void onPlayerDisconnect(UUID uuid) {
        freePlayerSlot(uuid);
    }

    private static void freePlayerSlot(UUID uuid) {
        int[] slot = playerSlots.remove(uuid);
        if (slot != null) {
            freeSlots.add(slot);
        }
        playerUVOffsets.remove(uuid);

        NativeImage oldSkin = skinCache.remove(uuid);
        if (oldSkin != null && !oldSkin.isClosed()) {
            oldSkin.close();
        }

        NativeImage oldTotem = totemCache.remove(uuid);
        if (oldTotem != null && !oldTotem.isClosed()) {
            oldTotem.close();
        }

        pendingDownloads.remove(uuid);

        TotemSkin.LOGGER.info("TotemSkin: freed slot for player={}", uuid);
    }

    private static void evictFarthestPlayer() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) return;

        LocalPlayer localPlayer = mc.player;
        if (localPlayer == null) return;

        Vec3 localPos = localPlayer.position();

        UUID farthestUUID = null;
        double maxDistSq = -1;

        for (UUID uuid : playerSlots.keySet()) {
            double distSq = getDistanceSqToPlayer(uuid, localPos);
            if (distSq > maxDistSq) {
                maxDistSq = distSq;
                farthestUUID = uuid;
            }
        }

        if (farthestUUID != null) {
            double blocks = Math.sqrt(maxDistSq);
            TotemSkin.LOGGER.info("TotemSkin: evicting farthest player={} dist={} blocks", farthestUUID, (int) blocks);
            freePlayerSlot(farthestUUID);
        }
    }

    private static double getDistanceSqToPlayer(UUID targetUUID, Vec3 localPos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) return Double.MAX_VALUE;

        ClientLevel level = mc.level;
        for (AbstractClientPlayer p : level.players()) {
            if (p.getUUID().equals(targetUUID)) {
                return p.position().distanceToSqr(localPos);
            }
        }
        return Double.MAX_VALUE;
    }

    private static void writeCustomToAtlas(UUID playerUUID, int[] slot) {
        NativeImage totemImage = totemCache.get(playerUUID);
        if (totemImage == null) return;

        TextureAtlas itemsAtlas = getItemsAtlas();
        if (itemsAtlas == null) return;

        GpuTexture atlasTexture = ((AbstractTexture) itemsAtlas).getTexture();
        if (atlasTexture == null) return;

        try {
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            encoder.writeToTexture(atlasTexture, totemImage, 0,
                    0, slot[0], slot[1],
                    Math.min(totemImage.getWidth(), TOTEM_TEX_W),
                    Math.min(totemImage.getHeight(), TOTEM_TEX_H), 0, 0);
        } catch (Exception e) {
            TotemSkin.LOGGER.warn("TotemSkin: write to atlas failed: {}", e.getMessage());
        }
    }

    private static void prepareTotemTexture(UUID playerUUID, PlayerSkin skin, boolean slim) {
        if (totemCache.containsKey(playerUUID)) return;

        NativeImage skinImage = skinCache.get(playerUUID);
        if (skinImage == null) {
            skinImage = getSkinNativeImage(playerUUID, skin);
            if (skinImage == null) return;
            skinCache.put(playerUUID, skinImage);
        }

        NativeImage totemImage = TotemTextureGenerator.generate(skinImage, slim);
        totemCache.put(playerUUID, totemImage);
    }

    private static NativeImage getSkinNativeImage(UUID playerUUID, PlayerSkin skin) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getTextureManager() == null) return null;

        try {
            Identifier texturePath = skin.body().texturePath();

            if (logCounter < 10) {
                TotemSkin.LOGGER.info("TotemSkin: skin lookup: player={} path={}", playerUUID, texturePath);
            }

            AbstractTexture abstractTexture = mc.getTextureManager().getTexture(texturePath);

            if (abstractTexture instanceof DynamicTexture dynamicTexture) {
                NativeImage source = dynamicTexture.getPixels();
                if (source != null && !source.isClosed()) {
                    NativeImage copy = new NativeImage(source.getWidth(), source.getHeight(), true);
                    copy.copyFrom(source);
                    return copy;
                }

                if (abstractTexture instanceof CachedSkinPixels cached) {
                    NativeImage cachedPixels = cached.totemskin$getCachedPixels();
                    if (cachedPixels != null && !cachedPixels.isClosed()) {
                        NativeImage copy = new NativeImage(cachedPixels.getWidth(), cachedPixels.getHeight(), true);
                        copy.copyFrom(cachedPixels);
                        return copy;
                    }
                }

                if (logCounter < 5) {
                    TotemSkin.LOGGER.warn("TotemSkin: DynamicTexture pixels null/closed for player={}", playerUUID);
                }
            }

            if (abstractTexture != null) {
                for (Class<?> clazz = abstractTexture.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
                    for (var declared : clazz.getDeclaredFields()) {
                        if (declared.getType() == NativeImage.class) {
                            declared.setAccessible(true);
                            Object val = declared.get(abstractTexture);
                            if (val instanceof NativeImage ni && !ni.isClosed()) {
                                NativeImage copy = new NativeImage(ni.getWidth(), ni.getHeight(), true);
                                copy.copyFrom(ni);
                                return copy;
                            }
                        }
                    }
                }
            }

            NativeImage fromResource = loadSkinFromResources(mc, texturePath);
            if (fromResource != null) {
                return fromResource;
            }

            startAsyncDownload(playerUUID, texturePath);
        } catch (Exception e) {
            TotemSkin.LOGGER.warn("TotemSkin: skin error: {}", e.getMessage());
        }
        return null;
    }

    private static NativeImage loadSkinFromResources(Minecraft mc, Identifier texturePath) {
        try {
            if (mc.getResourceManager().getResource(texturePath).isEmpty()) {
                return null;
            }
            InputStream is = mc.getResourceManager().open(texturePath);
            NativeImage image = NativeImage.read(is);
            is.close();
            if (image != null && !image.isClosed()) {
                if (logCounter < 5) {
                    TotemSkin.LOGGER.info("TotemSkin: loaded skin from resources {}x{} path={}",
                            image.getWidth(), image.getHeight(), texturePath);
                }
                return image;
            }
        } catch (Exception e) {
            if (logCounter < 5) {
                TotemSkin.LOGGER.warn("TotemSkin: resource load failed: {}", e.getMessage());
            }
        }
        return null;
    }

    private static void startAsyncDownload(UUID playerUUID, Identifier texturePath) {
        if (pendingDownloads.containsKey(playerUUID)) return;

        String path = texturePath.getPath();
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash < 0) return;
        String hash = path.substring(lastSlash + 1);
        if (hash.isEmpty()) return;

        pendingDownloads.put(playerUUID, Boolean.TRUE);

        if (logCounter < 5) {
            TotemSkin.LOGGER.info("TotemSkin: async download for player={} hash={}", playerUUID, hash);
        }

        CompletableFuture.runAsync(() -> {
            try {
                String urlString = "https://textures.minecraft.net/texture/" + hash;
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() != 200) {
                    TotemSkin.LOGGER.warn("TotemSkin: download failed code={}", conn.getResponseCode());
                    return;
                }

                InputStream inputStream = conn.getInputStream();
                NativeImage image = NativeImage.read(inputStream);
                inputStream.close();
                conn.disconnect();

                if (image != null && !image.isClosed()) {
                    skinCache.put(playerUUID, image);
                    TotemSkin.LOGGER.info("TotemSkin: async download OK {}x{} for player={}",
                            image.getWidth(), image.getHeight(), playerUUID);
                }
            } catch (Exception e) {
                TotemSkin.LOGGER.warn("TotemSkin: async download error: {}", e.getMessage());
            } finally {
                pendingDownloads.remove(playerUUID);
            }
        });
    }

    public static boolean isInitialized() { return initialized; }

    public static void deinit() {
        for (NativeImage img : skinCache.values()) {
            if (img != null && !img.isClosed()) img.close();
        }
        for (NativeImage img : totemCache.values()) {
            if (img != null && !img.isClosed()) img.close();
        }
        skinCache.clear();
        totemCache.clear();
        playerSlots.clear();
        playerUVOffsets.clear();
        pendingDownloads.clear();
        freeSlots.clear();
        initialized = false;
        initAttempted = false;
    }
}
