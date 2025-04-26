package pigcart.particlerain.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import pigcart.particlerain.config.ModConfig;

import java.awt.*;

import static pigcart.particlerain.config.ModConfig.CONFIG;

public class DustParticle extends WeatherParticle {

    protected DustParticle(ClientLevel clientWorld, double x, double y, double z, SpriteSet provider) {
        super(clientWorld, x, y, z, CONFIG.dust.gravity, CONFIG.dust.opacity, CONFIG.dust.size, CONFIG.dust.windStrength, CONFIG.dust.stormWindStrength);

        this.setSprite(provider.get(this.random));
        if (ModConfig.CONFIG.dust.spawnOnGround) this.yd = 0.1F;

        //? if >=1.21.4 {
        final Color color = new Color(level.getBlockState(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(x, y, z)).below()).getBlock().defaultMapColor().calculateARGBColor(MapColor.Brightness.NORMAL));
        this.bCol = (float)color.getBlue() / 255;
        this.rCol = (float)color.getRed() / 255;
        this.gCol = (float)color.getGreen() / 255;
        //?}
        //? if <=1.21.1 {
        /*final Color color = new Color(level.getBlockState(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(x, y, z)).below()).getBlock().defaultMapColor().calculateRGBColor(MapColor.Brightness.NORMAL));
        // red and blue are swapped
        this.rCol = (float)color.getBlue() / 255;
        this.bCol = (float)color.getRed() / 255;
        this.gCol = (float)color.getGreen() / 255;
        *///?}
    }
    @Override
    public void tick() {
        super.tick();
        if (this.onGround) {
            this.yd = 0.01F;
        }
        this.xd = level.isThundering() ? ModConfig.CONFIG.dust.stormWindStrength : ModConfig.CONFIG.dust.windStrength;
        this.zd = level.isThundering() ? ModConfig.CONFIG.dust.stormWindStrength : ModConfig.CONFIG.dust.windStrength;
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float tickPercent) {
        Vec3 camPos = camera.getPosition();
        float x = (float) (Mth.lerp(tickPercent, this.xo, this.x) - camPos.x());
        float y = (float) (Mth.lerp(tickPercent, this.yo, this.y) - camPos.y());
        float z = (float) (Mth.lerp(tickPercent, this.zo, this.z) - camPos.z());

        Quaternionf quaternion = camera.rotation();
        y = y + Mth.sin((Mth.lerp(tickPercent, this.age - 1.0F, this.age)) / 20);
        //? if >1.20.1 {
        this.renderRotatedQuad(vertexConsumer, quaternion, x, y, z, tickPercent);
        //?} else {
        /*// for some reason WeatherParticle.renderRotatedQuad can't be used here despite containing the same code.
        // whats up with that? why does it work for the other particles?
        float size = this.getQuadSize(tickPercent);
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int lightColor = this.getLightColor(tickPercent);
        Vector3f[] vector3fs = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)};
        for(int k = 0; k < 4; ++k) {
            Vector3f vector3f = vector3fs[k];
            vector3f.rotate(quaternion);
            vector3f.mul(size);
            vector3f.add(x, y, z);
        }
        vertexConsumer.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z()).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();
        vertexConsumer.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z()).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();
        vertexConsumer.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z()).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();
        vertexConsumer.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z()).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();
        *///?}
    }
    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet provider;

        public DefaultFactory(SpriteSet provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new DustParticle(level, x, y, z, this.provider);
        }
    }
}
