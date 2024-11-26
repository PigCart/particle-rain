package pigcart.particlerain.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.particlerain.ParticleRainClient;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import static pigcart.particlerain.ParticleRainClient.RAIN_TEXTURE;
import static pigcart.particlerain.ParticleRainClient.SNOW_TEXTURE;

@Mixin(Stitcher.class)
public abstract class StitcherMixin<T extends Stitcher.Entry> {

    @Shadow public abstract void registerSprite(Stitcher.Entry entry);

    @Shadow @Final private List<Stitcher.Holder<T>> texturesToBeStitched;

    // environment textures arent atlassed so we must add them to the atlas ourselves!
    @Inject(method = "stitch", at = @At("HEAD"))
    public void stitch(CallbackInfo ci) {
        // check for the existence of particle rain textures on this atlas to ensure we're only adding to the particle atlas
        Predicate<? super Stitcher.Holder<T>> predicate = h -> h.entry().name().getNamespace().equals(ParticleRainClient.MOD_ID);
        if (this.texturesToBeStitched.stream().anyMatch(predicate)) {

            NativeImage rainImage = null;
            NativeImage snowImage = null;
            try {
                rainImage = ParticleRainClient.loadTexture(RAIN_TEXTURE);
                snowImage = ParticleRainClient.loadTexture(SNOW_TEXTURE);
                if (ParticleRainClient.config.rain.biomeTint)
                    rainImage.applyToAllPixels(ParticleRainClient.desaturateOperation);
            } catch (IOException e) {
                e.printStackTrace();
            }

            var rainSpriteContents = new SpriteContents(ParticleRainClient.RAIN_SPRITE, new FrameSize(rainImage.getWidth(), rainImage.getWidth()), rainImage, new ResourceMetadata.Builder().build());
            var snowSpriteContents = new SpriteContents(ParticleRainClient.SNOW_SPRITE, new FrameSize(snowImage.getWidth(), snowImage.getWidth()), snowImage, new ResourceMetadata.Builder().build());
            this.registerSprite(rainSpriteContents);
            this.registerSprite(snowSpriteContents);
        }
    }
}
