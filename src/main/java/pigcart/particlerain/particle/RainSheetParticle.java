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

public class RainSheetParticle extends WeatherParticle {

    protected RainSheetParticle(ClientLevel clientWorld, double x, double y, double z, SpriteSet provider) {
        super(clientWorld, x, y, z, ParticleRainClient.config.rainDropGravity, provider);

        this.rCol = ParticleRainClient.config.color.rainRed;
        this.gCol = ParticleRainClient.config.color.rainGreen;
        this.bCol = ParticleRainClient.config.color.rainBlue;

        this.xd = gravity / 3;
        this.zd = gravity / 3;

        this.lifetime = 200;
        this.quadSize = 2F;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.removeIfObstructed() || this.onGround) {
            if (this.isHotBlock()) {
                Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.SMOKE, this.x, this.y, this.z, 0, 0, 0);
            } else {
                Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.RAIN, this.x, this.y, this.z, 0, 0, 0);
            }
        } else if (Minecraft.getInstance().cameraEntity.position().distanceTo(this.pos.getCenter()) < ParticleRainClient.config.particleRadius / 2) {
            //remove();
        } else {
            this.xd = gravity / 3;
            this.zd = gravity / 3;
        }
    }

    @Override
    public void render(VertexConsumer builder, Camera camera, float tickPercentage) {
        Vec3 vec3 = camera.getPosition();
        float x = (float) (Mth.lerp(tickPercentage, this.xo, this.x) - vec3.x());
        float y = (float) (Mth.lerp(tickPercentage, this.yo, this.y) - vec3.y());
        float z = (float) (Mth.lerp(tickPercentage, this.zo, this.z) - vec3.z());
        // ideally rain angle is velocity based but the math for that is too complicated for little ol me :3
        Quaternionf quaternion = new Quaternionf(new AxisAngle4f(0.3f, -1, 0, 1));
        quaternion.mul(camera.rotation());
        quaternion.mul(Axis.XN.rotationDegrees(camera.getXRot()));
        quaternion.mul(Axis.YP.rotationDegrees(camera.getYRot()));
        quaternion.mul(Axis.YP.rotation((float) Math.atan2(x, z)));

        Vector3f[] vector3fs = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float k = this.getQuadSize(tickPercentage);

        for (int l = 0; l < 4; ++l) {
            Vector3f vector3f = vector3fs[l];
            vector3f.rotate(quaternion);
            vector3f.mul(k);
            vector3f.add(x -1, y + 1.5F, z - 1);
        }

        float l = this.getU0();
        float vector3f = this.getU1();
        float m = this.getV0();
        float n = this.getV1();
        int o = this.getLightColor(tickPercentage);
        // bottom right
        builder.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z()).uv(vector3f, n).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
        // top right
        builder.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z()).uv(vector3f, m).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
        // top left
        builder.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z()).uv(l, m).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
        // bottom left
        builder.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z()).uv(l, n).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
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
            return new RainSheetParticle(level, x, y, z, this.provider);
        }
    }
}