package pigcart.particlerain.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.resources.ResourceLocation;
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
        Predicate<? super Stitcher.Holder<T>> predicate = h -> h.entry().name().getNamespace().equals(ParticleRainClient.MOD_ID);
        if (this.texturesToBeStitched.stream().anyMatch(predicate)) {
            // resource reload clears all particles. we can just reset the counter here instead of registering a listener.
            ParticleRainClient.particleCount = 0;
            ParticleRainClient.fogCount = 0;

            NativeImage rainImage = null;
            NativeImage snowImage = null;
            try {
                rainImage = ParticleRainClient.loadTexture(ResourceLocation.withDefaultNamespace("textures/environment/rain.png"));
                snowImage = ParticleRainClient.loadTexture(ResourceLocation.withDefaultNamespace("textures/environment/snow.png"));
                if (ParticleRainClient.config.rain.biomeTint) rainImage.applyToAllPixels(ParticleRainClient.desaturateOperation);
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
        }
    }
}
