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

public class SnowFlakeParticle extends WeatherParticle {

    float amountToRotateBy;

    protected SnowFlakeParticle(ClientLevel level, double x, double y, double z, SpriteSet provider) {
        super(level, x, y, z, ParticleRainClient.config.snowFlakeGravity, provider);
        this.setSize(0.1F, 0.1F);

        this.rCol = ParticleRainClient.config.color.snowRed;
        this.gCol = ParticleRainClient.config.color.snowGreen;
        this.bCol = ParticleRainClient.config.color.snowBlue;

        this.xd = level.getRandom().nextFloat()/ParticleRainClient.config.snowWindDampening;
        this.zd = level.getRandom().nextFloat()/ParticleRainClient.config.snowWindDampening;

        if (level.getRandom().nextBoolean()) {
            this.amountToRotateBy = ParticleRainClient.config.snowRotationAmount;
        } else {
            this.amountToRotateBy = -ParticleRainClient.config.snowRotationAmount;
        }
    }

    public void tick() {
        super.tick();

        xd = Mth.clamp(xd, 0.05, 100);
        zd = Mth.clamp(zd, 0.05, 100);

        this.oRoll = this.roll;
        this.roll = this.oRoll + this.amountToRotateBy;
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
