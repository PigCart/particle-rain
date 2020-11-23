package pigcart.particlerain.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;

public class DesertDustParticle extends SpriteBillboardParticle {

    protected DesertDustParticle(ClientWorld clientWorld, double d, double e, double f, double g, double h, double i, SpriteProvider provider) {
        super(clientWorld, d, e, f, g, h, i);
        this.setSprite(provider);

        this.gravityStrength = 0.1F;
        this.maxAge = 100;

        this.velocityX = -0.4F;
        this.velocityY = -0.1F;
        this.velocityZ = 0.0F;

        this.scale = 0.15F;
    }

    public void tick() {
        super.tick();
        if (this.age > this.maxAge || this.velocityX == 0.0F || this.world.getFluidState(new BlockPos(this.x, this.y, this.z)).isIn(FluidTags.WATER)) {
            this.markDead();
        }
        if (this.onGround) {
            this.velocityX = -0.3;
            this.velocityY = 0.1F;
        }
    }

    @Override
    public ParticleTextureSheet getType() { return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE; }

    @Environment(EnvType.CLIENT)
    public static class DefaultFactory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider provider;

        public DefaultFactory(SpriteProvider provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(DefaultParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new DesertDustParticle(world, x, y, z, velocityX, velocityY, velocityZ, this.provider);
        }
    }
}
