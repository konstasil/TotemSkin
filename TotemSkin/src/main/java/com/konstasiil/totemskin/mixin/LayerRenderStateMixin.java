package com.konstasiil.totemskin.mixin;

import com.konstasiil.totemskin.TotemSkinState;
import com.konstasiil.totemskin.TotemTextureManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mixin(targets = "net.minecraft.client.renderer.item.ItemStackRenderState$LayerRenderState")
public class LayerRenderStateMixin {

    @Shadow
    @Final
    private List<BakedQuad> quads;

    @Shadow
    @Final
    ItemStackRenderState this$0;

    @Inject(method = "submit", at = @At("HEAD"))
    private void totemskin$onSubmitHead(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            int overlayCoords,
            int outlineColor,
            CallbackInfo ci) {

        UUID player = ((TotemSkinState) this$0).totemskin$getPlayerUUID();
        if (player == null) return;

        if (!TotemTextureManager.isInitialized()) return;

        float[] offset = TotemTextureManager.getUVOffset(player);
        if (offset == null) return;

        float offU = offset[0];
        float offV = offset[1];
        if (offU == 0 && offV == 0) return;

        int size = this.quads.size();
        List<BakedQuad> newQuads = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            BakedQuad q = this.quads.get(i);
            newQuads.add(new BakedQuad(
                    q.position0(), q.position1(), q.position2(), q.position3(),
                    totemskin$remapUV(q.packedUV0(), offU, offV),
                    totemskin$remapUV(q.packedUV1(), offU, offV),
                    totemskin$remapUV(q.packedUV2(), offU, offV),
                    totemskin$remapUV(q.packedUV3(), offU, offV),
                    q.direction(), q.materialInfo()));
        }
        this.quads.clear();
        this.quads.addAll(newQuads);
    }

    private static long totemskin$remapUV(long packed, float offU, float offV) {
        return UVPair.pack(UVPair.unpackU(packed) + offU, UVPair.unpackV(packed) + offV);
    }
}
