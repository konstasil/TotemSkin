package com.konstasiil.totemskin.mixin;

import com.konstasiil.totemskin.CachedSkinPixels;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(DynamicTexture.class)
public class DynamicTextureMixin implements CachedSkinPixels {

    @Unique
    private NativeImage totemskin$cachedPixels;

    @Inject(method = "<init>(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/platform/NativeImage;)V", at = @At("RETURN"))
    private void totemskin$cachePixelsOnInit(Supplier<String> path, NativeImage image, CallbackInfo ci) {
        if (image != null && !image.isClosed()) {
            this.totemskin$cachedPixels = new NativeImage(image.getWidth(), image.getHeight(), true);
            this.totemskin$cachedPixels.copyFrom(image);
        }
    }

    @Override
    public NativeImage totemskin$getCachedPixels() {
        if (this.totemskin$cachedPixels != null && !this.totemskin$cachedPixels.isClosed()) {
            return this.totemskin$cachedPixels;
        }
        return null;
    }
}
