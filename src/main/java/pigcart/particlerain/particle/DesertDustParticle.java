package pigcart.particlerain.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;
import pigcart.particlerain.ParticleRainClient;

public class DesertDustParticle extends WeatherParticle {

    private DesertDustParticle(ClientLevel clientWorld, double x, double y, double z, float red, float green, float blue, SpriteSet provider) {
        super(clientWorld, x, y, z, red, green, blue, ParticleRainClient.config.desertDustGravity, provider);
        this.lifetime = 100;
        this.xd = -0.4F;
    }

    @Override
    public void tick() {
        super.tick();

        this.xd = -0.4;
        if (this.shouldRemove() || this.xo == this.x || this.level.getFluidState(this.pos).is(FluidTags.WATER))
            this.remove();
        if (this.onGround)
            this.yd = 0.1F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet provider;

        public DefaultFactory(SpriteSet provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double red, double green, double blue) {
            return new DesertDustParticle(level, x, y, z, (float) red, (float) green, (float) blue, this.provider);
        }
    }
}
