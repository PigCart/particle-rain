package pigcart.particlerain;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.IntUnaryOperator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.joml.Math;
import org.lwjgl.system.MemoryUtil;
import pigcart.particlerain.config.ConfigManager;
import pigcart.particlerain.config.ParticleData;
import pigcart.particlerain.mixin.access.NativeImageAccessor;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Locale;

public class TextureUtil {

    private static int highestAlpha;

    public static void boostAlpha(NativeImage img, String debugName) {
        highestAlpha = 0;
        applyToAllPixels(img, (int rgba) -> {
            Color col = new Color(rgba, true);
            highestAlpha = Math.max(col.getAlpha(), highestAlpha);
            return rgba;
        });
        if (highestAlpha == 255 || highestAlpha == 0) return;
        final int multiplier = 255 / highestAlpha;
        if (multiplier == 1) return;
        ParticleRain.LOGGER.info("Multiplying {} texture alpha by {}", debugName, multiplier);
        applyToAllPixels(img, (int rgba) -> {
            Color col = new Color(rgba, true);
            return (((col.getAlpha() * multiplier) & 0xFF) << 24) |
                    ((col.getRed() & 0xFF) << 16) |
                    ((col.getGreen() & 0xFF) << 8)  |
                    ((col.getBlue() & 0xFF));
        });
    }

    public static void desaturate(NativeImage img) {
        applyToAllPixels(img, (int rgba) -> {
            Color col = new Color(rgba, true);
            int gray = org.joml.Math.max(Math.max(col.getRed(), col.getGreen()), col.getBlue());
            return ((col.getAlpha() & 0xFF) << 24) |
                    ((gray & 0xFF) << 16) |
                    ((gray & 0xFF) << 8)  |
                    ((gray & 0xFF));
        });
    }

