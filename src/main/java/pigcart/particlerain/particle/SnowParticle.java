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
import net.minecraft.util.Mth;
import pigcart.particlerain.ParticleRainClient;

public class SnowParticle extends WeatherParticle {

    float rotationAmount;

    protected SnowParticle(ClientLevel level, double x, double y, double z, SpriteSet provider) {
        super(level, x, y, z, ParticleRainClient.config.snow.gravity, provider);
        this.quadSize = ParticleRainClient.config.snow.size;
        this.setSprite(Minecraft.getInstance().getModelManager().getAtlas(ParticleRainClient.BLOCKS_LOCATION).getSprite(ParticleRainClient.SNOW_SPRITE));

        this.xd = level.getRandom().nextFloat() * ParticleRainClient.config.snow.windStrength;
        this.zd = level.getRandom().nextFloat() * ParticleRainClient.config.snow.windStrength;

        if (level.getRandom().nextBoolean()) {
            this.rotationAmount = ParticleRainClient.config.snow.rotationAmount;
        } else {
            this.rotationAmount = -ParticleRainClient.config.snow.rotationAmount;
        }
    }

    public void tick() {
        super.tick();

        xd = Mth.clamp(xd, 0.05, 100);
        zd = Mth.clamp(zd, 0.05, 100);

        this.oRoll = this.roll;
        this.roll = this.oRoll + this.rotationAmount;
        if (this.onGround || this.removeIfObstructed()) {
            if (this.isHotBlock()) {
                Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.SMOKE, this.x, this.y, this.z, 0, 0, 0);
            }
            this.remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    @Environment(EnvType.CLIENT)
    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet provider;

        public DefaultFactory(SpriteSet provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new SnowParticle(level, x, y, z, this.provider);
        }
    }
}
