package pigcart.particlerain.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.joml.Vector2d;
import pigcart.particlerain.ParticleRainClient;

public abstract class WeatherParticle extends TextureSheetParticle {

    protected final BlockPos.MutableBlockPos pos;

    protected WeatherParticle(ClientLevel level, double x, double y, double z, float gravity, SpriteSet provider) {
        super(level, x, y, z);
        this.setSprite(provider.get(level.getRandom()));

        this.gravity = gravity;

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
    }

    void removeIfOOB() {
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (cameraEntity == null || cameraEntity.distanceToSqr(this.x, this.y, this.z) > (ParticleRainClient.config.particleRadius + 2) * (ParticleRainClient.config.particleRadius + 2)) {
            this.remove();
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