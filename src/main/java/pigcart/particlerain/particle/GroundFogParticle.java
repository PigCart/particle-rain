package pigcart.particlerain.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pigcart.particlerain.ParticleRainClient;

import java.awt.*;

public class GroundFogParticle extends WeatherParticle {

    float xdxd;
    float zdzd;

    private GroundFogParticle(ClientLevel level, double x, double y, double z, SpriteSet provider) {
        super(level, x, y, z);
        ParticleRainClient.fogCount++;
        this.setSprite(provider.get(level.getRandom()));
        this.quadSize = ParticleRainClient.config.groundFog.size;
        this.lifetime = 30000;

        Color color = new Color(this.level.getBiome(this.pos).value().getFogColor());
        this.rCol = color.getRed() / 255F;
        this.gCol = color.getGreen() / 255F;
        this.bCol = color.getBlue() / 255F;

        this.roll = Mth.HALF_PI * level.random.nextInt(4);
        this.oRoll = this.roll;

        this.xdxd = (this.random.nextFloat() - 0.5F) / 100;
        this.zdzd = (this.random.nextFloat() - 0.5F) / 100;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.onGround) this.remove();
        this.xd = this.xdxd;
        this.zd = this.zdzd;
    }

    public void remove() {
        if (this.isAlive()) ParticleRainClient.fogCount--;
        super.remove();
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
        Vec3 camPos = camera.getPosition();
        float x = (float) (Mth.lerp(f, this.xo, this.x) - camPos.x());
        float y = (float) (Mth.lerp(f, this.yo, this.y) - camPos.y());
        float z = (float) (Mth.lerp(f, this.zo, this.z) - camPos.z());

        Quaternionf quaternion = new Quaternionf(new AxisAngle4d(Mth.HALF_PI, -1, 0, 0));

        quaternion.rotateZ(Mth.lerp(f, this.oRoll, this.roll));
        this.renderRotatedQuad(vertexConsumer, quaternion, x, y, z, f);
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
            return new GroundFogParticle(level, x, y, z, this.provider);
        }
    }
}
