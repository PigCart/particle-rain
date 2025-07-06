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
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.joml.Math;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.StonecutterUtil;
import pigcart.particlerain.TextureUtil;
import pigcart.particlerain.config.ModConfig;
import pigcart.particlerain.mixin.access.ParticleEngineAccessor;
import pigcart.particlerain.mixin.access.SingleQuadParticleAccessor;
import pigcart.particlerain.particle.render.BlendedParticleRenderType;

import java.awt.*;

import static pigcart.particlerain.config.ModConfig.CONFIG;

public class CustomParticle extends WeatherParticle {

    ModConfig.ParticleOptions opts;

    public CustomParticle(ClientLevel level, double x, double y, double z, ModConfig.ParticleOptions opts) {
        super(level, x, y, z, opts.gravity, opts.opacity, opts.size, opts.windStrength, opts.stormWindStrength);
        this.opts = opts;
        this.lifetime = opts.lifetime;
        ParticleEngineAccessor particleEngine = (ParticleEngineAccessor) Minecraft.getInstance().particleEngine;
        this.setSprite(particleEngine.getTextureAtlas().getSprite(StonecutterUtil.parseResourceLocation(opts.spriteLocations.get(level.random.nextInt(opts.spriteLocations.size())))));
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
        //if (this.distanceSquared < 1) {this.remove();return;}
        if (opts.constantScreenSize && !doCollisionAnim) quadSize = getDistanceSize();
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

    public float getDistanceSize() {
        return distanceSquared * (opts.size / 100);
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
                final Vector3f camD = Minecraft.getInstance().cameraEntity.getDeltaMovement().toVector3f();
                Vector3f deltaMotion = new Vector3f((float) this.xd - (camD.x), (float) this.yd - (camD.y), (float) this.zd - (camD.z));
                // calculate velocity angle
                final float angle = Math.acos(new Vector3f(deltaMotion).normalize().y);
                Vector3f axis = new Vector3f(-deltaMotion.z(), 0, deltaMotion.x()).normalize();
                Quaternionf quaternion = new Quaternionf(new AxisAngle4f(-angle, axis));
                // rotate to face camera
                Vector3f transformedOffset = new Vector3f(offsetX, offsetY, offsetZ);
                transformedOffset.rotateAxis(angle, axis.x, axis.y, axis.z);
                quaternion.mul(Axis.YP.rotation(Math.atan2(transformedOffset.x, transformedOffset.z) + Mth.PI));
                // get sensible stretch factor from speed
                float speed = Mth.clamp(deltaMotion.lengthSquared(), 0.2F, 1.0F);
                // bung it in the oven
                turnBackfaceFlipways(quaternion, new Vector3f(offsetX, offsetY, offsetZ));
                renderSquishyRotatedQuad(vertexConsumer, quaternion, offsetX, offsetY, offsetZ, tickPercent, speed);
            }
            case LOOKAT_PLAYER -> {
                Vector3f localPos = new Vector3f(offsetX, offsetY, offsetZ);
                // rotate particle around y axis to face player
                Quaternionf quaternion = Axis.YP.rotation((float) java.lang.Math.atan2(offsetX, offsetZ) + Mth.PI);
                // rotate particle by angle between y axis and camera location
                float yAngle = (float) java.lang.Math.asin(offsetY / localPos.length());
                quaternion.rotateX(yAngle);
                quaternion.rotateZ((float) java.lang.Math.atan2(offsetX, offsetZ));
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
