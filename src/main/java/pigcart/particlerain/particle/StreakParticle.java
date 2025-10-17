package pigcart.particlerain.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.VersionUtil;
import pigcart.particlerain.TextureUtil;
import pigcart.particlerain.config.Whitelist;
import pigcart.particlerain.mixin.access.ParticleEngineAccessor;
//? if >=1.21.9 {
/*import net.minecraft.core.particles.ParticleLimit;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
*///?} else {
import net.minecraft.core.particles.ParticleGroup;
import com.mojang.blaze3d.vertex.VertexConsumer;
//?}

import java.util.Optional;

import static pigcart.particlerain.config.ConfigManager.config;

public class StreakParticle extends WeatherParticle {

    private final Direction direction;
    private final Whitelist.BlockList blockList;

    public StreakParticle(ClientLevel level, double x, double y, double z, Direction direction, Whitelist.BlockList blockList) {
        super(level, x, y, z, VersionUtil.getSprite(VersionUtil.getId("streak")));
        if (config.compat.waterTint) {
            TextureUtil.applyWaterTint(this, level, this.pos);
        } else {
            this.setColor(0.2f, 0.3f, 1.0f);
        }
        this.alpha = config.streak.opacity;
        this.quadSize = config.streak.size;
        this.setSize(0.01F, 0.01F);
        this.hasPhysics = true;
        this.yd = -0.1;
        this.roll = direction.get2DDataValue() * Mth.HALF_PI;

        this.direction = direction;
        this.blockList = blockList;
    }

    @Override
    //? if >=1.21.9 {
    /*public Optional<ParticleLimit> getParticleLimit() {
    *///?} else {
    public Optional<ParticleGroup> getParticleGroup() {
    //?}
        return Optional.empty();
    }

    @Override
    public void tick() {
        super.tick();
        if (onGround) doCollisionAnim = true;
        if (this.age % 10 == 0) {
            if (random.nextBoolean()) {
                this.gravity = random.nextFloat() / 10;
            } else {
                this.gravity = 0;
            }
        }
    }

    @Override
    public void onPositionUpdate() {
        Vec3 start = new Vec3(x, y, z);
        Vec3 end = start.relative(direction.getOpposite(), 0.06F);
        BlockHitResult hit = level.clip(VersionUtil.getClipContext(start, end));
        BlockState stateBehind = level.getBlockState(hit.getBlockPos());
        FluidState fluidState = level.getFluidState(pos);
        if (hit.getType().equals(HitResult.Type.MISS)) {
            Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.DRIPPING_WATER, x, y - 0.05, z, 0, 0, 0);
            doCollisionAnim = true;
        } else if (!blockList.contains(stateBehind.getBlockHolder()) || !fluidState.isEmpty()) {
            doCollisionAnim = true;
        }
    }

    @Override
    public void tickCollisionAnim() {
        gravity = 0;
        yd = 0;
        this.alpha = alpha - 0.1F;
        if (alpha <= 0) remove();
    }

    @Override
    public void tickDistanceFade() {
        //if (!doCollisionAnim) super.tickDistanceFade();
    }

    @Override
    public void /*? if >=1.21.9 {*//*extract(QuadParticleRenderState*//*?} else {*/render(VertexConsumer/*?}*/ h, Camera camera, float f) {
        Vec3 camPos = camera.getPosition();
        float x = (float) (Mth.lerp(f, this.xo, this.x) - camPos.x());
        float y = (float) (Mth.lerp(f, this.yo, this.y) - camPos.y());
        float z = (float) (Mth.lerp(f, this.zo, this.z) - camPos.z());

        Quaternionf quaternion = new Quaternionf(new AxisAngle4d(this.roll, 0, 1, 0));
        this.turnBackfaceFlipways(quaternion, new Vector3f(x, y, z));
        this.renderRotatedQuad(h, quaternion, x, y + quadSize, z, f);
    }

    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet provider;

        public DefaultFactory(SpriteSet provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ/*? if >=1.21.9 {*//*, RandomSource random*//*?}*/) {
            return new StreakParticle(level, x, y, z, Direction.getRandom(level.random), new Whitelist.BlockList());
        }
    }
}
