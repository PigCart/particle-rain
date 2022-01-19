package pigcart.particlerain.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import pigcart.particlerain.ParticleRainClient;

public class RainDropParticle extends WeatherParticle {

    private final BlockPos.MutableBlockPos fluidPos;

    protected RainDropParticle(ClientLevel clientWorld, double x, double y, double z, float red, float green, float blue, SpriteSet provider) {
        super(clientWorld, x, y, z, red, green, blue, ParticleRainClient.config.rainDropGravity, provider);

        this.lifetime = 200;
        this.quadSize = 0.5F;
        this.fluidPos = new BlockPos.MutableBlockPos();
    }

    @Override
    public void tick() {
        super.tick();
        this.fluidPos.set(this.x, this.y - 0.1, this.z);

        if (this.shouldRemove() || this.onGround || !this.level.getFluidState(this.fluidPos).isEmpty()) {
            this.remove();
            if (this.onGround)
                Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.RAIN, this.x, this.y, this.z, 0, 0, 0);
        }
    }

    @Override
    public void render(VertexConsumer builder, Camera camera, float f) {
        Vec3 vec3 = camera.getPosition();
        float x = (float) (Mth.lerp(f, this.xo, this.x) - vec3.x());
        float y = (float) (Mth.lerp(f, this.yo, this.y) - vec3.y());
        float z = (float) (Mth.lerp(f, this.zo, this.z) - vec3.z());
        Quaternion quaternion = new Quaternion(camera.rotation());
        quaternion.mul(Vector3f.XN.rotationDegrees(camera.getXRot()));
        quaternion.mul(Vector3f.YP.rotationDegrees(camera.getYRot()));
        quaternion.mul(Vector3f.YP.rotation((float) Math.atan2(x, z)));

        Vector3f[] vector3fs = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float k = this.getQuadSize(f);

        for (int l = 0; l < 4; ++l) {
            Vector3f vector3f = vector3fs[l];
            vector3f.transform(quaternion);
            vector3f.mul(k);
            vector3f.add(x, y, z);
        }

        float l = this.getU0();
        float vector3f = this.getU1();
        float m = this.getV0();
        float n = this.getV1();
        int o = this.getLightColor(f);
        builder.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z()).uv(vector3f, n).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
        builder.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z()).uv(vector3f, m).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
        builder.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z()).uv(l, m).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
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
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double red, double green, double blue) {
            return new RainDropParticle(level, x, y, z, (float) red, (float) green, (float) blue, this.provider);
        }
    }
}