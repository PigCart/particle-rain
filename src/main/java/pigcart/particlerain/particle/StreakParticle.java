package pigcart.particlerain.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pigcart.particlerain.StonecutterUtil;
import pigcart.particlerain.TextureUtil;
import pigcart.particlerain.WeatherParticleManager;

import static pigcart.particlerain.config.ModConfig.CONFIG;

public class StreakParticle extends WeatherParticle {

    Direction direction;

    private StreakParticle(ClientLevel level, double x, double y, double z, int direction2D, SpriteSet provider) {
        super(level, x, y, z, level.random.nextFloat()/10, CONFIG.streak.opacity, CONFIG.streak.size, 0, 0);

        if (CONFIG.compat.waterTint) {
            TextureUtil.applyWaterTint(this, level, this.pos);
        } else {
            this.setColor(0.2f, 0.3f, 1.0f);
        }

        this.setSprite(provider.get(level.getRandom()));
        this.alpha = CONFIG.streak.opacity;
        this.hasPhysics = true;

        this.roll = direction2D * Mth.HALF_PI;
        direction = Direction.from2DDataValue(direction2D);
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
        BlockHitResult hit = level.clip(StonecutterUtil.getClipContext(start, end));
        BlockState stateBehind = level.getBlockState(hit.getBlockPos());
        FluidState fluidState = level.getFluidState(pos);
        if (hit.getType().equals(HitResult.Type.MISS)) {
            Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.DRIPPING_WATER, x, y - 0.05, z, 0, 0, 0);
            doCollisionAnim = true;
        } else if (!WeatherParticleManager.canHostStreaks(stateBehind) || !fluidState.isEmpty()) {
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
        if (!doCollisionAnim) super.tickDistanceFade();
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
        Vec3 camPos = camera.getPosition();
        float x = (float) (Mth.lerp(f, this.xo, this.x) - camPos.x());
        float y = (float) (Mth.lerp(f, this.yo, this.y) - camPos.y());
        float z = (float) (Mth.lerp(f, this.zo, this.z) - camPos.z());

        Quaternionf quaternion = new Quaternionf(new AxisAngle4d(this.roll, 0, 1, 0));
        this.turnBackfaceFlipways(quaternion, new Vector3f(x, y, z));
        this.renderRotatedQuad(vertexConsumer, quaternion, x, y + quadSize, z, f);
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
            return new StreakParticle(level, x, y, z, (int) velocityX, this.provider);
        }
    }
}
