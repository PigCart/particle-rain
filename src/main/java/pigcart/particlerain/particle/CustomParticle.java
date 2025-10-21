package pigcart.particlerain.particle;

import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.joml.Math;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.VersionUtil;
import pigcart.particlerain.WeatherParticleManager;
import pigcart.particlerain.config.ConfigData;
import pigcart.particlerain.mixin.access.ParticleEngineAccessor;
//? if > 1.20.1 {
/*import pigcart.particlerain.mixin.access.SingleQuadParticleAccessor;
*///?}
//? if >=1.21.9 {
/*import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.core.particles.ParticleLimit;
*///?} else {
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
//?}

import java.util.Optional;
import java.util.Set;

import static pigcart.particlerain.config.ConfigManager.config;

public class CustomParticle extends WeatherParticle {

    private static final Set<String> usuallyUntintableSprites = Set.of("particlerain:rain_0", "particlerain:rain_1", "particlerain:rain_2", "particlerain:rain_3");
    public ConfigData.ParticleData opts;
    private float oCollisionAnimProgress = 1;
    private float collisionAnimProgress = 1;
    private float speed = 0;
    private final float rotationVariation;
    boolean doCollisionAnim = false;
    public BlockPos.MutableBlockPos pos;
    protected BlockPos.MutableBlockPos oPos;
    BlockHitResult collision = null;
    float baseTemp;
    float oQuadSize;
    float distance;

    public CustomParticle(ClientLevel level, double x, double y, double z, ConfigData.ParticleData opts) {
        super(level, x, y, z, VersionUtil.getSprite(VersionUtil.parseId(opts.spriteLocations.get(level.random.nextInt(opts.spriteLocations.size())))));

        this.gravity = opts.gravity;
        this.yd = -gravity;
        this.quadSize = opts.size;
        this.alpha = 0;
        this.hasPhysics = false;


        this.setSize(quadSize, quadSize);
        this.lifetime = config.perf.particleDistance * 100;
        this.pos = new BlockPos.MutableBlockPos(x, y, z);
        this.oPos = new BlockPos.MutableBlockPos(x, y, z);
        this.baseTemp = level.getBiome(this.pos).value().getBaseTemperature();

        this.opts = opts;
        this.lifetime = opts.lifetime;
        this.rotationVariation = opts.rotationAmount * ((random.nextFloat() - 0.5F) * 2.0F);
        if (opts.constantScreenSize) {
            this.quadSize = getDistanceSize();
        } else {
            this.quadSize = opts.size;
        }
        if (!usuallyUntintableSprites.contains(this.sprite.contents().name().toString()) || config.compat.waterTint) {
            opts.tintType.applyTint(this, level, this.pos, opts);
        }
    }

    public void tick() {
        super.tick();
        oQuadSize = quadSize;
        distance = (float) Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceTo(new Vec3(x, y, z));
        pos.set(x, y, z);
        if (!pos.equals(oPos)) {
            onPositionUpdate();
            oPos.set(pos);
        }
        if (doCollisionAnim) {
            tickCollisionAnim();
        }
        tickDistanceFade();
        speed = (float) new Vec3(xd, yd, zd).length();
        if (opts.constantScreenSize && !doCollisionAnim) quadSize = getDistanceSize();
        if (opts.rotationAmount != 0) {
            oRoll = roll;
            roll += rotationVariation * speed;
        }
        tickWind();
    }

    public void tickDistanceFade() {
        float renderDistance = config.perf.particleDistance;
        if (distance > renderDistance + 1) {
            remove();
        } else {
            alpha = Mth.lerp(Mth.clamp(distance / renderDistance, 0, 1), opts.opacity, 0);
        }
    }

    public void tickWind() {
        float frequency = config.wind.gustFrequency;
        float shift = ParticleRain.clientTicks * config.wind.modulationSpeed;
        float variance = config.wind.strengthVariance;
        float strength = config.wind.strength;
        float multiplier = level.isThundering() ? opts.stormWindStrength : opts.windStrength;
        if (config.wind.yLevelAdjustment) multiplier *= yLevelWindMultiplier(y);
        this.xd = (((Mth.sin((float)x * frequency + shift) * variance) + variance + strength) * multiplier) + 0.001F;
        this.zd = (((Mth.sin((float)z * frequency + shift) * variance) + variance + strength) * multiplier) + 0.001F;
    }

