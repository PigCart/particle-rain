package pigcart.particlerain.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureSheetParticle.class)
public abstract class TextureSheetParticleMixin extends SingleQuadParticle {

    @Shadow protected abstract void setSprite(TextureAtlasSprite textureAtlasSprite);

    protected TextureSheetParticleMixin(ClientLevel clientLevel, double d, double e, double f) {
        super(clientLevel, d, e, f);
    }

    @Inject(method = "pickSprite", at = @At("TAIL"))
    public void pickSprite(SpriteSet spriteSet, CallbackInfo ci) {};
}
