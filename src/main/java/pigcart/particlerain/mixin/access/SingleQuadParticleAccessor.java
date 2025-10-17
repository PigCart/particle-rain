package pigcart.particlerain.mixin.access;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.particle.SingleQuadParticle;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SingleQuadParticle.class)
public interface SingleQuadParticleAccessor {

    //? if >1.20.1 {
    /*//? if <1.21.9 {
    @Invoker
    void callRenderVertex(VertexConsumer buffer, Quaternionf quaternion, float x, float y, float z, float xOffset, float yOffset, float quadSize, float u, float v, int packedLight);
    //?}
    *///?}
}
