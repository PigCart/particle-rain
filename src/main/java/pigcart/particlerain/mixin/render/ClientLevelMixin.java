package pigcart.particlerain.mixin.render;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.particlerain.ParticleSpawner;

//? >=26.2 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.OptionInstance;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.levelgen.Heightmap;
import org.objectweb.asm.Opcodes;
import pigcart.particlerain.ParticleRain;

import static pigcart.particlerain.config.ConfigManager.getConfig;
*///?}

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Inject(method = "doAnimateTick", at = @At("TAIL"))
    public void hookDoAnimateTick(
            int posX, int posY, int posZ, //player position
            int range, // called twice with ranges 16 & 32
            RandomSource random,
            Block markerParticleTarget, // sourced from held item, used for barrier block display particles
            BlockPos.MutableBlockPos blockPos,
            CallbackInfo ci,
            @Local BlockState state
    ) {
        ParticleSpawner.tickBlockFX(blockPos, state, random);
    }

    //? >=26.2 {
    /*// bypass precipitation check so we can share the sound placement calculations with non-rain sounds
    @WrapOperation(method = "tickWeatherEffects", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;getPrecipitationAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;"
    ))
    public Biome.Precipitation overridePrecipitation(ClientLevel instance, BlockPos blockPos, Operation<Biome.Precipitation> original) {
        return Biome.Precipitation.RAIN;
    }

    // insert additional sounds without replacing vanilla code block where rain sounds are played
    @Inject(method = "tickWeatherEffects", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER, ordinal = 1,
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;rainSoundTime:I"), cancellable = true
    )
    public void hookWeatherSounds(CallbackInfo ci, @Local(name = "cameraPosition") BlockPos cameraPosition, @Local(name = "rainParticlePosition") BlockPos rainPos) {
        ParticleRain.doAdditionalWeatherSounds((ClientLevel)(Object)this, cameraPosition, rainPos, ci);
    }

    @WrapOperation(method = "tickWeatherEffects", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;getHeightmapPos(Lnet/minecraft/world/level/levelgen/Heightmap$Types;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"
    ))
    public BlockPos alterHeightmapPos(ClientLevel instance, Heightmap.Types types, BlockPos blockPos, Operation<BlockPos> original) {
        int newY = ParticleSpawner.getHeight(instance, blockPos.getX(), blockPos.getZ());
        return new BlockPos(blockPos.getX(), newY, blockPos.getZ());
    }

    // make rain sound use the mods rain volume slider
    @WrapOperation(method = "tickWeatherEffects", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;playLocalSound(Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"
    ))
    public void adjustRainVolume(ClientLevel instance, BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, boolean distanceDelay, Operation<Void> original) {
        original.call(instance, blockPos, soundEvent, soundSource, getConfig().sound.rainVolume, pitch, distanceDelay);
    }

    // particle status MINIMAL disables splash particles
    @SuppressWarnings({"unchecked", "rawtypes"})
    @WrapOperation(method = "tickWeatherEffects", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"
    ))
    public <T> T tickRainParticles(OptionInstance instance, Operation<T> original) {
        if (!getConfig().compat.doDefaultSplashing) {
            return (T) ParticleStatus.MINIMAL;
        }
        return original.call(instance);
    }
    *///?}
}
