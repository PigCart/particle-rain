package pigcart.particlerain.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import pigcart.particlerain.ParticleRainClient;

import static pigcart.particlerain.ParticleRainClient.config;

public class SnowParticle extends WeatherParticle {

    float rotationAmount;

    protected SnowParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.quadSize = config.snow.size;
        this.gravity = config.snow.gravity;
        this.yd = -gravity;
        this.setSprite(Minecraft.getInstance().particleEngine.textureAtlas.getSprite(ResourceLocation.fromNamespaceAndPath(ParticleRainClient.MOD_ID, "snow" + random.nextInt(4))));

        if (level.isThundering()) {
            this.xd = gravity * config.snow.stormWindStrength;
        } else {
            this.xd = gravity * config.snow.windStrength;
        }
        if (ParticleRainClient.config.yLevelWindAdjustment) {
            this.xd = this.xd * ParticleRainClient.yLevelWindAdjustment(y);
        }
        this.zd = this.xd;

        if (level.getRandom().nextBoolean()) {
            this.rotationAmount = 1;
        } else {
            this.rotationAmount = -1;
        }
    }

    public void tick() {
        super.tick();
        this.oRoll = this.roll;
        this.roll = this.oRoll + (level.isThundering() ? config.snow.stormRotationAmount : config.snow.rotationAmount) * this.rotationAmount;
        if (this.onGround || this.removeIfObstructed()) {
            this.remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        public DefaultFactory(SpriteSet provider) {
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new SnowParticle(level, x, y, z);
        }
    }
}
