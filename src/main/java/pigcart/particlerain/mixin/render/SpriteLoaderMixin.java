package pigcart.particlerain.mixin.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.VersionUtil;
import pigcart.particlerain.TextureUtil;
import pigcart.particlerain.config.ConfigManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;

@Mixin(SpriteLoader.class)
public abstract class SpriteLoaderMixin {

    @Shadow @Final private ResourceLocation location;

    @Unique
    List<SpriteContents> spriteContentsList;

    @Inject(method = "stitch", at = @At("HEAD"))
    public void stitch(List<SpriteContents> list, int i, Executor executor, CallbackInfoReturnable<SpriteLoader.Preparations> cir) {
        if (this.location.equals(VersionUtil.getMcId("textures/atlas/particles.png"))) {
            this.spriteContentsList = list;
        }
    }

    @ModifyExpressionValue(
            method = "stitch",
            at = @At(value = "NEW", args = "class=net/minecraft/client/renderer/texture/Stitcher")
    )
    private Stitcher<SpriteContents> registerWeatherParticleSprites(Stitcher<SpriteContents> stitcher) {
        if (this.location.equals(VersionUtil.getMcId("textures/atlas/particles.png"))) {
            // load weather textures
            NativeImage rainImage = null;
            NativeImage snowImage = null;
            try {
                rainImage = TextureUtil.loadTexture(VersionUtil.getMcId("textures/environment/rain.png"));
                snowImage = TextureUtil.loadTexture(VersionUtil.getMcId("textures/environment/snow.png"));
                TextureUtil.boostAlpha(rainImage, "rain");
                TextureUtil.boostAlpha(snowImage, "snow");
                if (ConfigManager.config.compat.waterTint) TextureUtil.desaturate(rainImage);
            } catch (IOException e) {
                ParticleRain.LOGGER.error("Error loading weather textures: ", e);
            }

            // split both weather textures into four sprites
            for (int i = 0; i < 4; i++) {
                stitcher.registerSprite(TextureUtil.splitImage(rainImage, i, "rain_"));
            }
            for (int i = 0; i < 4; i++) {
                stitcher.registerSprite(TextureUtil.splitImage(snowImage, i, "snow_"));
            }
            // generate ripple sprites
            int rippleResolution = TextureUtil.getRippleResolution(this.spriteContentsList);
            for (int i = 0; i < 8; i++) {
                stitcher.registerSprite(TextureUtil.generateRipple(i, rippleResolution));
            }
            // create gray versions of the default splashes so tint can be applied
            if (ConfigManager.config.compat.waterTint) {
                for (int i = 0; i < 4; i++) {
                    try {
                        stitcher.registerSprite(VersionUtil.loadSplashSprite(i));
                    } catch (IOException e) {
                        ParticleRain.LOGGER.error("Error loading splash particle {}: ", i, e);
                    }
                }
            }
        }
        return stitcher;
    }
}
