package pigcart.particlerain.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.particlerain.ParticleRainClient;

import java.io.IOException;

import static pigcart.particlerain.ParticleRainClient.RAIN_LOCATION;

@Mixin(Stitcher.class)
public abstract class StitcherMixin {

    @Shadow public abstract void registerSprite(Stitcher.Entry entry);

    @Inject(method = "stitch", at = @At("HEAD"))
    public void stitch(CallbackInfo ci) {
        //TODO: figure out how to target only the particle atlas
        NativeImage nativeImage = null;
        try {
            nativeImage = ParticleRainClient.getTexture(RAIN_LOCATION);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(ParticleRainClient.MOD_ID, "rainn");
        var sc = new SpriteContents(resourceLocation, new FrameSize(64, 64), nativeImage, new ResourceMetadata.Builder().build());
        this.registerSprite(sc);

    }
}
