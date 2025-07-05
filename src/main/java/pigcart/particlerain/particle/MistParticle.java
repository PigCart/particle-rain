package pigcart.particlerain.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pigcart.particlerain.WeatherParticleManager;
import pigcart.particlerain.particle.render.BlendedParticleRenderType;

import java.awt.*;

import static pigcart.particlerain.config.ModConfig.CONFIG;

public class MistParticle extends WeatherParticle {

    float xdxd;
    float zdzd;

    private MistParticle(ClientLevel level, double x, double y, double z, SpriteSet provider) {
        super(level, x, y, z, 0, CONFIG.mist.opacity, CONFIG.mist.size, CONFIG.mist.windStrength, CONFIG.mist.stormWindStrength);

        WeatherParticleManager.fogCount++;
        this.setSprite(provider.get(level.getRandom()));
        this.setSize(CONFIG.mist.size + 1, CONFIG.mist.size + 1);

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
        if (level.getBlockState(pos).isSolid()) this.remove();
        this.xd = this.xdxd;
        this.zd = this.zdzd;
    }

    public void remove() {
        if (this.isAlive()) WeatherParticleManager.fogCount--;
        super.remove();
    }

    @Override
    public ParticleRenderType getRenderType() {
        if (targetOpacity == 1F) {
            return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
        } else {
            // if IrisApi.isShaderPackInUse() return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
            return BlendedParticleRenderType.INSTANCE;
        }
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float tickPercent) {
        Vec3 camPos = camera.getPosition();
        float x = (float) (Mth.lerp(tickPercent, this.xo, this.x) - camPos.x());
        float y = (float) (Mth.lerp(tickPercent, this.yo, this.y) - camPos.y());
        float z = (float) (Mth.lerp(tickPercent, this.zo, this.z) - camPos.z());

        Quaternionf quaternion = new Quaternionf(new AxisAngle4d(Mth.HALF_PI, -1, 0, 0));

        quaternion.rotateZ(Mth.lerp(tickPercent, this.oRoll, this.roll));
        turnBackfaceFlipways(quaternion, new Vector3f(x, y, z));
        this.renderRotatedQuad(vertexConsumer, quaternion, x, y, z, tickPercent);
    }

    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet provider;

        public DefaultFactory(SpriteSet provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new MistParticle(level, x, y, z, this.provider);
        }
    }
}
