package pigcart.particlerain.particle;

import com.mojang.math.Transformation;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pigcart.particlerain.VersionUtil;
import pigcart.particlerain.config.ParticleData;
import pigcart.particlerain.mixin.access.BlockDisplayAccessor;
import pigcart.particlerain.mixin.access.ClientLevelAccessor;
import pigcart.particlerain.mixin.access.DisplayAccessor;
//? if >=1.21.9 {
/*import net.minecraft.client.renderer.state./^?>=26.1{^//^level.^//^?}^/QuadParticleRenderState;
*///?} else {
import com.mojang.blaze3d.vertex.VertexConsumer;
//?}

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class BlockDisplayParticle extends CustomParticle {

    // the correct way to do this would be with a custom ParticleGroup a la item pickup particles
    // but thats annoying for multiversion and spamming fake entities seems to work well enough

    private static final ArrayList<Display.BlockDisplay> ENTITIES_IN_LEVEL = new ArrayList<>();
    private final Display.BlockDisplay display;

    public BlockDisplayParticle(ClientLevel level, double x, double y, double z, ParticleData data) {
        super(level, x, y, z, data);

        //~ if >=26.2 'EntityType' -> 'EntityTypes'
        display = new Display.BlockDisplay(net.minecraft.world.entity.EntityType.BLOCK_DISPLAY, level);
        display.setPos(x, y, z);

        final ResourceLocation id = VersionUtil.parseId(data.blockId);
        if (id != null) {
            //TODO move validation for this into the config
            try {
                ((BlockDisplayAccessor) display).callSetBlockState(
                        //~ if >=1.21.9 'defaultBlockState' -> 'get().value().defaultBlockState'
                        BuiltInRegistries.BLOCK.get(id).defaultBlockState()
                );
            } catch (NoSuchElementException e) {
                this.remove();
            }
        }
        this.rotationVariation = data.rotationAmount;

        display.setId(-this.random.nextInt(Integer.MAX_VALUE));
        //~ if >1.20.1 '((ClientLevelAccessor)level).callAddEntity(display.getId(), ' -> 'level.addEntity('
        ((ClientLevelAccessor)level).callAddEntity(display.getId(), display);
        ENTITIES_IN_LEVEL.add(display);
        updateDisplay();
    }


    @Override
    //~ if >=1.21.9 'render(VertexConsumer' -> 'extract(QuadParticleRenderState'
    public void render(VertexConsumer h, Camera camera, float tickPercent) {
    }

    @Override
    public void tick() {
        super.tick();
        updateDisplay();
    }

    public void updateDisplay() {
        display.setPos(x, y, z);
        float yaw = (float)Math.atan2(-this.xd, this.zd) * (180.0F / (float)Math.PI);
        display.setYRot(yaw);
        ((DisplayAccessor) display).callSetTransformation(new Transformation(
                new Vector3f(0, -Mth.cos(roll + 0.8F) * 0.7F, -Mth.cos(roll + 5.5F) * 0.7F),
                new Quaternionf(),
                new Vector3f(quadSize),
                new Quaternionf(new AxisAngle4f(roll, 1, 0, 0))
        ));
        //? 1.20.1 {
        ((DisplayAccessor) display).callSetInterpolationDuration(1);
        ((DisplayAccessor) display).callSetInterpolationDelay(0);
        //? else {
        /*((DisplayAccessor) display).callSetTransformationInterpolationDuration(1);
        ((DisplayAccessor) display).callSetTransformationInterpolationDelay(0);
        *///?}
    }

    public static void clearAll() {
        BlockDisplayParticle.ENTITIES_IN_LEVEL.forEach(entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        BlockDisplayParticle.ENTITIES_IN_LEVEL.clear();
    }

   @Override
    public void remove() {
        display.remove(Entity.RemovalReason.DISCARDED);
        ENTITIES_IN_LEVEL.remove(display);
        super.remove();
    }

}
