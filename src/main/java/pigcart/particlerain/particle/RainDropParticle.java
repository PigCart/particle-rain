package pigcart.particlerain.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import pigcart.particlerain.ParticleRainClient;

import java.util.Objects;

public class RainDropParticle extends SpriteBillboardParticle {

    MinecraftClient client;

    protected RainDropParticle(ClientWorld clientWorld, double d, double e, double f, SpriteProvider provider) {
        super(clientWorld, d, e, f);
        this.setSprite(provider);

        float gravity = ParticleRainClient.config.rainDropGravity;

        this.gravityStrength = gravity;
        this.maxAge = 200;

        this.velocityX = 0.0F;
        this.velocityY = -gravity;
        this.velocityZ = 0.0F;

        this.scale = 0.15F;
    }

    public void tick() {
        super.tick();
        BlockPos blockPos = new BlockPos(this.x, this.y-0.1, this.z);
        if ( ParticleRainClient.getDistance(MinecraftClient.getInstance().getCameraEntity().getBlockPos(), this.x, this.y, this.z) > ParticleRainClient.config.particleRadius+2 || this.onGround || this.world.getFluidState(blockPos).isIn(FluidTags.WATER) || this.world.getFluidState(blockPos).isIn(FluidTags.LAVA)) {
            this.markDead();
        }
    }

    @Override
    public ParticleTextureSheet getType() { return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT; }

    @Environment(EnvType.CLIENT)
    public static class DefaultFactory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider provider;

        public DefaultFactory(SpriteProvider provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(DefaultParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new RainDropParticle(world, x, y, z, this.provider);
        }
    }
}