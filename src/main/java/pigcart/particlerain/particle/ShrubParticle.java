package pigcart.particlerain.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.joml.AxisAngle4f;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.StonecutterUtil;
import pigcart.particlerain.config.ModConfig;

import java.awt.*;

//? if >=1.21.5 {
/*import net.minecraft.client.renderer.block.model.BlockStateModel;
*///?} else {
import net.minecraft.client.resources.model.BakedModel;
//?}
import static pigcart.particlerain.config.ModConfig.CONFIG;

public class ShrubParticle extends WeatherParticle {

    protected ShrubParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z, CONFIG.shrub.gravity, CONFIG.shrub.opacity, CONFIG.shrub.size, CONFIG.shrub.windStrength, CONFIG.shrub.stormWindStrength);

        this.hasPhysics = true;

        this.yd = 0.2F;

        BlockState blockState = level.getBlockState(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, this.pos));
        // no foliage convention tag? :(
        if (blockState.is(BlockTags.REPLACEABLE) && !blockState.isAir() && blockState.getFluidState().isEmpty() && !blockState.is(BlockTags.CROPS) && !blockState.is(BlockTags.SNOW)) {
            //? if >=1.21.5 {
            /*final BlockStateModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState);
            this.setSprite(model.particleIcon());
            final BakedQuad quad = model.collectParts(this.random).getFirst().getQuads(null).getFirst();
            *///?} else {
            final BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState);
            this.setSprite(model.getParticleIcon());
            final BakedQuad quad = model.getQuads(blockState, null, this.random).get(0);
            //?}
            if (quad.isTinted()) {
                Color color = new Color(BiomeColors.getAverageFoliageColor(level, this.pos));
                this.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
            }
        } else {
            blockState = Blocks.DEAD_BUSH.defaultBlockState();
        }
        //? if >=1.21.5 {
        /*this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState).particleIcon());
        *///?} else {
        this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState).getParticleIcon());
        //?}
    }

    @Override
    public void testForCollisions() {
        //dont
    }

    @Override
    public void tick() {
        super.tick();
        if (this.xd == 0 || this.zd == 0) this.remove();
        this.xd = level.isThundering() ? ModConfig.CONFIG.shrub.stormWindStrength : ModConfig.CONFIG.shrub.windStrength;
        this.zd = level.isThundering() ? ModConfig.CONFIG.shrub.stormWindStrength : ModConfig.CONFIG.shrub.windStrength;
        this.oRoll = this.roll;
        this.roll = this.roll + ModConfig.CONFIG.shrub.rotationAmount;
        if (this.onGround) {
            this.yd = ModConfig.CONFIG.shrub.bounciness;
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
        quat1 = this.turnBackfaceFlipways(quat1, new Vector3f(x, y, z));
        quat2 = this.turnBackfaceFlipways(quat2, new Vector3f(x, y, z));
        this.renderRotatedQuad(vertexConsumer, quat1, x, y, z, tickPercentage);
        this.renderRotatedQuad(vertexConsumer, quat2, x, y, z, tickPercentage);
    }

    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        public DefaultFactory(SpriteSet provider) {
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new ShrubParticle(level, x, y, z);
        }
    }
}
