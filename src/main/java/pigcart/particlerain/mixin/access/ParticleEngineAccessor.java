package pigcart.particlerain.mixin.access;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.particles.ParticleGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ParticleEngine.class)
public interface ParticleEngineAccessor {

    @Accessor
    TextureAtlas getTextureAtlas();

    @Accessor
    Object2IntOpenHashMap<ParticleGroup> getTrackedParticleCounts();

    @Invoker
    boolean callHasSpaceInParticleLimit(ParticleGroup group);
}
