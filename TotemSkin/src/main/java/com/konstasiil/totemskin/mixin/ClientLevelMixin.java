package com.konstasiil.totemskin.mixin;

import com.konstasiil.totemskin.TotemTextureManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Inject(method = "removeEntity", at = @At("HEAD"))
    private void totemskin$onRemoveEntity(int entityId, Entity.RemovalReason reason, CallbackInfo ci) {
        ClientLevel level = (ClientLevel) (Object) this;
        Entity entity = level.getEntity(entityId);
        if (entity instanceof AbstractClientPlayer player) {
            TotemTextureManager.onPlayerDisconnect(player.getUUID());
        }
    }
}