    // method removed from NativeImage in 1.21.5
    public static void applyToAllPixels(NativeImage image, IntUnaryOperator function) {
        if (image.format() != NativeImage.Format.RGBA) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "function application only works on RGBA images; have %s", image.format()));
        } else {
            ((NativeImageAccessor)(Object)image).callCheckAllocated();
            int i = image.getWidth() * image.getHeight();
            IntBuffer intBuffer = MemoryUtil.memIntBuffer(((NativeImageAccessor)(Object)image).getPixels(), i);

            for(int j = 0; j < i; ++j) {
                int k = argbToABGR(intBuffer.get(j));
                int l = function.applyAsInt(k);
                intBuffer.put(j, argbToABGR(l));
            }

        }
    }
    public static int argbToABGR(int i) {
        return i & -16711936 | (i & 16711680) >> 16 | (i & 255) << 16;
    }

    public static void applyWaterTint(SingleQuadParticle particle, ClientLevel level, BlockPos blockPos) {
        final ParticleData rain = ParticleLoader.particles.get("rain");
        rain.tintType.applyTint(particle, level, blockPos, rain);
    }

    public static NativeImage loadTexture(ResourceLocation location) throws IOException {
        return loadTexture(Minecraft.getInstance().getResourceManager().getResourceOrThrow(location));
    }

    public static NativeImage loadTexture(Resource resource) throws IOException {
        InputStream inputStream = resource.open();
        NativeImage nativeImage;
        try {
            nativeImage = NativeImage.read(inputStream);
        } catch (Throwable owo) {
            try {
                inputStream.close();
            } catch (Throwable uwu) {
                owo.addSuppressed(uwu);
            }
            throw owo;
        }
        inputStream.close();
        return nativeImage;
    }

    ///  Splits one sprite from a vertical sprite sheet
    public static SpriteContents getSpriteFromSheet(NativeImage spriteSheet, String id, int i, int columns) {
        int size = spriteSheet.getWidth() / columns;
        int rows = spriteSheet.getHeight() / size;
        // check bounds
        int max = rows * columns;
        if (i >= max) throw new IndexOutOfBoundsException("Cannot get " + id + i + " as sheet only contains " + max + " sprites");
        // create sprite
        NativeImage sprite = new NativeImage(size, size, false);
        int row = i % rows;
        int col = i / rows;
        try {
            spriteSheet.copyRect(sprite, size * col, size * row, 0, 0, size, size, true, true);
        } catch (Exception e) {
            ParticleRain.LOGGER.error("Error splitting {} texture", id + i, e);
        }
        return VersionUtil.newNonAnimatedSpriteContents(id + i, new FrameSize(size, size), sprite);
    }

    ///  Merges a vertical sprite sheet into a single sprite by alpha
    public static SpriteContents mergeSpritesFromSheet(NativeImage spriteSheet, String id, int startIndex, int spritesToMerge, int columns) {
        int size = spriteSheet.getWidth() / columns;
        int rows = spriteSheet.getHeight() / size;
        int endIndex = startIndex + spritesToMerge;
        // check bounds
        int sheetSize = rows * columns;
        if (endIndex > sheetSize) throw new IndexOutOfBoundsException("Cannot merge "+id+". Index "+endIndex+" out of bounds for sprite sheet of size "+sheetSize);
        // create merged sprite
        NativeImage sprite = new NativeImage(size, size, false);
        for (int i = startIndex; i < endIndex; i++) {
            int row = i % rows;
            int col = i / rows;
            try {
                copyRectHighestAlpha(spriteSheet, sprite, size * col, size * row, 0, 0, size, size, true, true);
            } catch (Exception e) {
                ParticleRain.LOGGER.error("Error merging {} texture", id + i, e);
            }
        }
        return VersionUtil.newNonAnimatedSpriteContents(id, new FrameSize(size, size), sprite);
    }

    ///  NativeImage.copyRect but pixels are only overwritten if they have a greater alpha value
    public static void copyRectHighestAlpha(NativeImage source, NativeImage target, int sourceX, int sourceY, int targetX, int targetY, int sizeX, int sizeY, boolean swapX, boolean swapY) {
        for(int y = 0; y < sizeY; ++y) {
            for(int x = 0; x < sizeX; ++x) {
                int dx = swapX ? sizeX - 1 - x : x;
                int dy = swapY ? sizeY - 1 - y : y;
                int src = getPixel(source, sourceX + x, sourceY + y);
                int trg = getPixel(source, targetX + dx, targetY + dy);
                int srcA = new Color(src, true).getAlpha();
                int trgA = new Color(trg, true).getAlpha();
                if (srcA > trgA) setPixel(target, targetX + dx, targetY + dy, src);
            }
        }
    }

    public static int getRippleResolution(List<SpriteContents> contents) {
        if (ConfigManager.getConfig().compat.rippleUsePackRes) {
            ResourceLocation resourceLocation = VersionUtil.getMcId("big_smoke_0");
            for (SpriteContents spriteContents : contents) {
                if (spriteContents.name().equals(resourceLocation)) {
                    //return Math.min(spriteContents.width(), 256); ...why does this not work?
                    if (spriteContents.width() < 256) {
                        return spriteContents.width();
                    } else {
                        return 256;
                    }
                }
            }
        }
        if (ConfigManager.getConfig().compat.rippleRes < 4)   ConfigManager.getConfig().compat.rippleRes = 4;
        if (ConfigManager.getConfig().compat.rippleRes > 256) ConfigManager.getConfig().compat.rippleRes = 256;
        return ConfigManager.getConfig().compat.rippleRes;
    }

    public static SpriteContents generateRipple(int i, int size) {
        float radius = ((size / 2F) / 8) * (i + 1);
        NativeImage image = new NativeImage(size, size, true);
        Color color = Color.WHITE;
        int colorint = ((color.getAlpha() & 0xFF) << 24) |
                ((color.getRed() & 0xFF) << 16) |
                ((color.getGreen() & 0xFF) << 8)  |
                ((color.getBlue() & 0xFF));
        generateBresenhamCircle(image, size, (int) Math.clamp(1, (size / 2F) - 1, radius), colorint);
        return VersionUtil.newNonAnimatedSpriteContents("ripple_" + i, new FrameSize(size, size), image);
    }

    public static void generateBresenhamCircle(NativeImage image, int imgSize, int radius, int colorint) {
        int centerX = imgSize / 2;
        int centerY = imgSize / 2;
        int x = 0, y = radius;
        int d = 3 - 2 * radius;
        drawCirclePixel(centerX, centerY, x, y, image, colorint);
        while (y >= x){
            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            }
            else
                d = d + 4 * x + 6;
            x++;
            drawCirclePixel(centerX, centerY, x, y, image, colorint);
        }
    }

    static void drawCirclePixel(int xc, int yc, int x, int y, NativeImage img, int col){
        setPixel(img, xc+x, yc+y, col);
        setPixel(img, xc-x, yc+y, col);
        setPixel(img, xc+x, yc-y, col);
        setPixel(img, xc-x, yc-y, col);
        setPixel(img, xc+y, yc+x, col);
        setPixel(img, xc-y, yc+x, col);
        setPixel(img, xc+y, yc-x, col);
        setPixel(img, xc-y, yc-x, col);
    }

    static void setPixel(NativeImage img, int x, int y, int col) {
        //~ if >1.21.1 'setPixelRGBA' -> 'setPixel'
        img.setPixelRGBA(x, y, col);
    }

    static int getPixel(NativeImage img, int x, int y) {
        //~ if >1.21.1 'getPixelRGBA' -> 'getPixel'
        return img.getPixelRGBA(x, y);
    }
}
