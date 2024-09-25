package pigcart.particlerain.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pigcart.particlerain.ParticleRainClient;

public class DustSheetParticle extends DustMoteParticle {

    protected DustSheetParticle(ClientLevel clientWorld, double x, double y, double z, SpriteSet provider) {
        super(clientWorld, x, y, z, provider);
        this.quadSize = ParticleRainClient.config.size.dustSheetSize;
        this.gravity = ParticleRainClient.config.desertDustGravity - 0.1F;
    }
    @Override
    public void tick() {
        super.tick();
        if (this.onGround) {
            this.yd = 0.01F;
        }
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float tickPercentage) {

        Vec3 camPos = camera.getPosition();
        float x = (float)(Mth.lerp((double)tickPercentage, this.xo, this.x) - camPos.x());
        float y = (float)(Mth.lerp((double)tickPercentage, this.yo, this.y) - camPos.y());
        float z = (float)(Mth.lerp((double)tickPercentage, this.zo, this.z) - camPos.z());

        Quaternionf quaternion = camera.rotation();
        y = y + Mth.sin((Mth.lerp(tickPercentage, this.age - 1.0F, this.age)) / 20) + 1.5F;

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

    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet provider;

        public DefaultFactory(SpriteSet provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new DustSheetParticle(level, x, y, z, this.provider);
        }
    }
}
