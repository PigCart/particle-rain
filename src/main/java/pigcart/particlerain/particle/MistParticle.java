package pigcart.particlerain.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pigcart.particlerain.config.ConfigData;
//? if >=1.21.9 {
/*import net.minecraft.core.particles.ParticleLimit;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
*///?} else {
import net.minecraft.core.particles.ParticleGroup;
import com.mojang.blaze3d.vertex.VertexConsumer;
//?}

import java.awt.*;
import java.util.Optional;

import static pigcart.particlerain.config.ConfigManager.config;

public class MistParticle extends WeatherParticle {

    private MistParticle(ClientLevel level, double x, double y, double z, SpriteSet provider) {
        super(level, x, y, z, config.mist.renderStyle.getSprite());

        this.y = ((int) y) + random.nextFloat();

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
    //? if >=1.21.9 {
    /*public Optional<ParticleLimit> getParticleLimit() {
    *///?} else {
    public Optional<ParticleGroup> getParticleGroup() {
    //?}
        return Optional.empty();
    }

    @Override
    //? if >=1.21.9 {
    /*public SingleQuadParticle.Layer getLayer() {
    *///?} else {
    public ParticleRenderType getRenderType() {
    //?}
        // if >=1.21.5 & IrisApi.isShaderPackInUse() return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        return config.mist.renderStyle.getRenderType();
    }

    @Override
    public void /*? if >=1.21.9 {*//*extract(QuadParticleRenderState*//*?} else {*/render(VertexConsumer/*?}*/ h, Camera camera, float tickPercent) {
        Vec3 camPos = camera.getPosition();
        float x = (float) (Mth.lerp(tickPercent, this.xo, this.x) - camPos.x());
        float y = (float) (Mth.lerp(tickPercent, this.yo, this.y) - camPos.y());
        float z = (float) (Mth.lerp(tickPercent, this.zo, this.z) - camPos.z());

        Quaternionf quaternion = new Quaternionf(new AxisAngle4d(Mth.HALF_PI, -1, 0, 0));

        quaternion.rotateZ(Mth.lerp(tickPercent, this.oRoll, this.roll));
        turnBackfaceFlipways(quaternion, new Vector3f(x, y, z));
        this.renderRotatedQuad(h, quaternion, x, y, z, tickPercent);
    }

    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet provider;

        public DefaultFactory(SpriteSet provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ/*? if >=1.21.9 {*//*, RandomSource random*//*?}*/) {
            return new MistParticle(level, x, y, z, this.provider);
        }
    }
}
