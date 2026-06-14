package com.konstasiil.totemskin;

import net.minecraft.world.entity.player.PlayerSkin;

public class TotemRenderContext {

    private static final ThreadLocal<Integer> CURRENT_ENTITY_ID = ThreadLocal.withInitial(() -> -1);
    private static final ThreadLocal<PlayerSkin> CURRENT_SKIN = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> CURRENT_SLIM = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> IS_RENDERING_TOTEM = ThreadLocal.withInitial(() -> false);

    public static void set(int entityId, PlayerSkin skin, boolean slim) {
        CURRENT_ENTITY_ID.set(entityId);
        CURRENT_SKIN.set(skin);
        CURRENT_SLIM.set(slim);
    }

    public static int getEntityId() {
        return CURRENT_ENTITY_ID.get();
    }

    public static PlayerSkin getSkin() {
        return CURRENT_SKIN.get();
    }

    public static boolean isSlim() {
        return CURRENT_SLIM.get();
    }

    public static void setRenderingTotem(boolean value) {
        IS_RENDERING_TOTEM.set(value);
    }

    public static boolean isRenderingTotem() {
        return IS_RENDERING_TOTEM.get();
    }

    public static void clear() {
        CURRENT_ENTITY_ID.remove();
        CURRENT_SKIN.remove();
        CURRENT_SLIM.remove();
        IS_RENDERING_TOTEM.remove();
    }
}
