package pigcart.particlerain.particle;

import com.mojang.math.Axis;
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
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pigcart.particlerain.ParticleRainClient;

public abstract class WeatherParticle extends TextureSheetParticle {

    protected BlockPos.MutableBlockPos pos;
    boolean shouldFadeOut = false;

    protected WeatherParticle(ClientLevel level, double x, double y, double z, float gravity, SpriteSet provider) {
        super(level, x, y, z);
        this.lifetime = ParticleRainClient.config.particleRadius * 10;
        this.gravity = gravity;

        this.alpha = 0.0F;

        this.xd = 0.0F;
        this.yd = -gravity;
        this.zd = 0.0F;

        this.pos = new BlockPos.MutableBlockPos(x, y, z);
        ParticleRainClient.particleCount++;
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

    @Override
    public void remove() {
        if (this.isAlive()) ParticleRainClient.particleCount--;
        super.remove();
    }

    void removeIfOOB() {
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (cameraEntity == null || cameraEntity.distanceToSqr(this.x, this.y, this.z) > Mth.square(ParticleRainClient.config.particleRadius)) {
            shouldFadeOut = true;
        }

    }
    protected boolean removeIfObstructed() {
        if (x == xo || z == zo) {
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
    public Quaternionf flipItTurnwaysIfBackfaced(Quaternionf quaternion, Vector3f toCamera) {
        Vector3f normal = new Vector3f(0, 0, 1);
        normal.rotate(quaternion).normalize();
        float dot = normal.dot(toCamera);
        if (dot > 0) {
            return quaternion.mul(Axis.YP.rotation(Mth.PI));
        }
        else return quaternion;
    }
}