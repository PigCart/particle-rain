package pigcart.particlerain.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import pigcart.particlerain.ParticleRainClient;
import pigcart.particlerain.StonecutterUtil;
import pigcart.particlerain.config.ModConfig;
import pigcart.particlerain.mixin.access.ParticleEngineAccessor;

import static pigcart.particlerain.config.ModConfig.CONFIG;

public class SnowParticle extends WeatherParticle {

    float rotationAmount;

    protected SnowParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.quadSize = CONFIG.snow.size;
        this.gravity = CONFIG.snow.gravity;
        this.yd = -gravity;
        ParticleEngineAccessor particleEngine = (ParticleEngineAccessor) Minecraft.getInstance().particleEngine;
        this.setSprite(particleEngine.getTextureAtlas().getSprite(StonecutterUtil.getResourceLocation(ParticleRainClient.MOD_ID, "snow" + random.nextInt(4))));

        if (level.isThundering()) {
            this.xd = gravity * CONFIG.snow.stormWindStrength;
        } else {
            this.xd = gravity * CONFIG.snow.windStrength;
        }
        if (ModConfig.CONFIG.compat.yLevelWindAdjustment) {
            this.xd = this.xd * yLevelWindAdjustment(y);
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
        this.roll = this.oRoll + (level.isThundering() ? CONFIG.snow.stormRotationAmount : CONFIG.snow.rotationAmount) * this.rotationAmount;
        if (this.onGround || this.removeIfObstructed()) {
            this.remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        public DefaultFactory(SpriteSet provider) {
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new SnowParticle(level, x, y, z);
        }
    }
}
