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
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pigcart.particlerain.ParticleRainClient;

public class FogParticle extends WeatherParticle {

    private FogParticle(ClientLevel level, double x, double y, double z, SpriteSet provider) {
        super(level, x, y, z, 0.2F, provider);
        this.lifetime = ParticleRainClient.config.particleRadius * 5;
        final double distance = Minecraft.getInstance().cameraEntity.position().distanceTo(new Vec3(x, y, z));
        this.quadSize = (float) (ParticleRainClient.config.size.FogSize / distance);

        if (level.getBiome(new BlockPos((int) this.x, (int) this.y, (int) this.z)).value().hasPrecipitation()) {
            this.rCol = 0.85F;
            this.gCol = 0.85F;
            this.bCol = 1.0F;
        }

        this.roll = level.random.nextFloat() * Mth.PI;
        this.oRoll = this.roll;

        this.xd = gravity / 3;
        this.zd = gravity / 3;
    }

    public void tick() {
        super.tick();
        final double camdist = Minecraft.getInstance().cameraEntity.position().distanceTo(new Vec3(x, y, z));
        this.quadSize = (float) camdist / 2;
        BlockState fallingTowards = level.getBlockState(this.pos.offset(3, -8, 3));
        BlockPos blockPos = this.pos.offset(2, -4, 2);
        if (level.getHeight(Heightmap.Types.MOTION_BLOCKING, blockPos.getX(), blockPos.getZ()) >= blockPos.getY() || !fallingTowards.getFluidState().isEmpty()) {
            if (!shouldFadeOut) {
                shouldFadeOut = true;
            }
        }
        if (onGround) {
            remove();
        } else {
            this.xd = gravity / 3;
            this.zd = gravity / 3;
        }
        this.lifetime++;
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
        //TODO: have fog face the camera position instead of copying its rotation
        Vec3 camPos = camera.getPosition();
        float x = (float) (Mth.lerp(f, this.xo, this.x) - camPos.x());
        float y = (float) (Mth.lerp(f, this.yo, this.y) - camPos.y());
        float z = (float) (Mth.lerp(f, this.zo, this.z) - camPos.z());

        //Quaternionf quaternion = Axis.YP.rotation((float) Math.atan2(x, z) + Mth.PI);
        Quaternionf quaternion = new Quaternionf();
        final int radius = ParticleRainClient.config.particleRadius;
        float yrot = Mth.HALF_PI * (y / radius);
        quaternion.rotateX(Mth.HALF_PI * (z / radius) + Mth.HALF_PI);
        quaternion.rotateY(-Mth.HALF_PI * (x / radius));
        // almost working, not quite there.
        this.renderRotatedQuad(vertexConsumer, quaternion, x, y, z, f);
        if (camera.getEntity().getRandom().nextFloat() < 0.001F) {
            //System.out.println(yrot);
        }
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
            return new FogParticle(level, x, y, z, this.provider);
        }
    }
}
