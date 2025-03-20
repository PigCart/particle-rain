package pigcart.particlerain.mixin.render.puddle;

import net.minecraft.client.renderer.block.LiquidBlockRenderer;
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

@Mixin(LiquidBlockRenderer.class)
public class LiquidBlockRendererMixin {

    // prevent puddles from showing side faces
    @Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true)
    private static void shouldRenderFace(FluidState fluidState, BlockState blockState, Direction side, FluidState neighborFluid, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.CONFIG.effect.doPuddles && side != Direction.UP && fluidState.getAmount() == 1 && blockState.getFluidState().isEmpty()) {
            cir.setReturnValue(false);
        }
    }
    // flatten puddles to reduce gaps between uneven puddle faces
    @Inject(method = "getHeight(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)F", at = @At("HEAD"), cancellable = true)
    private void getHeight(BlockAndTintGetter level, Fluid fluid, BlockPos pos, BlockState blockState, FluidState fluidState, CallbackInfoReturnable<Float> cir) {
        if (ModConfig.CONFIG.effect.doPuddles && fluidState.getAmount() == 1) {
            cir.setReturnValue(0.02F);
        }
    }
}
