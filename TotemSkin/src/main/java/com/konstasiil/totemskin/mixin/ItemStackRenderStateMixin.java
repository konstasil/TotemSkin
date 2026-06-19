package com.konstasiil.totemskin.mixin;

import com.konstasiil.totemskin.TotemSkinState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

@Mixin(ItemStackRenderState.class)
public class ItemStackRenderStateMixin implements TotemSkinState {

    @Unique
    private UUID totemskin$playerUUID;

    @Override
    public UUID totemskin$getPlayerUUID() {
        return totemskin$playerUUID;
    }

    @Override
    public void totemskin$setPlayerUUID(UUID uuid) {
        totemskin$playerUUID = uuid;
    }
}
