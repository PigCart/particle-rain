//? if neoforge {
package pigcart.particlerain.mixin.neoforge;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import pigcart.particlerain.WeatherBlockManager;
import pigcart.particlerain.config.ModConfig;

@Mixin(SectionCompiler.class)
public class SectionCompilerMixinNeoforge {

    // neoforge modifies the compile method so seperate implementations are needed
    @ModifyVariable(method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;",
            at = @At(value = "STORE"))
    private FluidState getFluidStateNeoForge(FluidState value, @Local(ordinal = 2) BlockPos blockPos3) {
        if (ModConfig.CONFIG.effect.doPuddles && WeatherBlockManager.hasPuddle(Minecraft.getInstance().level, blockPos3)) {
            value = Blocks.WATER.defaultBlockState().setValue(BlockStateProperties.LEVEL, 7).getFluidState();
        }
        return value;
    }
}
//?}