package pigcart.particlerain.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.joml.Math;
import pigcart.particlerain.ParticleRainClient;
import pigcart.particlerain.StonecutterUtil;
import pigcart.particlerain.TextureUtil;
import pigcart.particlerain.WeatherParticleManager;
import pigcart.particlerain.mixin.access.ParticleEngineAccessor;

import static pigcart.particlerain.config.ModConfig.CONFIG;

public class RainParticle extends WeatherParticle {

    public RainParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z, CONFIG.rain.gravity, CONFIG.rain.opacity, CONFIG.rain.size, CONFIG.rain.windStrength, CONFIG.rain.stormWindStrength);

        if (CONFIG.compat.biomeTint) TextureUtil.applyWaterTint(this, level, this.pos);

        ParticleEngineAccessor particleEngine = (ParticleEngineAccessor) Minecraft.getInstance().particleEngine;
        this.setSprite((particleEngine.getTextureAtlas().getSprite(StonecutterUtil.getResourceLocation(ParticleRainClient.MOD_ID, "rain" + random.nextInt(4)))));

        Vec3 vec3 = Minecraft.getInstance().cameraEntity.position();
        this.roll = (float) (Math.atan2(x - vec3.x, z - vec3.z) + Mth.HALF_PI);
    }

    @Override
    public void doCollisionEffects(BlockHitResult hitResult) {
        if (!hitResult.getLocation().closerThan(Minecraft.getInstance().cameraEntity.position(), CONFIG.perf.particleDistance * CONFIG.perf.impactParticleDistance, CONFIG.perf.particleDistance)) {
            return;
        }
        Vec3 collisionPos = hitResult.getLocation();
        if (hitResult.getDirection().getAxis().isVertical()) {
            Vec3 spawnPos = new Vec3(collisionPos.x + ((quadSize * 2) * random.nextFloat()) - quadSize, collisionPos.y, collisionPos.z + ((quadSize * 2) * random.nextFloat()) - quadSize);
            BlockPos blockPos = BlockPos.containing(spawnPos).below();
            boolean collisionBelowExists = !level.getBlockState(blockPos).getCollisionShape(level, blockPos).isEmpty();
            BlockHitResult hit = level.clip(StonecutterUtil.getClipContext(collisionPos, spawnPos));
            if (hit.getType().equals(HitResult.Type.MISS) && collisionBelowExists) {
                BlockState state = level.getBlockState(BlockPos.containing(spawnPos.x, spawnPos.y, spawnPos.z).below());
                SimpleParticleType particle = ParticleTypes.RAIN;
                if (state.is(BlockTags.INFINIBURN_OVERWORLD) || state.is(BlockTags.STRIDER_WARM_BLOCKS)) {
                    particle = level.isThundering() ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SMOKE;
                } else if (state.is(Blocks.WATER)) {
                    particle = ParticleRainClient.RIPPLE;
                }
                Minecraft.getInstance().particleEngine.createParticle(particle, spawnPos.x, spawnPos.y, spawnPos.z, 0, 0, 0);
            }
        } else if (CONFIG.effect.doStreakParticles && WeatherParticleManager.canHostStreaks(level.getBlockState(hitResult.getBlockPos()))) {
            Minecraft.getInstance().particleEngine.createParticle(ParticleRainClient.STREAK, collisionPos.x, collisionPos.y, collisionPos.z, hitResult.getDirection().get2DDataValue(), 0, 0);
        }
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float tickPercentage) {
        Vector3f camPos = camera.getPosition().toVector3f();
        float x = (float) (Mth.lerp(tickPercentage, this.xo, this.x) - camPos.x);
        float y = (float) (Mth.lerp(tickPercentage, this.yo, this.y) - camPos.y);
        float z = (float) (Mth.lerp(tickPercentage, this.zo, this.z) - camPos.z);

        // angle particle along axis of velocity
        Vector3f delta = new Vector3f((float) this.xd, (float) this.yd, (float) this.zd);
        final float angle = Math.acos(delta.normalize().y);
        Vector3f axis = new Vector3f(-delta.z(), 0, delta.x()).normalize();
        Quaternionf quaternion = new Quaternionf(new AxisAngle4f(-angle, axis));

        // rotate particle to face camera
        //quaternion.mul(Axis.YN.rotation(Math.atan2(x, z) + Mth.HALF_PI));
        // idk how to translate this to work with the angled axis, using as-is results in weird rotation
        // for now the rotation is calculated once when the particle spawns, which looks good enough
        quaternion.mul(Axis.YN.rotation(this.roll));
        quaternion = this.flipItTurnwaysIfBackfaced(quaternion, new Vector3f(x, y, z));
        this.renderRotatedQuad(vertexConsumer, quaternion, x, y, z, tickPercentage);
    }

    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        public DefaultFactory(SpriteSet provider) {
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new RainParticle(level, x, y, z);
        }
    }
}