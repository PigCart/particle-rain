package pigcart.particlerain.mixin.access;

import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.client.renderer.texture.TextureAtlas;

@Mixin(ParticleEngine.class)
public interface ParticleEngineAccessor {

    //? if <1.21.9 {
    @Accessor
    TextureAtlas getTextureAtlas();
    //?}

    @Invoker
    void callClearParticles();
}
