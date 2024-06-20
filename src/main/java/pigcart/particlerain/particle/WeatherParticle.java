package pigcart.particlerain.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import pigcart.particlerain.ParticleRainClient;

public abstract class WeatherParticle extends TextureSheetParticle {

    protected final BlockPos.MutableBlockPos pos;
    int fadeOutTime = 10;
    boolean shouldFadeOut = false;

    protected WeatherParticle(ClientLevel level, double x, double y, double z, float gravity, SpriteSet provider) {
        super(level, x, y, z);
        this.setSprite(provider.get(level.getRandom()));
        this.lifetime = ParticleRainClient.config.particleRadius * 10;
        this.gravity = gravity;

        this.alpha = 0.0F;

        this.xd = 0.0F;
        this.yd = -gravity;
        this.zd = 0.0F;

        this.quadSize = 0.1F;

        this.pos = new BlockPos.MutableBlockPos();
    }

    @Override
    public void tick() {
        super.tick();
        this.pos.set(this.x, this.y - 0.2, this.z);
        this.removeIfOOB();
        if (shouldFadeOut) {
            if (this.alpha < 0.01) {
                remove();
            } else {
                this.alpha = this.alpha - 0.1F;
            }
        } else {
            if (age < 10) {
                this.alpha = (age * 1.0f) / 10;
            }
        }
    }

    void removeIfOOB() {
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (cameraEntity == null || cameraEntity.distanceToSqr(this.x, this.y, this.z) > Mth.square(ParticleRainClient.config.particleRadius)) {
            shouldFadeOut = true;
        }

    }
    protected boolean removeIfObstructed() {
        if (x == xo || y == yo || z == zo || !this.level.getFluidState(this.pos).isEmpty()) {
            this.remove();
            return true;
        } else {
            return false;
        }
    }
    protected boolean isHotBlock() {
        FluidState fluidState = this.level.getFluidState(this.pos);
        BlockState blockState = this.level.getBlockState(this.pos);
        return fluidState.is(FluidTags.LAVA) || blockState.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(blockState);
    }
}