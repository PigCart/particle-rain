package pigcart.particlerain.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import pigcart.particlerain.VersionUtil;

import java.awt.*;
import static pigcart.particlerain.config.ConfigManager.config;

import net.minecraft.util.RandomSource;


public class ShrubParticle extends WeatherParticle {

    protected ShrubParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z, VersionUtil.getSprite(MissingTextureAtlasSprite.getLocation()));
        this.quadSize = config.shrub.size;
        this.setSize(quadSize, quadSize);
        this.hasPhysics = true;
        this.gravity = config.shrub.gravity;
        this.yd = 0.1F;
        this.lifetime = 200;
        this.xd = level.isThundering() ? config.shrub.stormWindStrength : config.shrub.windStrength;
        this.zd = level.isThundering() ? config.shrub.stormWindStrength : config.shrub.windStrength;
    }

    //TODO

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        public Provider(SpriteSet provider) {
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ/*? if >=1.21.9 {*//*, RandomSource random*//*?}*/) {
            return new ShrubParticle(level, x, y, z);
        }
    }
}
