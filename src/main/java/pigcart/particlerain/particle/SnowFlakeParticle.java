package pigcart.particlerain.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import pigcart.particlerain.ParticleRainClient;

public class SnowFlakeParticle extends WeatherParticle {

    private SnowFlakeParticle(ClientLevel level, double x, double y, double z, SpriteSet provider) {
        super(level, x, y, z, ParticleRainClient.config.snowFlakeGravity, provider);
        this.lifetime = ParticleRainClient.config.particleRadius * 10;
        this.setSize(0.1F, 0.1F);

        this.rCol = ParticleRainClient.config.color.snowRed;
        this.gCol = ParticleRainClient.config.color.snowGreen;
        this.bCol = ParticleRainClient.config.color.snowBlue;

        RandomSource rand = RandomSource.create();
        this.xd = rand.nextFloat()/ParticleRainClient.config.snowWindDampening;
        this.zd = rand.nextFloat()/ParticleRainClient.config.snowWindDampening;
    }

    public void tick() {
        super.tick();
        this.oRoll = this.roll;
        this.roll = this.oRoll + ParticleRainClient.config.snowRotationAmount;
        if (this.removeIfObstructed()) {
            if (this.isHotBlock()) {
                Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.SMOKE, this.x, this.y, this.z, 0, 0, 0);
            }
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet provider;

        public DefaultFactory(SpriteSet provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new SnowFlakeParticle(level, x, y, z, this.provider);
        }
    }
}
