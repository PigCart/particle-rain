package pigcart.particlerain.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.BiomeTags;
import pigcart.particlerain.ParticleRainClient;

public class DustMoteParticle extends WeatherParticle {
    protected DustMoteParticle(ClientLevel level, double x, double y, double z, SpriteSet provider) {
        super(level, x, y, z, ParticleRainClient.config.desertDustGravity, provider);
        this.xd = 0.2F;
        this.zd = 0.2F;
        if (level.getBiome(new BlockPos((int) this.x, (int) this.y, (int) this.z)).is(BiomeTags.IS_BADLANDS)) {
            this.rCol = ParticleRainClient.config.color.mesaRed;
            this.gCol = ParticleRainClient.config.color.mesaGreen;
            this.bCol = ParticleRainClient.config.color.mesaBlue;
        } else {
            this.rCol = ParticleRainClient.config.color.desertRed;
            this.gCol = ParticleRainClient.config.color.desertGreen;
            this.bCol = ParticleRainClient.config.color.desertBlue;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.onGround) {
            this.yd = 0.1F;
        }
         else if (this.removeIfObstructed()) {
            if (this.isHotBlock()) {
                Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.SMOKE, this.x, this.y, this.z, 0, 0, 0);
            }
        }
        else if (!this.level.getFluidState(this.pos).isEmpty()) {
            this.shouldFadeOut = true;
            this.gravity = 0;
        } else {
            this.xd = 0.2;
            this.zd = 0.2;
        }

        if (age < 10) {
            this.alpha = (age * 1.0f) / 10;
        }
    }
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet provider;

        public DefaultFactory(SpriteSet provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new DustMoteParticle(level, x, y, z, this.provider);
        }
    }
}
