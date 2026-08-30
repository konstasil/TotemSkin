package com.konstasiil.totemskin;

import com.mojang.blaze3d.platform.NativeImage;

public class TotemTextureGenerator {

    private static final int TOTEM_SIZE = 16;

    public static NativeImage generate(NativeImage skin, boolean slim) {
        NativeImage out = new NativeImage(TOTEM_SIZE, TOTEM_SIZE, true);
        clear(out);

        boolean hasLeftLimbs = hasNonTransparentPixels(skin, 20, 52, 24, 64);

        copy(skin, out, 8, 8, 8, 8, 4, 1);

        copy(skin, out, 20, 21, 8, 1, 4, 9);
        copy(skin, out, 20, 23, 8, 1, 4, 10);
        copy(skin, out, 20, 29, 8, 1, 4, 11);
        copy(skin, out, 20, 31, 8, 1, 4, 12);

        copy(skin, out, 5, 20, 3, 2, 5, 13);
        copy(skin, out, 6, 31, 2, 1, 6, 15);

        if (hasLeftLimbs) {
            copy(skin, out, 20, 52, 3, 2, 8, 13);
            copy(skin, out, 20, 63, 2, 1, 8, 15);
        } else {
            copy(skin, out, 7, 20, 3, 2, 8, 13);
            copy(skin, out, 7, 31, 2, 1, 8, 15);
        }

        copy(skin, out, 44, 20, 1, 1, 3, 8);
        copy(skin, out, 45, 20, 1, 1, 3, 9);
        copy(skin, out, 46, 20, 1, 1, 3, 10);
        copy(skin, out, 44, 21, 1, 1, 2, 8);
        copy(skin, out, 45, 21, 1, 1, 2, 9);
        copy(skin, out, 46, 21, 1, 1, 2, 10);
        copy(skin, out, 44, 31, 1, 1, 1, 8);
        copy(skin, out, 45, 31, 1, 1, 1, 9);

        if (hasLeftLimbs) {
            copy(skin, out, 39, 52, 1, 1, 12, 8);
            copy(skin, out, 38, 52, 1, 1, 12, 9);
            copy(skin, out, 37, 52, 1, 1, 12, 10);
            copy(skin, out, 39, 53, 1, 1, 13, 8);
            copy(skin, out, 38, 53, 1, 1, 13, 9);
            copy(skin, out, 37, 53, 1, 1, 13, 10);
            copy(skin, out, 37, 63, 1, 1, 14, 8);
            copy(skin, out, 38, 63, 1, 1, 14, 9);
        } else {
            copy(skin, out, 44, 20, 1, 1, 12, 8);
            copy(skin, out, 45, 20, 1, 1, 12, 9);
            copy(skin, out, 46, 20, 1, 1, 12, 10);
            copy(skin, out, 44, 21, 1, 1, 13, 8);
            copy(skin, out, 45, 21, 1, 1, 13, 9);
            copy(skin, out, 46, 21, 1, 1, 13, 10);
            copy(skin, out, 46, 31, 1, 1, 14, 8);
            copy(skin, out, 45, 31, 1, 1, 14, 9);
        }

        copyOverlay(skin, out, 40, 8, 8, 8, 4, 1);
        clearPixel(out, 4, 1);
        clearPixel(out, 11, 1);

        return out;
    }

    private static boolean hasNonTransparentPixels(NativeImage img, int x1, int y1, int x2, int y2) {
        if (img.getHeight() <= 32) return false;
        for (int x = x1; x < x2; x++) {
            for (int y = y1; y < y2; y++) {
                if (x < img.getWidth() && y < img.getHeight()) {
                    int px = img.getPixel(x, y);
                    if (((px >> 24) & 0xFF) != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void copy(NativeImage src, NativeImage dst,
                             int sx, int sy, int w, int h, int dx, int dy) {
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int sxx = sx + x;
                int syy = sy + y;
                if (sxx < src.getWidth() && syy < src.getHeight()) {
                    int dxx = dx + x;
                    int dyy = dy + y;
                    if (dxx < TOTEM_SIZE && dyy < TOTEM_SIZE) {
                        dst.setPixel(dxx, dyy, src.getPixel(sxx, syy));
                    }
                }
            }
        }
    }

    private static void copyOverlay(NativeImage src, NativeImage dst,
                                    int sx, int sy, int w, int h, int dx, int dy) {
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int sxx = sx + x;
                int syy = sy + y;
                if (sxx < src.getWidth() && syy < src.getHeight()) {
                    int px = src.getPixel(sxx, syy);
                    if (((px >> 24) & 0xFF) != 0) {
                        int dxx = dx + x;
                        int dyy = dy + y;
                        if (dxx < TOTEM_SIZE && dyy < TOTEM_SIZE) {
                            dst.setPixel(dxx, dyy, px);
                        }
                    }
                }
            }
        }
    }

    private static void clear(NativeImage img) {
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                img.setPixel(x, y, 0x00000000);
            }
        }
    }

    private static void clearPixel(NativeImage img, int x, int y) {
        if (x < img.getWidth() && y < img.getHeight()) {
            img.setPixel(x, y, 0x00000000);
        }
    }
}
