package pigcart.particlerain.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.joml.Math;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.StonecutterUtil;
import pigcart.particlerain.TextureUtil;
import pigcart.particlerain.config.ModConfig;
import pigcart.particlerain.mixin.access.ParticleEngineAccessor;
//? if > 1.20.1 {
/*import pigcart.particlerain.mixin.access.SingleQuadParticleAccessor;
*///?}
import pigcart.particlerain.particle.render.BlendedParticleRenderType;

import java.awt.*;

import static pigcart.particlerain.config.ModConfig.CONFIG;

public class CustomParticle extends WeatherParticle {

    //TODO reminders to check on mod compatibility
    // iris
    // cool rain (sounds)
    // culling mods
    // asyncparticles
    // create (asyncparticles?)
    // valkyrian skies (asyncparticles?)
    // generic biome mods (bop, natures spirit, ad astra)
    // WEATHER REPLACEMENTS
    // terrafirmacraft
    // simple clouds
    // storms & tornadoes
    // WIND
    // wilder wilds
    // immersive winds
    // physics mod maybe

    ModConfig.ParticleOptions opts;
    private float oCollisionAnimProgress = 1;
    private float collisionAnimProgress = 1;
    private float speed = 0;
    private final float rotationVariation;

    public CustomParticle(ClientLevel level, double x, double y, double z, ModConfig.ParticleOptions opts) {
        super(level, x, y, z, opts.gravity, opts.opacity, opts.size, opts.windStrength, opts.stormWindStrength);
        this.opts = opts;
        this.lifetime = opts.lifetime;
        ParticleEngineAccessor particleEngine = (ParticleEngineAccessor) Minecraft.getInstance().particleEngine;
        this.setSprite(particleEngine.getTextureAtlas().getSprite(StonecutterUtil.parseResourceLocation(opts.spriteLocations.get(level.random.nextInt(opts.spriteLocations.size())))));
        this.rotationVariation = opts.rotationAmount * ((random.nextFloat() - 0.5F) * 2.0F);
        if (opts.constantScreenSize) {
            this.quadSize = getDistanceSize();
        } else {
            this.quadSize = opts.size;
        }
        switch (opts.tintType) {
            case CUSTOM ->
                    setColor(opts.customTint.getRed() / 255F, opts.customTint.getGreen() / 255F, opts.customTint.getBlue() / 255F);
            case FOG -> {
                Color color = new Color(this.level.getBiome(this.pos).value().getFogColor()).darker();
                setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
            }
            case WATER -> {
                if (CONFIG.compat.waterTint) TextureUtil.applyWaterTint(this, level, this.pos);
            }
            case MAP -> {
                Color color = StonecutterUtil.getMapColor(level, pos);
                setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
            }
        }
    }

    public void tick() {
        super.tick();
        speed = (float) new Vec3(xd, yd, zd).length();
        if (opts.constantScreenSize && !doCollisionAnim) quadSize = getDistanceSize();
        if (opts.rotationAmount != 0) {
            oRoll = roll;
            roll += rotationVariation * speed;
        }
        tickWind();
    }

    public void tickWind() {
        float frequency = CONFIG.wind.gustFrequency;
        float shift = ParticleRain.clientTicks * CONFIG.wind.modulationSpeed;
        float variance = CONFIG.wind.strengthVariance;
        float strength = CONFIG.wind.strength;
        float multiplier = level.isThundering() ? opts.stormWindStrength : opts.windStrength;
        this.xd = (((Mth.sin((float)x * frequency + shift) * variance) + variance + strength) * multiplier) * yLevelWindAdjustment(y);
        this.zd = (((Mth.sin((float)z * frequency + shift) * variance) + variance + strength) * multiplier) * yLevelWindAdjustment(y);
    }

    @Override
    public void onPositionUpdate() {
        super.onPositionUpdate();
        testForCollisions();
    }

    public void testForCollisions() {
        float length = quadSize;
        if (opts.rotationType.equals(ModConfig.RotationType.RELATIVE_VELOCITY)) {
            final Vec3 camD = Minecraft.getInstance().cameraEntity.getDeltaMovement();
            Vector3f deltaMotion = new Vector3f((float) (this.xd - camD.x), (float) (this.yd - camD.y), (float) (this.zd - camD.z));
            length *= Mth.clamp(deltaMotion.lengthSquared(), 0.2F, 1.0F);
        }
        Vec3 quadCenterPos = new Vec3(x, y, z);
        Vec3 quadEdgePos = new Vec3(xd, yd, zd).normalize().multiply(length, length, length).add(x, y, z);
        final BlockHitResult hitResult = level.clip(StonecutterUtil.getClipContext(quadCenterPos, quadEdgePos));
        if (!hitResult.getType().equals(HitResult.Type.MISS) && !doCollisionAnim) {
            collision = hitResult;
            doCollisionAnim = true;
        }
    }

