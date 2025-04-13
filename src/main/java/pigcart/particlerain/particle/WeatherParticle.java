package pigcart.particlerain.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pigcart.particlerain.WeatherParticleManager;
import pigcart.particlerain.config.ModConfig;

public abstract class WeatherParticle extends TextureSheetParticle {

    protected BlockPos.MutableBlockPos pos;
    //TODO: 'optimised' fade
    boolean shouldFadeOut = false;
    //TODO: why is this even using temperature
    float temperature;

    protected WeatherParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.setSize(0.01F, 0.01F);
        this.lifetime = ModConfig.CONFIG.perf.particleRadius * 100;
        this.alpha = 0.0F;
        this.pos = new BlockPos.MutableBlockPos(x, y, z);
        this.temperature = level.getBiome(this.pos).value().getBaseTemperature();
        WeatherParticleManager.particleCount++;
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
        if (this.isAlive()) WeatherParticleManager.particleCount--;
        super.remove();
    }

    void removeIfOOB() {
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (cameraEntity == null || cameraEntity.distanceToSqr(this.x, this.y, this.z) > Mth.square(ModConfig.CONFIG.perf.particleRadius)) {
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
    public static double yLevelWindAdjustment(double y) {
        return Math.clamp(0.01, 1, (y - 64) / 40);
    }

    //? if <=1.20.1 {
    /*protected void renderRotatedQuad(VertexConsumer vertexConsumer, Quaternionf quaternionf, float x, float y, float z, float tickPercent) {
        quaternionf.rotateY(Mth.PI);
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
            vector3f.rotate(quaternionf);
            vector3f.mul(size);
            vector3f.add(x, y, z);
        }

        vertexConsumer.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z()).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();
        vertexConsumer.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z()).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();
        vertexConsumer.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z()).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();
        vertexConsumer.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z()).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();

    }
    *///?}
}