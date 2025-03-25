package pigcart.particlerain.mixin.render.puddle.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import pigcart.particlerain.WeatherBlockManager;
import pigcart.particlerain.config.ModConfig;

@Mixin(ChunkBuilderMeshingTask.class)
public class ChunkBuilderMeshingTaskMixin {

    // insert a fake water block if the blockpos has a puddle
    @ModifyVariable(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
        at = @At(value = "STORE"), remap = false)
    private FluidState getFluidState(FluidState value, @Local(ordinal = 0) BlockPos.MutableBlockPos blockPos) {
        if (ModConfig.CONFIG.effect.doPuddles && WeatherBlockManager.hasPuddle(Minecraft.getInstance().level, blockPos)) {
            value = Blocks.WATER.defaultBlockState().setValue(BlockStateProperties.LEVEL, 7).getFluidState();
        }
        return value;
    }

    // disable skipping of air blocks (sorry) so that puddles can be calculated
    @WrapOperation(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    private boolean isAir(BlockState instance, Operation<Boolean> original) {
        if (ModConfig.CONFIG.effect.doPuddles) {
            return false;
        } else {
            return original.call(instance);
        }
    }
}
