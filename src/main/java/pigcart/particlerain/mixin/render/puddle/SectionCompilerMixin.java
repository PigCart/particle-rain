package pigcart.particlerain.mixin.render.puddle;

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
public class SectionCompilerMixin {

    // insert a fake water block if the blockpos has a puddle
    @ModifyVariable(method = "compile", at = @At(value = "STORE"))
    private FluidState getFluidState(FluidState value, @Local(ordinal = 2) BlockPos blockPos3) {
        if (ModConfig.CONFIG.effect.doPuddles && WeatherBlockManager.hasPuddle(Minecraft.getInstance().level, blockPos3)) {
            value = Blocks.WATER.defaultBlockState().setValue(BlockStateProperties.LEVEL, 7).getFluidState();
        }
        return value;
    }
}
