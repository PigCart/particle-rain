package pigcart.particlerain.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pigcart.particlerain.StonecutterUtil;
import pigcart.particlerain.WeatherParticleManager;

import static pigcart.particlerain.config.ModConfig.CONFIG;

//TODO
public abstract class WeatherParticle extends TextureSheetParticle {

    protected BlockPos.MutableBlockPos pos;
    protected BlockPos.MutableBlockPos oPos;
    boolean doCollisionAnim = false;
    BlockHitResult collision = null;
    float baseTemp;
    float targetOpacity;
    float oQuadSize;
    float distanceSquared;

    protected WeatherParticle(ClientLevel level, double x, double y, double z, float gravity, float opacity, float size, float windStrength, float stormWindStrength) {
        super(level, x, y, z);

        this.gravity = gravity;
        this.quadSize = size;
        this.alpha = 0;
        this.xd = gravity * (level.isThundering() ? stormWindStrength : windStrength);
        if (CONFIG.compat.yLevelWindAdjustment) this.xd = this.xd * yLevelWindAdjustment(y);
        this.zd = this.xd;
        this.yd = -gravity;
        this.hasPhysics = false;

        this.targetOpacity = opacity;

        this.setSize(0.01F, 0.01F);
        this.lifetime = CONFIG.perf.particleDistance * 100;
        this.pos = new BlockPos.MutableBlockPos(x, y, z);
        this.oPos = new BlockPos.MutableBlockPos(x, y, z);
        this.baseTemp = level.getBiome(this.pos).value().getBaseTemperature();

        testForCollisions();
        WeatherParticleManager.particleCount++;
    }

    @Override
    public void tick() {
        super.tick();
        oQuadSize = quadSize;
        distanceSquared = (float) Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceToSqr(x, y, z);
        pos.set(x, y, z);
        if (!pos.equals(oPos)) {
            onPositionUpdate();
            oPos.set(pos);
        }
        if (doCollisionAnim) {
            collisionAnim();
        }
        fadeByDistance();
    }

    public void onPositionUpdate() {
        if (!CONFIG.compat.crossBiomeBorder && Mth.abs(level.getBiome(pos).value().getBaseTemperature() - baseTemp) > 0.4) {
            doCollisionAnim = true;
        }
        if (level.getBlockState(pos).isCollisionShapeFullBlock(level, pos) || !level.getFluidState(pos).isEmpty()) {
            this.remove();
        } else {
            testForCollisions();
        }
    }

    public void testForCollisions() {
        Vec3 quadCenterPos = new Vec3(x, y, z);
        Vec3 quadEdgePos = new Vec3(xd, yd, zd).normalize().multiply(quadSize, quadSize, quadSize).add(x, y, z);
        final BlockHitResult hitResult = level.clip(StonecutterUtil.getClipContext(quadCenterPos, quadEdgePos));
        if (!hitResult.getType().equals(HitResult.Type.MISS) && !doCollisionAnim) {
            collision = hitResult;
            doCollisionAnim = true;
        }

    }

    public void fadeByDistance() {
        final float renderDistanceSquared = Mth.square(CONFIG.perf.particleDistance);
        if (distanceSquared > renderDistanceSquared + 2) {
            remove();
        } else {
            alpha = Mth.lerp(distanceSquared / renderDistanceSquared, targetOpacity, 0);
        }
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        return Mth.lerp(scaleFactor, oQuadSize, quadSize);
    }

    public void collisionAnim() {
        float deltaMovement = (float) new Vec3(xd, yd, zd).length();
        quadSize = quadSize - deltaMovement;
        if (quadSize <= 0) remove();
    }

    @Override
    public void remove() {
        if (this.isAlive()) WeatherParticleManager.particleCount--;
        super.remove();
    }

    public Quaternionf turnBackfaceFlipways(Quaternionf quaternion, Vector3f cameraOffset) {
        Vector3f normal = new Vector3f(0, 0, 1);
        normal.rotate(quaternion).normalize();
        float dot = normal.dot(cameraOffset);
        if (dot > 0) {
            return quaternion.mul(Axis.YP.rotation(Mth.PI));
        }
        else return quaternion;
    }

    //TODO
    public static double yLevelWindAdjustment(double y) {
        return Math.clamp(0.01, 0.5, (y - 64) / 40);
    }

    @Override
    public ParticleRenderType getRenderType() {
        if (targetOpacity == 1F) {
            return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
        } else {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }
    }

    //? if <=1.20.1 {
    protected void renderRotatedQuad(VertexConsumer vertexConsumer, Quaternionf quaternionf, float x, float y, float z, float tickPercent) {
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
    //?}
}