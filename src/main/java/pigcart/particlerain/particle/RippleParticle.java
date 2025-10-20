package pigcart.particlerain.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Math;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.VersionUtil;
import pigcart.particlerain.mixin.access.ParticleEngineAccessor;
//? if >=1.21.9 {
/*import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.util.RandomSource;
*///?} else {
import com.mojang.blaze3d.vertex.VertexConsumer;
//?}

import static pigcart.particlerain.config.ConfigManager.config;

public class RippleParticle extends WeatherParticle {

    private RippleParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z, VersionUtil.getSprite(VersionUtil.getId("ripple_0")));
        this.x = Math.round(this.x / (1F / 16F)) * (1F / 16F);
        this.z = Math.round(this.z / (1F / 16F)) * (1F / 16F);
        this.quadSize = config.ripple.size;
        this.alpha = config.ripple.opacity;
    }

    @Override
    public void tickDistanceFade() {
        //dont
    }

    @Override
    public void tick() {
        super.tick();
        this.alpha = Mth.lerp(this.age / 9F, config.ripple.opacity, 0);
        if (this.age > 8) this.remove();
        this.setSprite(VersionUtil.getSprite(VersionUtil.getId("ripple_" + (this.age - 1))));
    }

    @Override
    public void /*? if >=1.21.9 {*//*extract(QuadParticleRenderState*//*?} else {*/render(VertexConsumer/*?}*/ h, Camera camera, float f) {
        Vec3 camPos = camera.getPosition();
        float x = (float) (Mth.lerp(f, this.xo, this.x) - camPos.x());
        float y = (float) (Mth.lerp(f, this.yo, this.y) - camPos.y());
        float z = (float) (Mth.lerp(f, this.zo, this.z) - camPos.z());

        Quaternionf quaternion = new Quaternionf(new AxisAngle4d(Mth.HALF_PI, -1, 0, 0));
        this.turnBackfaceFlipways(quaternion, new Vector3f(x, y, z));
        this.renderRotatedQuad(h, quaternion, x, y, z, f);
    }

    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        public DefaultFactory(SpriteSet provider) {
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ/*? if >=1.21.9 {*//*, RandomSource random*//*?}*/) {
            return new RippleParticle(level, x, y, z);
        }
    }
}
