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
import pigcart.particlerain.WeatherBlockSpawner;

@Mixin(ChunkBuilderMeshingTask.class)
public class ChunkBuilderMeshingTaskMixin {

    @ModifyVariable(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
        at = @At(value = "STORE"), remap = false)
    private FluidState getFluidState(FluidState value, @Local(ordinal = 0) BlockPos.MutableBlockPos blockPos) {
        if (WeatherBlockSpawner.hasPuddle(Minecraft.getInstance().level, blockPos)) {
            value = Blocks.WATER.defaultBlockState().setValue(BlockStateProperties.LEVEL, 7).getFluidState();
        }
        return value;
    }

    @WrapOperation(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"), remap = false)
    private boolean isAir(BlockState instance, Operation<Boolean> original) {
        if (Minecraft.getInstance().level.isRaining()) {
            return false;
            // sorry. its probably fine.
        } else {
            return original.call(instance);
        }
    }
}
