package com.konstasiil.totemskin.mixin;

import com.konstasiil.totemskin.TotemSkinConfig;
import com.konstasiil.totemskin.TotemSkinState;
import com.konstasiil.totemskin.TotemTextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CuboidItemModelWrapper.class)
public class CuboidItemModelWrapperMixin {

    @Inject(method = "update", at = @At("HEAD"))
    private void totemskin$onUpdateHead(
            ItemStackRenderState output,
            ItemStack item,
            ItemModelResolver resolver,
            ItemDisplayContext displayContext,
            @Nullable ClientLevel level,
            @Nullable ItemOwner owner,
            int seed,
            CallbackInfo ci) {

        ((TotemSkinState) output).totemskin$setPlayerUUID(null);

        if (item.getItem() != Items.TOTEM_OF_UNDYING) return;

        if (displayContext == ItemDisplayContext.GUI && !TotemSkinConfig.get().showInInventory) return;

        AbstractClientPlayer player = null;
        if (owner != null && owner.asLivingEntity() instanceof AbstractClientPlayer p) {
            player = p;
        } else if (displayContext == ItemDisplayContext.FIXED) {
            player = Minecraft.getInstance().player;
        }

        if (player == null) return;

        if (TotemSkinConfig.get().getModeForPlayer(player.getUUID()) == TotemSkinConfig.TotemMode.VANILLA) return;

        TotemTextureManager.prepareAndWrite(
                player.getUUID(),
                player.getSkin(),
                player.getSkin().model() == PlayerModelType.SLIM);

        ((TotemSkinState) output).totemskin$setPlayerUUID(player.getUUID());
    }
}