    @Override
    public void tickCollisionAnim() {
        //TODO 
        oCollisionAnimProgress = collisionAnimProgress;
        collisionAnimProgress -= speed;
        if (!opts.rotationType.equals(ModConfig.RotationType.RELATIVE_VELOCITY)) {
            quadSize -= speed;
        }
        if (oCollisionAnimProgress <= 0) remove();
    }

    public float getDistanceSize() {
        return distance * opts.size;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return switch (opts.renderType) {
            case TRANSLUCENT -> ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
            case OPAQUE -> ParticleRenderType.PARTICLE_SHEET_OPAQUE;
            case TERRAIN -> ParticleRenderType.TERRAIN_SHEET;
            case BLENDED -> BlendedParticleRenderType.INSTANCE;
        };
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float tickPercent) {
        Vec3 camPos = camera.getPosition();
        float offsetX = (float) (Mth.lerp(tickPercent, this.xo, this.x) - camPos.x());
        float offsetY = (float) (Mth.lerp(tickPercent, this.yo, this.y) - camPos.y());
        float offsetZ = (float) (Mth.lerp(tickPercent, this.zo, this.z) - camPos.z());
        // idk if it would be better to use a BiConsumer<vertexConsumer, tickPercent> for each case set on init instead of switch here
        switch (opts.rotationType) {
            case COPY_CAMERA -> {
                Quaternionf quaternion = new Quaternionf(camera.rotation());
                if (roll != 0) quaternion.rotateZ(Mth.lerp(tickPercent, oRoll, roll));
                //? if <= 1.20.1 {
                quaternion.mul(Axis.YP.rotation(Mth.PI));
                //?}
                this.renderRotatedQuad(vertexConsumer, quaternion, offsetX, offsetY, offsetZ, tickPercent);
            }
            case RELATIVE_VELOCITY -> { //FIXME: particle invisible when wind is 0
                // get velocity
                final Vec3 camD = Minecraft.getInstance().cameraEntity.getDeltaMovement();
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
                float stretchFactor = Mth.clamp(deltaMotion.lengthSquared(), 0.2F, 1.0F);
                if (doCollisionAnim) {
                    float collisionProg = Mth.lerp(tickPercent, oCollisionAnimProgress, collisionAnimProgress);
                    if (collisionProg < stretchFactor) stretchFactor = collisionProg;
                }
                // bung it in the oven
                renderSquishyRotatedQuad(vertexConsumer, quaternion, offsetX, offsetY, offsetZ, tickPercent, stretchFactor);
            }
            case LOOKAT_PLAYER -> {
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
                this.renderRotatedQuad(vertexConsumer, quaternion, offsetX, offsetY, offsetZ, tickPercent);
            }
            case FLAT_PLANES -> {
                Quaternionf quaternion = new Quaternionf(new AxisAngle4d(Mth.HALF_PI, -1, 0, 0));

                quaternion.rotateZ(Mth.lerp(tickPercent, this.oRoll, this.roll));
                turnBackfaceFlipways(quaternion, new Vector3f(offsetX, offsetY, offsetZ));
                this.renderRotatedQuad(vertexConsumer, quaternion, offsetX, offsetY, offsetZ, tickPercent);
            }
        }
    }
    private void renderSquishyRotatedQuad(VertexConsumer vertexConsumer, Quaternionf quaternion, float x, float y, float z, float tickPercent, float squish) {
        float size = this.getQuadSize(tickPercent);
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int color = this.getLightColor(tickPercent);
        this.renderVertex(vertexConsumer, quaternion, x, y, z,  1.0F, -squish, size, u1, v1, color);
        this.renderVertex(vertexConsumer, quaternion, x, y, z,  1.0F,  squish, size, u1, v0, color);
        this.renderVertex(vertexConsumer, quaternion, x, y, z, -1.0F,  squish, size, u0, v0, color);
        this.renderVertex(vertexConsumer, quaternion, x, y, z, -1.0F, -squish, size, u0, v1, color);
    }

    private void renderVertex(VertexConsumer buffer, Quaternionf quaternion, float x, float y, float z, float xOffset, float yOffset, float quadSize, float u, float v, int packedLight) {
        //? if <= 1.20.1 {
        Vector3f vector3f = (new Vector3f(xOffset, yOffset, 0.0F)).rotate(quaternion).mul(quadSize).add(x, y, z);
        buffer.vertex(vector3f.x(), vector3f.y(), vector3f.z()).uv(u, v).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(packedLight).endVertex();
        //?} else {
        /*SingleQuadParticleAccessor p = (SingleQuadParticleAccessor) this;
        p.callRenderVertex(buffer, quaternion, x, y, z, xOffset, yOffset, quadSize, u, v, packedLight);
        *///?}
    }

    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {
        ModConfig.ParticleOptions opts;

        public DefaultFactory(ModConfig.ParticleOptions opts) {
            this.opts = opts;
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            // grab latest particle options before spawning particle
            for (ModConfig.ParticleOptions options : CONFIG.customParticles) {
                if (opts.id.equals(options.id)) opts = options;
            }
            return new CustomParticle(level, x, y, z, opts);
        }
    }
}
