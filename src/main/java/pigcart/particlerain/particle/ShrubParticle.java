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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.AxisAngle4f;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pigcart.particlerain.ParticleRainClient;

public class ShrubParticle extends WeatherParticle {

    protected ShrubParticle(ClientLevel level, double x, double y, double z, SpriteSet provider) {
        super(level, x, y, z, ParticleRainClient.config.shrub.gravity, provider);
        this.quadSize = 0.5F;
        this.xd = ParticleRainClient.config.sand.windStrength;
        this.zd = ParticleRainClient.config.sand.windStrength;
        ItemStack itemStack = new ItemStack(Items.DEAD_BUSH);
        this.setSprite(Minecraft.getInstance().getItemRenderer().getModel(itemStack, level, null, 0).getParticleIcon());
    }
    @Override
    public void tick() {
        super.tick();
        if (this.removeIfObstructed()) {
            if (this.isHotBlock()) {
                Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.SMOKE, this.x, this.y, this.z, 0, 0, 0);
            }
        }
        else if (!this.level.getFluidState(this.pos).isEmpty()) {
            this.shouldFadeOut = true;
            this.gravity = 0;
        } else {
            this.xd = 0.2;
            this.zd = 0.2;
        }
        this.oRoll = this.roll;
        this.roll = this.roll + ParticleRainClient.config.shrub.rotationAmount;
        if (this.onGround) {
            this.yd = ParticleRainClient.config.shrub.bounciness;
        }
    }
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }
    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float tickPercentage) {
        Vector3f camPos = camera.getPosition().toVector3f();
        float x = (float) (Mth.lerp(tickPercentage, this.xo, this.x) - camPos.x);
        float y = (float) (Mth.lerp(tickPercentage, this.yo, this.y) - camPos.y);
        float z = (float) (Mth.lerp(tickPercentage, this.zo, this.z) - camPos.z);

        final float angle = (float) Math.atan2(this.xd, this.zd);
        Quaternionf quaternion = new Quaternionf();
        quaternion.rotateY(angle);

        Quaternionf quat1 = new Quaternionf(new AxisAngle4f(0, 0, 1, 0));
        Quaternionf quat2 = new Quaternionf(new AxisAngle4f(Mth.HALF_PI, 0, 1, 0));
        quat1.mul(quaternion).rotateX(Mth.lerp(tickPercentage, this.oRoll, this.roll));
        quat2.mul(quaternion).rotateZ(Mth.lerp(tickPercentage, this.oRoll, this.roll));
        quat1 = this.flipItTurnwaysIfBackfaced(quat1, new Vector3f(x, y, z));
        quat2 = this.flipItTurnwaysIfBackfaced(quat2, new Vector3f(x, y, z));
        this.renderRotatedQuad(vertexConsumer, quat1, x, y, z, tickPercentage);
        this.renderRotatedQuad(vertexConsumer, quat2, x, y, z, tickPercentage);
    }
    @Environment(EnvType.CLIENT)
    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet provider;

        public DefaultFactory(SpriteSet provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new ShrubParticle(level, x, y, z, this.provider);
        }
    }
}
