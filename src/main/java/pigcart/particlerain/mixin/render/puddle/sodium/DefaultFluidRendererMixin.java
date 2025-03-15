package pigcart.particlerain.mixin.render.puddle.sodium;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pigcart.particlerain.config.ModConfig;

@Mixin(DefaultFluidRenderer.class)
public class DefaultFluidRendererMixin {

    @Inject(method = "fluidHeight", at = @At("HEAD"), cancellable = true)
    private void fluidHeight(BlockAndTintGetter world, Fluid fluid, BlockPos blockPos, Direction direction, CallbackInfoReturnable<Float> cir) {
        if (ModConfig.CONFIG.effect.doPuddles && world.getFluidState(blockPos).isEmpty() || (world.getFluidState(blockPos.relative(direction.getOpposite())).isEmpty() && direction != Direction.UP)) {
            cir.setReturnValue(0.02F);
        }
    }

    @Inject(method = "fluidCornerHeight", at = @At("HEAD"), cancellable = true)
    private void fluidCornerHeight(BlockAndTintGetter world, Fluid fluid, float fluidHeight, float fluidHeightX, float fluidHeightY, BlockPos blockPos, CallbackInfoReturnable<Float> cir) {
        if (ModConfig.CONFIG.effect.doPuddles && fluidHeight == 0.02F) {
            cir.setReturnValue(0.02F);
        }
    }

    @Inject(method = "isFullBlockFluidOccluded", at = @At("HEAD"), cancellable = true)
    private void isFullBlockFluidOccluded(BlockAndTintGetter world, BlockPos pos, Direction dir, BlockState blockState, FluidState fluid, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.CONFIG.effect.doPuddles && world.getFluidState(pos).isEmpty() && dir != Direction.UP) {
            cir.setReturnValue(true);
        }
    }
}
