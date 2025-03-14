package pigcart.particlerain.mixin.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pigcart.particlerain.ParticleRainClient;
import pigcart.particlerain.Util;
import pigcart.particlerain.config.ModConfig;

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
        this.spriteContentsList = list;
    }

    @ModifyExpressionValue(
            method = "stitch",
            at = @At(value = "NEW", args = "class=net/minecraft/client/renderer/texture/Stitcher")
    )
    private Stitcher<SpriteContents> registerWeatherParticleSprites(Stitcher<SpriteContents> stitcher) {
        if (this.location.equals(ResourceLocation.withDefaultNamespace("textures/atlas/particles.png"))) {
            // resource reload clears all particles. we can just reset the counter here instead of registering a listener.
            ParticleRainClient.resetParticleCount();

            // load weather textures
            NativeImage rainImage = null;
            NativeImage snowImage = null;
            try {
                rainImage = Util.loadTexture(ResourceLocation.withDefaultNamespace("textures/environment/rain.png"));
                snowImage = Util.loadTexture(ResourceLocation.withDefaultNamespace("textures/environment/snow.png"));
                if (ModConfig.CONFIG.compat.biomeTint) rainImage.applyToAllPixels(Util.desaturateOperation);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // split both weather textures into four sprites
            for (int i = 0; i < 4; i++) {
                stitcher.registerSprite(Util.splitImage(rainImage, i, "rain"));
            }
            for (int i = 0; i < 4; i++) {
                stitcher.registerSprite(Util.splitImage(snowImage, i, "snow"));
            }
            // generate ripple sprites
            int rippleResolution = Util.getRippleResolution(this.spriteContentsList);
            for (int i = 0; i < 8; i++) {
                stitcher.registerSprite(Util.generateRipple(i, rippleResolution));
            }
            // create gray versions of the default splashes so tint can be applied
            if (ModConfig.CONFIG.compat.biomeTint) {
                for (int i = 0; i < 4; i++) {
                    NativeImage splashImage = null;
                    try {
                        splashImage = Util.loadTexture(ResourceLocation.withDefaultNamespace("textures/particle/splash_" + i + ".png"));
                        splashImage.applyToAllPixels(Util.desaturateOperation);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    stitcher.registerSprite(new SpriteContents(ResourceLocation.fromNamespaceAndPath(ParticleRainClient.MOD_ID, "splash" + i), new FrameSize(splashImage.getWidth(), splashImage.getHeight()), splashImage, new ResourceMetadata.Builder().build()));
                }
            }
        }
        return stitcher;
    }
}
