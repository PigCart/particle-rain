package pigcart.particlerain.mixin.render.puddle.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
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

    // flatten puddle
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/DefaultFluidRenderer;fluidHeight(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)F"))
    private float fluidHeight(DefaultFluidRenderer instance, BlockAndTintGetter world, Fluid fluid, BlockPos blockPos, Direction direction, Operation<Float> original, @Local(ordinal = 0, argsOnly = true) BlockPos renderBlockPos) {
        if (ModConfig.CONFIG.effect.doPuddles && world.getFluidState(renderBlockPos).isEmpty()) {
            return 0.02F;
        }
        return original.call(instance, world, fluid, blockPos, direction);
    }

    // remove faces between puddles
    @Inject(method = "isFullBlockFluidOccluded", at = @At("HEAD"), cancellable = true)
    private void isFullBlockFluidOccluded(BlockAndTintGetter world, BlockPos pos, Direction dir, BlockState blockState, FluidState fluid, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.CONFIG.effect.doPuddles && world.getFluidState(pos).isEmpty() && dir != Direction.UP) {
            cir.setReturnValue(true);
        }
    }
}
