package pigcart.particlerain.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pigcart.particlerain.config.ConfigData;

import java.awt.*;
import java.util.Optional;

import static pigcart.particlerain.config.ConfigManager.config;

public class MistParticle extends WeatherParticle {

    private MistParticle(ClientLevel level, double x, double y, double z, SpriteSet provider) {
        super(level, x, y, z);

        this.y = ((int) y) + random.nextFloat();

        this.setSprite(config.mist.renderStyle.getSprite());
        this.quadSize = config.mist.size;
        this.setSize(quadSize, 0.2F);
        this.targetOpacity = config.mist.opacity;
        this.lifetime = config.mist.lifetime;
        this.alpha = 0;

        Color color = new Color(this.level.getBiome(this.pos).value().getFogColor());
        this.rCol = color.getRed() / 255F;
        this.gCol = color.getGreen() / 255F;
        this.bCol = color.getBlue() / 255F;

        this.roll = Mth.HALF_PI * level.random.nextInt(4);
        this.oRoll = this.roll;
    }

    @Override
    public void tick() {
        super.tick();
        int halfLife = lifetime / 2;
        this.alpha = (age < halfLife ? (float) age / (halfLife) : (float) (-age + lifetime) / halfLife) * config.mist.opacity;
        if (config.mist.renderStyle == ConfigData.MistOptions.RenderStyle.DITHERED) quadSize = (distance * 0.1F) * config.mist.size;
        if (distance > config.perf.surfaceRange) remove();
    }

    @Override
    public void tickDistanceFade() {
        //dont
    }

    @Override
    public Optional<ParticleGroup> getParticleGroup() {
        return Optional.empty();
    }

    @Override
    public ParticleRenderType getRenderType() {
        // if IrisApi.isShaderPackInUse() return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        return config.mist.renderStyle.getRenderType();
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
