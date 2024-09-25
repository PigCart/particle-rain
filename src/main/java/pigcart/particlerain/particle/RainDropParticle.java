package pigcart.particlerain.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pigcart.particlerain.ParticleRainClient;

public class RainDropParticle extends WeatherParticle {

    protected RainDropParticle(ClientLevel clientWorld, double x, double y, double z, SpriteSet provider) {
        super(clientWorld, x, y, z, ParticleRainClient.config.rainDropGravity, provider);

        this.rCol = ParticleRainClient.config.color.rainRed;
        this.gCol = ParticleRainClient.config.color.rainGreen;
        this.bCol = ParticleRainClient.config.color.rainBlue;
        //TODO: Match biome water tint

        this.xd = gravity / 3;
        this.zd = gravity / 3;

        this.lifetime = ParticleRainClient.config.particleRadius * 5;
        this.quadSize = ParticleRainClient.config.size.rainDropSize;
    }

    @Override
    public void tick() {
        super.tick();
        this.xd = gravity / 3;
        this.zd = gravity / 3;
        //TODO: variable wind/angle

        if (this.onGround || this.removeIfObstructed()) {
            if (this.isHotBlock()) {
                Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.SMOKE, this.x, this.y, this.z, 0, 0, 0);
            } else {
                Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.RAIN, this.x, this.y, this.z, 0, 0, 0);
            }
            this.remove();
        } else if (!this.level.getFluidState(this.pos).isEmpty()) {
            this.remove();
        }
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float tickPercentage) {

        Vec3 camPos = camera.getPosition();
        float x = (float)(Mth.lerp((double)tickPercentage, this.xo, this.x) - camPos.x());
        float y = (float)(Mth.lerp((double)tickPercentage, this.yo, this.y) - camPos.y());
        float z = (float)(Mth.lerp((double)tickPercentage, this.zo, this.z) - camPos.z());

        // Can't figure out how to make this work

        /*
        // technically not correct when rain angle is used, but the backfacing isnt too noticeable at shallow angles!
        Quaternionf quaternion = new Quaternionf(new AxisAngle4f(0.3f, -1, 0, 1));
        quaternion.mul(Axis.YP.rotation((float) Math.atan2(x, z) + Mth.PI));
        this.renderRotatedQuad(vertexConsumer, quaternion, x, y, z, tickPercentage);*/

        // Using old implementation
        Quaternionf quaternion = new Quaternionf(new AxisAngle4f(0.3f, -1, 0, 1));
        quaternion.mul(camera.rotation());
        quaternion.mul(Axis.XN.rotationDegrees(camera.getXRot()));
        quaternion.mul(Axis.YP.rotationDegrees(camera.getYRot()));
        quaternion.mul(Axis.YP.rotation((float) Math.atan2(x, z)));

        float quadSize = this.getQuadSize(tickPercentage);
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int lightColor = this.getLightColor(tickPercentage);

        Vector3f[] vector3fs = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)};

        for(int k = 0; k < 4; ++k) {
            Vector3f vector3f = vector3fs[k];
            vector3f.rotate(quaternion);
            vector3f.mul(quadSize);
            vector3f.add(x, y, z);
        }

        vertexConsumer.vertex((double)vector3fs[0].x(), (double)vector3fs[0].y(), (double)vector3fs[0].z()).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();
        vertexConsumer.vertex((double)vector3fs[1].x(), (double)vector3fs[1].y(), (double)vector3fs[1].z()).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();
        vertexConsumer.vertex((double)vector3fs[2].x(), (double)vector3fs[2].y(), (double)vector3fs[2].z()).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();
        vertexConsumer.vertex((double)vector3fs[3].x(), (double)vector3fs[3].y(), (double)vector3fs[3].z()).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet provider;

        public DefaultFactory(SpriteSet provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new RainDropParticle(level, x, y, z, this.provider);
        }
    }
}