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
        float x = (float) (Mth.lerp(tickPercentage, this.xo, this.x) - camPos.x());
        float y = (float) (Mth.lerp(tickPercentage, this.yo, this.y) - camPos.y());
        float z = (float) (Mth.lerp(tickPercentage, this.zo, this.z) - camPos.z());

        // technically not correct when rain angle is used, but the backfacing isnt too noticeable at shallow angles!
        Quaternionf quaternion = new Quaternionf(new AxisAngle4f(0.3f, -1, 0, 1));
        quaternion.mul(Axis.YP.rotation((float) Math.atan2(x, z) + Mth.PI));
        this.renderRotatedQuad(vertexConsumer, quaternion, x, y, z, tickPercentage);
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