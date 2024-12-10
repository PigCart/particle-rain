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
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Math;
import pigcart.particlerain.ParticleRainClient;

public class RippleParticle extends WeatherParticle {

    private RippleParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.setSprite(Minecraft.getInstance().particleEngine.textureAtlas.getSprite(ResourceLocation.fromNamespaceAndPath(ParticleRainClient.MOD_ID, "ripple0")));
        this.quadSize = 0.25F;
        this.alpha = 0.1F;
        this.x = Math.round(this.x / (1F / 16F)) * (1F / 16F);
        this.z = Math.round(this.z / (1F / 16F)) * (1F / 16F);
    }

    @Override
    public void tick() {
        super.tick();
        this.alpha = Mth.lerp(this.age / 9F, 0.3F, 0F);
        if (this.age > 8) this.remove();
        this.setSprite(Minecraft.getInstance().particleEngine.textureAtlas.getSprite(ResourceLocation.fromNamespaceAndPath(ParticleRainClient.MOD_ID, "ripple" + (this.age - 1))));
    }

    @Override
    public void fadeIn() {
        //dont
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
        Vec3 camPos = camera.getPosition();
        float x = (float) (Mth.lerp(f, this.xo, this.x) - camPos.x());
        float y = (float) (Mth.lerp(f, this.yo, this.y) - camPos.y());
        float z = (float) (Mth.lerp(f, this.zo, this.z) - camPos.z());

        Quaternionf quaternion = new Quaternionf(new AxisAngle4d(Mth.HALF_PI, -1, 0, 0));
        this.flipItTurnwaysIfBackfaced(quaternion, new Vector3f(x, y, z));
        this.renderRotatedQuad(vertexConsumer, quaternion, x, y, z, f);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        public DefaultFactory(SpriteSet provider) {
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new RippleParticle(level, x, y, z);
        }
    }
}
