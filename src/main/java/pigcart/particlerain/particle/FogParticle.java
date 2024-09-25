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
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
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
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
        //TODO: have fog face the camera position instead of copying its rotation
        Quaternionf quaternionf = new Quaternionf();
        quaternionf.rotateTo((float) this.x, (float) this.y, (float) this.z, (float) camera.getPosition().x, (float) camera.getPosition().y, (float) camera.getPosition().z);
        this.renderRotatedQuad(vertexConsumer, camera, quaternionf, f);
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