    public static float yLevelWindMultiplier(double y) {
        int transitionStart = 50;
        int transitionDistance = 40;
        return (float) Mth.clamp((y - transitionStart) / transitionDistance, 0, 1);
    }

    public void onPositionUpdate() {
        if (!config.compat.crossBiomeBorder && Mth.abs(level.getBiome(pos).value().getBaseTemperature() - baseTemp) > 0.4) {
            doCollisionAnim = true;
        }
        if (level.getBlockState(pos).isCollisionShapeFullBlock(level, pos) || !level.getFluidState(pos).isEmpty()) {
            this.remove();
        }
        testForCollisions();
    }

    public void testForCollisions() {
        float length = quadSize;
        if (opts.rotationType.equals(ConfigData.RotationType.RELATIVE_VELOCITY)) {
            final Vec3 camD = Minecraft.getInstance().getCameraEntity().getDeltaMovement();
            Vector3f deltaMotion = new Vector3f((float) (this.xd - camD.x), (float) (this.yd - camD.y), (float) (this.zd - camD.z));
            length *= Mth.clamp(deltaMotion.lengthSquared(), 0.2F, 1.0F);
        }
        Vec3 quadCenterPos = new Vec3(x, y, z);
        Vec3 quadEdgePos = new Vec3(xd, yd, zd).normalize().multiply(length, length, length).add(x, y, z);
        final BlockHitResult hitResult = level.clip(VersionUtil.getClipContext(quadCenterPos, quadEdgePos));
        if (!hitResult.getType().equals(HitResult.Type.MISS) && !doCollisionAnim) {
            collision = hitResult;
            doCollisionAnim = true;
        }
    }

    public void tickCollisionAnim() {
        //TODO 
        oCollisionAnimProgress = collisionAnimProgress;
        collisionAnimProgress -= speed;
        if (!opts.rotationType.equals(ConfigData.RotationType.RELATIVE_VELOCITY)) {
            quadSize -= speed;
        }
        if (oCollisionAnimProgress <= 0) remove();
    }

