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

public class SnowFlakeParticle extends SpriteBillboardParticle {

    protected SnowFlakeParticle(ClientWorld clientWorld, double d, double e, double f, double red, double green, double blue, SpriteProvider provider) {
        super(clientWorld, d, e, f, red, green, blue);
        this.setSprite(provider);

        float gravity = ParticleRainClient.config.snowFlakeGravity;

        this.gravityStrength = gravity;
        this.maxAge = 200;

        this.velocityX = 0.0F;
        this.velocityY = -gravity;
        this.velocityZ = 0.0F;

        this.colorRed = (float)red;
        this.colorGreen = (float)green;
        this.colorBlue = (float)blue;

        this.scale = 0.15F;
    }

    public void tick() {
        super.tick();
        if (ParticleRainClient.getDistance(MinecraftClient.getInstance().getCameraEntity().getBlockPos(), this.x, this.y, this.z) > ParticleRainClient.config.particleRadius+2 || this.onGround || this.world.getFluidState(new BlockPos(this.x, this.y, this.z)).isIn(FluidTags.WATER)) {
            this.markDead();
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }

    @Environment(EnvType.CLIENT)
    public static class DefaultFactory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider provider;

        public DefaultFactory(SpriteProvider provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(DefaultParticleType parameters, ClientWorld world, double x, double y, double z, double red, double green, double blue) {
            return new SnowFlakeParticle(world, x, y, z, red, green, blue, this.provider);
        }
    }
}
