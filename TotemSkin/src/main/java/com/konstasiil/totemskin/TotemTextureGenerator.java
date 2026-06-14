package com.konstasiil.totemskin;

import com.mojang.blaze3d.platform.NativeImage;

public class TotemTextureGenerator {

    private static final int TOTEM_SIZE = 16;

    public static NativeImage generate(NativeImage skin, boolean slim) {
        NativeImage totem = new NativeImage(TOTEM_SIZE, TOTEM_SIZE, true);

        drawFace(totem, skin);
        drawBody(totem, skin);
        drawArms(totem, skin, slim);
        drawLegs(totem, skin);

        return totem;
    }

    private static void drawFace(NativeImage totem, NativeImage skin) {
        int faceW = 6;
        int faceH = 6;
        int destX = (TOTEM_SIZE - faceW) / 2;
        int destY = 0;
        copyRegion(totem, skin, 9, 9, destX, destY, faceW, faceH);
    }

    private static void drawBody(NativeImage totem, NativeImage skin) {
        int bodyW = 6;
        int bodyH = 6;
        int destX = (TOTEM_SIZE - bodyW) / 2;
        int destY = 6;
        copyRegion(totem, skin, 21, 21, destX, destY, bodyW, bodyH);
    }

    private static void drawArms(NativeImage totem, NativeImage skin, boolean slim) {
        int armW = slim ? 3 : 4;
        int armH = 6;
        int rightSrcX = 44;
        int leftSrcX = slim ? 36 : 44;

        copyRegion(totem, skin, rightSrcX, 5, 0, 7, armW, armH);
        copyRegion(totem, skin, leftSrcX, 5, TOTEM_SIZE - armW, 7, armW, armH);
    }

    private static void drawLegs(NativeImage totem, NativeImage skin) {
        int legW = 4;
        int legH = 4;

        copyRegion(totem, skin, 4, 20, 0, 12, legW, legH);
        copyRegion(totem, skin, 12, 20, TOTEM_SIZE - legW, 12, legW, legH);
    }

    private static void copyRegion(NativeImage dest, NativeImage src,
                                   int srcX, int srcY, int destX, int destY,
                                   int width, int height) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int sx = srcX + x;
                int sy = srcY + y;
                if (sx >= src.getWidth() || sy >= src.getHeight()) continue;
                int px = src.getPixel(sx, sy);
                if (((px >> 24) & 0xFF) == 0) continue;
                int dx = destX + x;
                int dy = destY + y;
                if (dx >= TOTEM_SIZE || dy >= TOTEM_SIZE) continue;
                dest.setPixel(dx, dy, px);
            }
        }
    }
}
