package pigcart.particlerain.mixin.neoforge;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderPipelines.class)
public interface RenderPipelinesAccessor {

    @Invoker
    RenderPipeline callRegister(RenderPipeline renderPipeline);
}
