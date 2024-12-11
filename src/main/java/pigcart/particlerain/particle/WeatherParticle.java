package pigcart.particlerain.particle;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pigcart.particlerain.ParticleRainClient;

public abstract class WeatherParticle extends TextureSheetParticle {

    protected BlockPos.MutableBlockPos pos;
    boolean shouldFadeOut = false;
    float temperature;

    protected WeatherParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.setSize(0.01F, 0.01F);
        this.lifetime = ParticleRainClient.config.particleRadius * 10;
        this.alpha = 0.0F;
        this.pos = new BlockPos.MutableBlockPos(x, y, z);
        this.temperature = level.getBiome(this.pos).value().getBaseTemperature();
        ParticleRainClient.particleCount++;
    }

    @Override
    public void tick() {
        super.tick();
        this.pos.set(this.x, this.y - 0.2, this.z);
        this.removeIfOOB();
        if (shouldFadeOut) {
            fadeOut();
        } else if (this.age % 10 == 0) {
            if (Mth.abs(level.getBiome(this.pos).value().getBaseTemperature() - this.temperature) > 0.4) shouldFadeOut = true;
        } else {
            fadeIn();
        }
    }

    public void fadeIn() {
        if (age < 20) {
            this.alpha = (age * 1.0f) / 20;
        }
    }

    public void fadeOut() {
        if (this.alpha < 0.01) {
            remove();
        } else {
            this.alpha = this.alpha - 0.05F;
        }
    }

    @Override
    public void remove() {
        if (this.isAlive()) ParticleRainClient.particleCount--;
        super.remove();
    }

    void removeIfOOB() {
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (cameraEntity == null || cameraEntity.distanceToSqr(this.x, this.y, this.z) > Mth.square(ParticleRainClient.config.particleRadius)) {
            shouldFadeOut = true;
        }

    }
    //FIXME: obstruction removal triggers when wind is 0...
    protected boolean removeIfObstructed() {
        if (x == xo || z == zo) {
            this.remove();
            return true;
        } else {
            return false;
        }
    }
    public Quaternionf flipItTurnwaysIfBackfaced(Quaternionf quaternion, Vector3f toCamera) {
        Vector3f normal = new Vector3f(0, 0, 1);
        normal.rotate(quaternion).normalize();
        float dot = normal.dot(toCamera);
        if (dot > 0) {
            return quaternion.mul(Axis.YP.rotation(Mth.PI));
        }
        else return quaternion;
    }
}