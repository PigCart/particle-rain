package pigcart.particlerain.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import pigcart.particlerain.config.ModConfig;

import java.awt.Color;

public class DustMoteParticle extends WeatherParticle {
    // currently unused

    //TODO: completely redo sand/dust effects

    protected DustMoteParticle(ClientLevel level, double x, double y, double z, SpriteSet provider) {
        super(level, x, y, z);
        this.setSprite(provider.get(level.getRandom()));
        //this.quadSize = ModConfig.INSTANCE.dust.moteSize;
        this.xd = level.isThundering() ? ModConfig.CONFIG.dust.stormWindStrength : ModConfig.CONFIG.dust.windStrength;
        this.zd = level.isThundering() ? ModConfig.CONFIG.dust.stormWindStrength : ModConfig.CONFIG.dust.windStrength;
        this.gravity = ModConfig.CONFIG.dust.gravity;

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
            this.yd = 0.1F;
        }
        this.removeIfObstructed();
        if (!this.level.getFluidState(this.pos).isEmpty()) {
            this.shouldFadeOut = true;
            this.gravity = 0;
        } else {
            this.xd = level.isThundering() ? ModConfig.CONFIG.dust.stormWindStrength : ModConfig.CONFIG.dust.windStrength;
            this.zd = level.isThundering() ? ModConfig.CONFIG.dust.stormWindStrength : ModConfig.CONFIG.dust.windStrength;
        }
    }
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet provider;

        public DefaultFactory(SpriteSet provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new DustMoteParticle(level, x, y, z, this.provider);
        }
    }
}
