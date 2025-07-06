package pigcart.particlerain;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.IntUnaryOperator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import org.joml.Math;
import org.lwjgl.system.MemoryUtil;
import pigcart.particlerain.config.ModConfig;
import pigcart.particlerain.mixin.access.NativeImageAccessor;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Locale;

public class TextureUtil {

    public static IntUnaryOperator desaturateOperation = (int rgba) -> {
        Color col = new Color(rgba, true);
        int gray = org.joml.Math.max(Math.max(col.getRed(), col.getGreen()), col.getBlue());
        return ((col.getAlpha() & 0xFF) << 24) |
                ((gray & 0xFF) << 16) |
                ((gray & 0xFF) << 8)  |
                ((gray & 0xFF));
    };

    // method removed from NativeImage in 1.21.5
    public static void applyToAllPixels(java.util.function.IntUnaryOperator function, NativeImage image) {
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

    public static void applyWaterTint(TextureSheetParticle particle, ClientLevel clientLevel, BlockPos blockPos) {
        // IrisApi.isShaderPackInUse()
        final Color waterColor = new Color(BiomeColors.getAverageWaterColor(clientLevel, blockPos));
        final Color fogColor = new Color(clientLevel.getBiome(blockPos).value().getFogColor());
        float rCol = (Mth.lerp(ModConfig.CONFIG.compat.tintMix, waterColor.getRed(), fogColor.getRed()) / 255F);
        float gCol = (Mth.lerp(ModConfig.CONFIG.compat.tintMix, waterColor.getGreen(), fogColor.getGreen()) / 255F);
        float bCol = (Mth.lerp(ModConfig.CONFIG.compat.tintMix, waterColor.getBlue(), fogColor.getBlue()) / 255F);
        particle.setColor(rCol, gCol, bCol);
    }

    public static NativeImage loadTexture(ResourceLocation resourceLocation) throws IOException {
        Resource resource = Minecraft.getInstance().getResourceManager().getResourceOrThrow(resourceLocation);
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

    public static SpriteContents splitImage(NativeImage image, int segment, String id) {
        int size = image.getWidth();
        NativeImage sprite = new NativeImage(size, size, false);
        image.copyRect(sprite, 0, size * segment, 0, 0, size, size, true, true);
        return(new SpriteContents(StonecutterUtil.getResourceLocation(ParticleRain.MOD_ID, id + segment), new FrameSize(size, size), sprite, StonecutterUtil.getSpriteMetadata()));
    }

    public static int getRippleResolution(List<SpriteContents> contents) {
        if (ModConfig.CONFIG.ripple.useResourcepackResolution) {
            ResourceLocation resourceLocation = StonecutterUtil.getResourceLocation("big_smoke_0");
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
        if (ModConfig.CONFIG.ripple.resolution < 4) ModConfig.CONFIG.ripple.resolution = 4;
        if (ModConfig.CONFIG.ripple.resolution > 256) ModConfig.CONFIG.ripple.resolution = 256;
        return ModConfig.CONFIG.ripple.resolution;
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
        return(new SpriteContents(StonecutterUtil.getResourceLocation(ParticleRain.MOD_ID, "ripple_" + i), new FrameSize(size, size), image, StonecutterUtil.getSpriteMetadata()));
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
        //? if >1.21.1 {
        /*img.setPixel(xc+x, yc+y, col);
        img.setPixel(xc-x, yc+y, col);
        img.setPixel(xc+x, yc-y, col);
        img.setPixel(xc-x, yc-y, col);
        img.setPixel(xc+y, yc+x, col);
        img.setPixel(xc-y, yc+x, col);
        img.setPixel(xc+y, yc-x, col);
        img.setPixel(xc-y, yc-x, col);
        *///?} else {
        img.setPixelRGBA(xc+x, yc+y, col);
        img.setPixelRGBA(xc-x, yc+y, col);
        img.setPixelRGBA(xc+x, yc-y, col);
        img.setPixelRGBA(xc-x, yc-y, col);
        img.setPixelRGBA(xc+y, yc+x, col);
        img.setPixelRGBA(xc-y, yc+x, col);
        img.setPixelRGBA(xc+y, yc-x, col);
        img.setPixelRGBA(xc-y, yc-x, col);
        //?}
    }
}
