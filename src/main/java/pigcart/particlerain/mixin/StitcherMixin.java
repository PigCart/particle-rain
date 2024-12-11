package pigcart.particlerain.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
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

@Mixin(Stitcher.class)
public abstract class StitcherMixin<T extends Stitcher.Entry> {

    @Shadow public abstract void registerSprite(Stitcher.Entry entry);

    @Shadow @Final private List<Stitcher.Holder<T>> texturesToBeStitched;

    // environment textures arent atlassed so we must add them to the atlas ourselves!
    @Inject(method = "stitch", at = @At("HEAD"))
    public void stitch(CallbackInfo ci) {
        // check for the existence of particle rain textures on this atlas to ensure we're only adding to the particle atlas
        Predicate<? super Stitcher.Holder<T>> namespacePredicate = h -> h.entry().name().getNamespace().equals(ParticleRainClient.MOD_ID);
        if (this.texturesToBeStitched.stream().anyMatch(namespacePredicate)) {
            // resource reload clears all particles. we can just reset the counter here instead of registering a listener.
            ParticleRainClient.particleCount = 0;
            ParticleRainClient.fogCount = 0;

            NativeImage rainImage = null;
            NativeImage snowImage = null;
            try {
                rainImage = ParticleRainClient.loadTexture(ResourceLocation.withDefaultNamespace("textures/environment/rain.png"));
                snowImage = ParticleRainClient.loadTexture(ResourceLocation.withDefaultNamespace("textures/environment/snow.png"));
                if (ParticleRainClient.config.biomeTint) rainImage.applyToAllPixels(ParticleRainClient.desaturateOperation);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // split both weather textures into four sprites
            for (int i = 0; i < 4; i++) {
                this.registerSprite(ParticleRainClient.splitImage(rainImage, i, "rain"));
            }
            for (int i = 0; i < 4; i++) {
                this.registerSprite(ParticleRainClient.splitImage(snowImage, i, "snow"));
            }
            // generate ripple sprites
            T entry = ParticleRainClient.getTextureToBeStitched(this.texturesToBeStitched, ResourceLocation.withDefaultNamespace("big_smoke_0"));
            if (entry != null) {
                for (int i = 0; i < 8; i++) {
                    this.registerSprite(ParticleRainClient.generateRipple(i, entry.width()));
                }
            } else {
                for (int i = 0; i < 8; i++) {
                    this.registerSprite(ParticleRainClient.generateRipple(i, 16));
                }
            }
            // add tinted versions of the default splashes
            if (ParticleRainClient.config.biomeTint) {
                for (int i = 0; i < 4; i++) {
                    NativeImage splashImage = null;
                    try {
                        splashImage = ParticleRainClient.loadTexture(ResourceLocation.withDefaultNamespace(String.format("textures/particle/splash_%d.png", i)));
                        splashImage.applyToAllPixels(ParticleRainClient.desaturateOperation);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    this.registerSprite(new SpriteContents(ResourceLocation.fromNamespaceAndPath(ParticleRainClient.MOD_ID, "splash" + i), new FrameSize(splashImage.getWidth(), splashImage.getHeight()), splashImage, new ResourceMetadata.Builder().build()));
                }
            }
        }
    }
}
