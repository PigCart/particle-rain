package pigcart.particlerain.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import pigcart.particlerain.ParticleRainClient;

public abstract class WeatherParticle extends TextureSheetParticle {

    protected final BlockPos.MutableBlockPos pos;

    protected WeatherParticle(ClientLevel level, double x, double y, double z, float red, float green, float blue, float gravity, SpriteSet provider) {
        super(level, x, y, z, red, green, blue);
        this.setSprite(provider.get(level.getRandom()));

        this.gravity = gravity;

        this.xd = 0.0F;
        this.yd = -gravity;
        this.zd = 0.0F;

        this.rCol = red;
        this.gCol = green;
        this.bCol = blue;

        this.quadSize = 0.1F;

        this.pos = new BlockPos.MutableBlockPos();
    }

    @Override
    public void tick() {
        super.tick();
        this.pos.set(this.x, this.y, this.z);
    }

    protected boolean shouldRemove() {
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        return cameraEntity == null || cameraEntity.distanceToSqr(this.x, this.y, this.z) > (ParticleRainClient.config.particleRadius + 2) * (ParticleRainClient.config.particleRadius + 2);
    }
}