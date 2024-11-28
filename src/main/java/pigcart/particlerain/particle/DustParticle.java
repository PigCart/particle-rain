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
import pigcart.particlerain.ParticleRainClient;

public class DustParticle extends DustMoteParticle {

    protected DustParticle(ClientLevel clientWorld, double x, double y, double z, SpriteSet provider) {
        super(clientWorld, x, y, z, provider);
        this.quadSize = ParticleRainClient.config.sand.size;
        this.gravity = ParticleRainClient.config.sand.gravity - 0.1F;
        if (ParticleRainClient.config.sand.spawnOnGround) this.yd = 0.1F;
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
        float x = (float) (Mth.lerp(tickPercentage, this.xo, this.x) - camPos.x());
        float y = (float) (Mth.lerp(tickPercentage, this.yo, this.y) - camPos.y());
        float z = (float) (Mth.lerp(tickPercentage, this.zo, this.z) - camPos.z());

        Quaternionf quaternion = camera.rotation();
        y = y + Mth.sin((Mth.lerp(tickPercentage, this.age - 1.0F, this.age)) / 20) + 1.5F;
        this.renderRotatedQuad(vertexConsumer, quaternion, x, y, z, tickPercentage);
    }
    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet provider;

        public DefaultFactory(SpriteSet provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new DustParticle(level, x, y, z, this.provider);
        }
    }
}
