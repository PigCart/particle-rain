package pigcart.particlerain.mixin;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pigcart.particlerain.ParticleRainClient;

@Mixin(TextureAtlasSprite.class)
public abstract class TextureAtlasSpriteMixin {

    @Shadow @Final private SpriteContents contents;

    @Inject(method = "createTicker", at = @At("HEAD"), cancellable = true)
    public void createTicker(CallbackInfoReturnable<TextureAtlasSprite.Ticker> cir) {
        if (this.contents.name().getNamespace().equals(ParticleRainClient.MOD_ID)) {
            cir.cancel();
        }
    }
}
