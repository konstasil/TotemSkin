package com.konstasiil.totemskin.mixin;

import com.konstasiil.totemskin.TotemTextureManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerItemInHandLayer.class)
public class PlayerItemInHandLayerMixin {

    @Inject(method = "submitArmWithItem", at = @At("HEAD"))
    private void totemskin$onSubmitArmWithItemHead(
            AvatarRenderState state,
            ItemStackRenderState itemRenderState,
            ItemStack stack,
            HumanoidArm arm,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            CallbackInfo ci) {

        if (stack.getItem() != Items.TOTEM_OF_UNDYING) return;
        if (itemRenderState.isEmpty()) return;

        var world = Minecraft.getInstance().level;
        if (world == null) return;

        var entity = world.getEntity(state.id);
        if (entity == null) return;

        boolean slim = state.skin.model() == PlayerModelType.SLIM;

        TotemTextureManager.prepareTotemTexture(entity.getUUID(), state.skin, slim);
        TotemTextureManager.swapTotemTexture(entity.getUUID());
    }

    @Inject(method = "submitArmWithItem", at = @At("RETURN"))
    private void totemskin$onSubmitArmWithItemReturn(
            AvatarRenderState state,
            ItemStackRenderState itemRenderState,
            ItemStack stack,
            HumanoidArm arm,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            CallbackInfo ci) {

        if (stack.getItem() != Items.TOTEM_OF_UNDYING) return;
        if (itemRenderState.isEmpty()) return;

        TotemTextureManager.restoreTotemTexture();
    }
}
