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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

public class DeadBushParticle extends DustMoteParticle {

    protected DeadBushParticle(ClientLevel level, double x, double y, double z, SpriteSet provider) {
        super(level, x, y, z, provider);
        this.quadSize = 0.5F;
        this.rCol = 1;
        this.bCol = 1;
        this.gCol = 1;
        ItemStack itemStack = new ItemStack(Items.DEAD_BUSH);
        this.setSprite(Minecraft.getInstance().getItemRenderer().getModel(itemStack, level, (LivingEntity)null, 0).getParticleIcon());
    }
    @Override
    public void tick() {
        super.tick();
        this.oRoll = this.roll;
        this.roll = this.roll + 0.1F;
    }
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }
    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float tickPercentage) {
        Quaternionf[] faceArray = new Quaternionf[] {
                new Quaternionf(new AxisAngle4f(0, 0, 1, 0)),
                new Quaternionf(new AxisAngle4f(Mth.PI, 0, 1, 0)),
                new Quaternionf(new AxisAngle4f(Mth.HALF_PI, 0, 1, 0)),
                new Quaternionf(new AxisAngle4f(Mth.HALF_PI, 0, -1, 0))
        };
        for (Quaternionf quaternion : faceArray) {
            quaternion.rotateLocalX(Mth.lerp(tickPercentage, this.oRoll, this.roll));
            quaternion.rotateLocalY(Mth.lerp(tickPercentage, this.oRoll, this.roll));
            this.renderRotatedQuad(vertexConsumer, camera, quaternion, tickPercentage);
        }
    }
    @Environment(EnvType.CLIENT)
    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet provider;

        public DefaultFactory(SpriteSet provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new DeadBushParticle(level, x, y, z, this.provider);
        }
    }
}
