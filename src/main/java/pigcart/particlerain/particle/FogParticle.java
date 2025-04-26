package pigcart.particlerain.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pigcart.particlerain.config.ModConfig;

import java.awt.*;

import static pigcart.particlerain.config.ModConfig.CONFIG;

public class FogParticle extends WeatherParticle {

    private FogParticle(ClientLevel level, double x, double y, double z, SpriteSet provider) {
        super(level, x, y, z, CONFIG.fog.gravity, CONFIG.fog.opacity, CONFIG.fog.size, CONFIG.fog.windStrength, CONFIG.fog.stormWindStrength);

        this.setSprite(provider.get(level.getRandom()));
        this.setSize(getSize(), getSize());

        Color color = new Color(this.level.getBiome(this.pos).value().getFogColor()).darker();
        this.rCol = color.getRed() / 255F;
        this.gCol = color.getGreen() / 255F;
        this.bCol = color.getBlue() / 255F;

        this.roll = level.random.nextFloat() * Mth.PI;
        this.oRoll = this.roll;
    }

    public float getSize() {
        final double distance = Minecraft.getInstance().cameraEntity.position().distanceTo(new Vec3(x, y, z));
        return (float) (distance * ModConfig.CONFIG.fog.size);
    }

    public void tick() {
        super.tick();
        this.quadSize = getSize();
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
        Vec3 camPos = camera.getPosition();
        float x = (float) (Mth.lerp(f, this.xo, this.x) - camPos.x());
        float y = (float) (Mth.lerp(f, this.yo, this.y) - camPos.y());
        float z = (float) (Mth.lerp(f, this.zo, this.z) - camPos.z());
        Vector3f localPos = new Vector3f(x, y, z);

        // rotate particle around y axis to face player
        Quaternionf quaternion = Axis.YP.rotation((float) Math.atan2(x, z) + Mth.PI);
        // rotate particle by angle between y axis and camera location
        float yAngle = (float) Math.asin(y / localPos.length());
        quaternion.rotateX(yAngle);
        quaternion.rotateZ((float) Math.atan2(x, z));
        // the z rotation doubles up on the -y axis instead of negating it like the positive axis. idk how to fix
        // for now we remove them before it gets to look too weird
        if (yAngle < -1) doCollisionAnim = true;

        quaternion.rotateZ(Mth.lerp(f, this.oRoll, this.roll));
        this.renderRotatedQuad(vertexConsumer, quaternion, x, y, z, f);
    }

    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet provider;

        public DefaultFactory(SpriteSet provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new FogParticle(level, x, y, z, this.provider);
        }
    }
}
