package pigcart.particlerain.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.*;
import org.joml.Math;
import pigcart.particlerain.ParticleRainClient;
import pigcart.particlerain.Util;

import static pigcart.particlerain.config.ModConfig.CONFIG;

public class RainParticle extends WeatherParticle {

    protected RainParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);

        if (CONFIG.compat.biomeTint) Util.applyWaterTint(this, level, this.pos);

        this.quadSize = CONFIG.rain.size;
        this.gravity = CONFIG.rain.gravity;
        this.yd = -gravity;
        this.setSprite(Minecraft.getInstance().particleEngine.textureAtlas.getSprite(ResourceLocation.fromNamespaceAndPath(ParticleRainClient.MOD_ID, "rain" + random.nextInt(4))));

        if (level.isThundering()) {
            this.xd = gravity * CONFIG.rain.stormWindStrength;
        } else {
            this.xd = gravity * CONFIG.rain.windStrength;
        }
        if (CONFIG.compat.yLevelWindAdjustment) {
            this.xd = this.xd * yLevelWindAdjustment(y);
        }
        this.zd = this.xd;

        this.lifetime = CONFIG.perf.particleRadius * 5;
        Vec3 vec3 = Minecraft.getInstance().cameraEntity.position();
        this.roll = (float) (Math.atan2(x - vec3.x, z - vec3.z) + Mth.HALF_PI);
    }

    @Override
    public void fadeIn() {
        if (age < 20) {
            this.alpha = Math.clamp(0, CONFIG.rain.opacity, (age * 1.0f) / 20);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.onGround || !this.level.getFluidState(this.pos).isEmpty()) {
            // TODO: rewrite this whole bit
            // TODO: really: REWRITE THIS WHOLE BIT!!!
            if ((CONFIG.effect.doSplashParticles || CONFIG.effect.doSmokeParticles || CONFIG.effect.doRippleParticles) && Minecraft.getInstance().cameraEntity.position().distanceTo(this.pos.getCenter()) < CONFIG.perf.particleRadius - (CONFIG.perf.particleRadius / 2.0)) {
                for (int i = 0; i < CONFIG.rain.impactEffectAmount; i++) {
                    Vec3 spawnPos = Vec3.atLowerCornerWithOffset(this.pos, (random.nextFloat() * 3) - 1, 0, (random.nextFloat() * 3) - 1);
                    double d = random.nextDouble();
                    double e = random.nextDouble();
                    BlockPos blockPos = BlockPos.containing(spawnPos);
                    BlockState blockState = level.getBlockState(blockPos);
                    FluidState fluidState = level.getFluidState(blockPos);
                    VoxelShape voxelShape = blockState.getCollisionShape(level, blockPos);
                    double voxelHeight = voxelShape.max(Direction.Axis.Y, d, e);
                    double fluidHeight = fluidState.getHeight(level, blockPos);
                    double height = java.lang.Math.max(voxelHeight, fluidHeight);
                    Vec3 raycastStart = new Vec3(this.x, this.y, this.z);
                    Vec3 raycastEnd = new Vec3(spawnPos.x, this.y, spawnPos.z);
                    BlockHitResult hit = level.clip(new ClipContext(raycastStart, raycastEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                    Vec2 raycastHit = new Vec2((float)hit.getLocation().x, (float)hit.getLocation().z);
                    // this is SUCH a god damn mess
                    if (height != 0 && raycastHit.distanceToSqr(new Vec2((float) spawnPos.x, (float) spawnPos.z)) < 0.01) {
                        if (CONFIG.effect.doRippleParticles && fluidState.isSourceOfType(Fluids.WATER)) {
                            if (height != 1) { // lazy workaround
                                Minecraft.getInstance().particleEngine.createParticle(ParticleRainClient.RIPPLE, spawnPos.x, spawnPos.y + height, spawnPos.z, 0, 0, 0);
                                if (level.isThundering() && CONFIG.effect.doSplashParticles) Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.RAIN, spawnPos.x, spawnPos.y + height, spawnPos.z, 0, 0, 0);
                            }
                        } else if (CONFIG.effect.doSmokeParticles && (blockState.is(BlockTags.INFINIBURN_OVERWORLD) || blockState.is(BlockTags.STRIDER_WARM_BLOCKS))) {
                            //TODO: config option for warm block tags
                            Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.SMOKE, spawnPos.x, spawnPos.y + height, spawnPos.z, 0, 0, 0);
                            if (level.isThundering()) Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.LARGE_SMOKE, spawnPos.x, spawnPos.y + height, spawnPos.z, 0, 0, 0);
                        } else if (CONFIG.effect.doSplashParticles) {
                            Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.RAIN, spawnPos.x, spawnPos.y + height, spawnPos.z, 0, 0, 0);
                        }
                    }
                }
            }
            this.remove();
        } else if (this.removeIfObstructed()) {
            Vec3 raycastStart = new Vec3(this.x, this.y, this.z);
            Vec3 raycastEnd = new Vec3(this.x + CONFIG.rain.windStrength, this.y, this.z + CONFIG.rain.windStrength);
            BlockHitResult hit = level.clip(new ClipContext(raycastStart, raycastEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
            if (hit.getType().equals(HitResult.Type.BLOCK)) {
                if (CONFIG.effect.doStreakParticles && Minecraft.getInstance().cameraEntity.position().distanceTo(this.pos.getCenter()) < CONFIG.perf.particleRadius - (CONFIG.perf.particleRadius / 2.0)) {
                    if (Util.canHostStreaks(level.getBlockState(hit.getBlockPos()))) {
                        Minecraft.getInstance().particleEngine.createParticle(ParticleRainClient.STREAK, this.x, this.y, this.z, hit.getDirection().get2DDataValue(), 0, 0);
                        Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.RAIN, this.x, this.y, this.z, 0, 0, 0);
                    }
                }
                this.remove();
            }
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

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        public DefaultFactory(SpriteSet provider) {
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new RainParticle(level, x, y, z);
        }
    }
}