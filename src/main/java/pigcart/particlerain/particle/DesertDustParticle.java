package pigcart.particlerain.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import pigcart.particlerain.ParticleRainClient;

public class DesertDustParticle extends SpriteBillboardParticle {

    protected DesertDustParticle(ClientWorld clientWorld, double d, double e, double f, double g, double h, double i, SpriteProvider provider) {
        super(clientWorld, d, e, f, g, h, i);
        this.setSprite(provider);

        float gravity = ParticleRainClient.config.desertDustGravity;

        this.gravityStrength = gravity;
        this.maxAge = 100;

        this.velocityX = -0.3F;
        this.velocityY = -gravity;
        this.velocityZ = 0.0F;

        this.colorRed = (float) g;
        this.colorGreen = (float) h;
        this.colorBlue = (float) i;

        this.scale = 0.15F;
    }

    public void tick() {
        super.tick();
        this.velocityX = -0.4;
        if (ParticleRainClient.getDistance(MinecraftClient.getInstance().getCameraEntity().getBlockPos(), this.x, this.y, this.z) > ParticleRainClient.config.particleRadius+2 || this.prevPosX == this.x || this.world.getFluidState(new BlockPos(this.x, this.y, this.z)).isIn(FluidTags.WATER)) {
            this.markDead();
        }
        if (this.onGround) {
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
