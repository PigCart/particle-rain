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
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.AxisAngle4f;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pigcart.particlerain.ParticleRainClient;

import java.awt.*;

public class RainParticle extends WeatherParticle {

    protected RainParticle(ClientLevel clientWorld, double x, double y, double z, SpriteSet provider) {
        super(clientWorld, x, y, z, ParticleRainClient.config.rain.gravity, provider);

        this.quadSize = ParticleRainClient.config.rain.sheetSize;
        ResourceLocation atlasLocation = ResourceLocation.withDefaultNamespace("textures/atlas/blocks.png");
        ResourceLocation spriteLocation = ResourceLocation.fromNamespaceAndPath(ParticleRainClient.MOD_ID, "rainn");
        TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getAtlas(atlasLocation).getSprite(spriteLocation);
        this.setSprite(sprite);

        final Color waterColor = new Color(BiomeColors.getAverageWaterColor(clientWorld, this.pos));
        final Color fogColor = new Color(this.level.getBiome(this.pos).value().getFogColor()).darker();
        this.rCol = (Mth.lerp(ParticleRainClient.config.rain.mix / 100F, waterColor.getRed(), fogColor.getRed()) / 255F);
        this.gCol = (Mth.lerp(ParticleRainClient.config.rain.mix / 100F, waterColor.getGreen(), fogColor.getGreen()) / 255F);
        this.bCol = (Mth.lerp(ParticleRainClient.config.rain.mix / 100F, waterColor.getBlue(), fogColor.getBlue()) / 255F);

        this.xd = gravity * ParticleRainClient.config.rain.windStrength;
        this.zd = gravity * ParticleRainClient.config.rain.windStrength;

        this.lifetime = ParticleRainClient.config.particleRadius * 5;
    }

    @Override
    public void tick() {
        super.tick();
         if (this.age < 10) this.alpha = Math.clamp(0, ParticleRainClient.config.rain.opacity / 100F, this.alpha);
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
        Vector3f camPos = camera.getPosition().toVector3f();
        float x = (float) (Mth.lerp(tickPercentage, this.xo, this.x) - camPos.x);
        float y = (float) (Mth.lerp(tickPercentage, this.yo, this.y) - camPos.y);
        float z = (float) (Mth.lerp(tickPercentage, this.zo, this.z) - camPos.z);

        // angle particle along axis of velocity
        Vector3f delta = new Vector3f((float) this.xd, (float) this.yd, (float) this.zd);
        final float angle = Math.acos(delta.normalize().y);
        Vector3f axis = new Vector3f(-delta.z(), 0, delta.x()).normalize();
        Quaternionf quaternion = new Quaternionf(new AxisAngle4f(-angle, axis));

        // rotate particle to face camera
        //FIXME: results in some backfaced particles and weird rotation because of the off-axis tilt. not sure what the correct approach is.
        Vector3f local = new Vector3f(x, y, z);
        local.rotate(quaternion);
        quaternion.mul(Axis.YP.rotation(Math.atan2(local.x, local.z) + Mth.PI));

        quaternion = this.flipItTurnwaysIfBackfaced(quaternion, new Vector3f(x, y, z));
        this.renderRotatedQuad(vertexConsumer, quaternion, x, y, z, tickPercentage);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    @Environment(EnvType.CLIENT)
    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet provider;

        public DefaultFactory(SpriteSet provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new RainParticle(level, x, y, z, this.provider);
        }
    }
}