    public float getDistanceSize() {
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson() && Minecraft.getInstance().player.isScoping()) {
            return distance * opts.size * 0.25F;
        } else {
            return distance * opts.size;
        }
    }

    @Override
    //? if >=1.21.9 {
    /*public Optional<net.minecraft.core.particles.ParticleLimit> getParticleLimit() {
        return Optional.of(WeatherParticleManager.particleGroup);
    }
    *///?} else {
    public Optional<ParticleGroup> getParticleGroup() {
        return Optional.of(WeatherParticleManager.particleGroup);
    }
    //?}

    @Override
    //? if >=1.21.9 {
    /*public SingleQuadParticle.Layer getLayer() {
        return opts.renderType.get();
    }
    *///?} else {
    public ParticleRenderType getRenderType() {
        return opts.renderType.get();
    }
    //?}

    @Override
    //? if >=1.21.9 {
    /*public void extract(QuadParticleRenderState h, Camera camera, float tickPercent) {
    *///?} else {
    public void render(VertexConsumer h, Camera camera, float tickPercent) {
    //?}
        opts.rotationType.render(h, camera, tickPercent, this);
    }

    public void renderLookingQuad(/*? if >=1.21.9 {*//*QuadParticleRenderState*//*?} else {*/VertexConsumer/*?}*/ h, Camera camera, float tickPercent) {
        Vec3 camPos = camera.getPosition();
        float offsetX = (float) (Mth.lerp(tickPercent, this.xo, this.x) - camPos.x());
        float offsetY = (float) (Mth.lerp(tickPercent, this.yo, this.y) - camPos.y());
        float offsetZ = (float) (Mth.lerp(tickPercent, this.zo, this.z) - camPos.z());

        Vector3f localPos = new Vector3f(offsetX, offsetY, offsetZ);
        // rotate particle around y axis to face player
        Quaternionf quaternion = Axis.YP.rotation(Math.atan2(offsetX, offsetZ) + Mth.PI);
        // rotate particle by angle between y axis and camera location
        float yAngle = Math.asin(offsetY / localPos.length());
        quaternion.rotateX(yAngle);
        quaternion.rotateZ(Math.atan2(offsetX, offsetZ));
        // the z rotation doubles up on the -y axis instead of negating it like the positive axis. idk how to fix
        // for now we remove them before it gets to look too weird
        if (yAngle < -1) doCollisionAnim = true;

        quaternion.rotateZ(Mth.lerp(tickPercent, this.oRoll, this.roll));
        this.renderRotatedQuad(h, quaternion, offsetX, offsetY, offsetZ, tickPercent);
    }

    //FIXME: particle invisible when horizontal velocity is 0
    public void renderRelativeVelocityQuad(/*? if >=1.21.9 {*//*QuadParticleRenderState*//*?} else {*/VertexConsumer/*?}*/ h, Camera camera, float tickPercent) {
        Vec3 camPos = camera.getPosition();
        float offsetX = (float) (Mth.lerp(tickPercent, this.xo, this.x) - camPos.x());
        float offsetY = (float) (Mth.lerp(tickPercent, this.yo, this.y) - camPos.y());
        float offsetZ = (float) (Mth.lerp(tickPercent, this.zo, this.z) - camPos.z());

        // get velocity
        final Vec3 camD = Minecraft.getInstance().getCameraEntity().getDeltaMovement();
        Vector3f deltaMotion = new Vector3f((float) (this.xd - camD.x), (float) (this.yd - camD.y), (float) (this.zd - camD.z));
        // calculate velocity angle
        final float angle = Math.acos(new Vector3f(deltaMotion).normalize().y);
        Vector3f axis = new Vector3f(-deltaMotion.z(), 0, deltaMotion.x()).normalize();
        Quaternionf quaternion = new Quaternionf(new AxisAngle4f(-angle, axis));
        // rotate to face camera
        Vector3f transformedOffset = new Vector3f(offsetX, offsetY, offsetZ);
        transformedOffset.rotateAxis(angle, axis.x, axis.y, axis.z);
        quaternion.mul(Axis.YP.rotation(Math.atan2(transformedOffset.x, transformedOffset.z) + Mth.PI));
        // decide particle length from speed or collision progress
        float stretchFactor = Mth.clamp(deltaMotion.lengthSquared(), 0.25F, 1.0F);
        if (doCollisionAnim) {
            float collisionProg = Mth.lerp(tickPercent, oCollisionAnimProgress, collisionAnimProgress);
            if (collisionProg < stretchFactor) stretchFactor = collisionProg;
        }
        // bung it in the oven
        renderSquishyRotatedQuad(h, quaternion, offsetX, offsetY, offsetZ, tickPercent, stretchFactor);
    }
    //FIXME: particle invisible when horizontal velocity is 0
    public void renderWorldVelocityQuad(/*? if >=1.21.9 {*//*QuadParticleRenderState*//*?} else {*/VertexConsumer/*?}*/ h, Camera camera, float tickPercent) {
        Vec3 camPos = camera.getPosition();
        float offsetX = (float) (Mth.lerp(tickPercent, this.xo, this.x) - camPos.x());
        float offsetY = (float) (Mth.lerp(tickPercent, this.yo, this.y) - camPos.y());
        float offsetZ = (float) (Mth.lerp(tickPercent, this.zo, this.z) - camPos.z());

        // get velocity
        Vector3f deltaMotion = new Vector3f((float) xd, (float) yd, (float) zd);
        // calculate velocity angle
        final float angle = Math.acos(new Vector3f(deltaMotion).normalize().y);
        Vector3f axis = new Vector3f(-deltaMotion.z(), 0, deltaMotion.x()).normalize();
        Quaternionf quaternion = new Quaternionf(new AxisAngle4f(-angle, axis));
        // rotate to face camera
        Vector3f transformedOffset = new Vector3f(offsetX, offsetY, offsetZ);
        transformedOffset.rotateAxis(angle, axis.x, axis.y, axis.z);
        quaternion.mul(Axis.YP.rotation(Math.atan2(transformedOffset.x, transformedOffset.z) + Mth.PI));
        // decide particle length from speed or collision progress
        float stretchFactor = Mth.clamp(deltaMotion.lengthSquared(), 0.25F, 1.0F);
        if (doCollisionAnim) {
            float collisionProg = Mth.lerp(tickPercent, oCollisionAnimProgress, collisionAnimProgress);
            if (collisionProg < stretchFactor) stretchFactor = collisionProg;
        }
        // bung it in the oven
        renderSquishyRotatedQuad(h, quaternion, offsetX, offsetY, offsetZ, tickPercent, stretchFactor);
    }

    public void renderCameraCopyQuad(/*? if >=1.21.9 {*//*QuadParticleRenderState*//*?} else {*/VertexConsumer/*?}*/ h, Camera camera, float tickPercent) {
        Vec3 camPos = camera.getPosition();
        float offsetX = (float) (Mth.lerp(tickPercent, this.xo, this.x) - camPos.x());
        float offsetY = (float) (Mth.lerp(tickPercent, this.yo, this.y) - camPos.y());
        float offsetZ = (float) (Mth.lerp(tickPercent, this.zo, this.z) - camPos.z());

        Quaternionf quaternion = new Quaternionf(camera.rotation());
        if (roll != 0) quaternion.rotateZ(Mth.lerp(tickPercent, oRoll, roll));
        //? if <= 1.20.1 {
        quaternion.mul(Axis.YP.rotation(Mth.PI));
        //?}
        this.renderRotatedQuad(h, quaternion, offsetX, offsetY, offsetZ, tickPercent);
    }

    //? if >=1.21.9 {
    /*public void renderRotatedQuad(QuadParticleRenderState h, Quaternionf quaternion, float offsetX, float offsetY, float offsetZ, float tickPercent) {
        this.extractRotatedQuad(h, quaternion, offsetX, offsetY, offsetZ, tickPercent);
    }
    *///?}

    private void renderSquishyRotatedQuad(/*? if >=1.21.9 {*//*QuadParticleRenderState*//*?} else {*/VertexConsumer/*?}*/ h, Quaternionf quaternion, float x, float y, float z, float tickPercent, float squish) {
        //? if >=1.21.9 {
        /*this.extractRotatedQuad(h, quaternion, x, y, z, tickPercent);
        // doesnt seem to be an easy way to dig into a particles size now.
        // im thinking of instead using the perspective trick of rotating the particle such that it looks stretched without actually being so
        *///?} else {
        float size = this.getQuadSize(tickPercent);
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int color = this.getLightColor(tickPercent);
        this.renderVertex(h, quaternion, x, y, z,  1.0F, -squish, size, u1, v1, color);
        this.renderVertex(h, quaternion, x, y, z,  1.0F,  squish, size, u1, v0, color);
        this.renderVertex(h, quaternion, x, y, z, -1.0F,  squish, size, u0, v0, color);
        this.renderVertex(h, quaternion, x, y, z, -1.0F, -squish, size, u0, v1, color);
        //?}
    }

    //? if <1.21.9 {
    private void renderVertex(VertexConsumer buffer, Quaternionf quaternion, float x, float y, float z, float xOffset, float yOffset, float quadSize, float u, float v, int packedLight) {
        //? if <= 1.20.1 {
        Vector3f vec = (new Vector3f(xOffset, yOffset, 0.0F)).rotate(quaternion).mul(quadSize).add(x, y, z);
        buffer.vertex(vec.x(), vec.y(), vec.z()).uv(u, v).color(rCol, gCol, bCol, alpha).uv2(packedLight).endVertex();
        //?} else {
        /*SingleQuadParticleAccessor p = (SingleQuadParticleAccessor) this;
        p.callRenderVertex(buffer, quaternion, x, y, z, xOffset, yOffset, quadSize, u, v, packedLight);
        *///?}
    }
    //?}

    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {
        ConfigData.ParticleData opts;

        public DefaultFactory(ConfigData.ParticleData opts) {
            this.opts = opts;
        }

        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ/*? if >=1.21.9 {*//*, RandomSource random*//*?}*/) {
            // grab latest particle options before spawning particle
            for (ConfigData.ParticleData options : config.particles) {
                if (opts.id.equals(options.id)) opts = options;
            }
            return new CustomParticle(level, x, y, z, opts);
        }
    }
}
