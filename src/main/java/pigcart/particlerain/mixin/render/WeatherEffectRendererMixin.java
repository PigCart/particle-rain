package pigcart.particlerain.mixin.render;

//? >1.21.1 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import pigcart.particlerain.ParticleSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.particlerain.ParticleRain;
//? <26.2 {
import net.minecraft.client.renderer.MultiBufferSource;
//?}
//? >=1.21.9 {
/^import net.minecraft.client.renderer.state./^?>=26.1{^//^level.^//^?}^/WeatherRenderState;
^///?}

import java.util.List;

import static pigcart.particlerain.config.ConfigManager.getConfig;

@Mixin(WeatherEffectRenderer.class)
public abstract class WeatherEffectRendererMixin {

    //? <26.2 {
    @Shadow protected abstract Biome.Precipitation getPrecipitationAt(Level level, BlockPos pos);

    // bypass precipitation check so we can share the sound placement calculations with non-rain sounds
    @WrapOperation(method = "tickRainParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WeatherEffectRenderer;getPrecipitationAt(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;"))
    public Biome.Precipitation getPrecipitationAt(WeatherEffectRenderer instance, Level level, BlockPos blockPos3, Operation<Biome.Precipitation> original) {
        return Biome.Precipitation.RAIN;
    }

    // insert additional sounds without replacing vanilla code block where rain sounds are played
    @Inject(method = "tickRainParticles", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER, ordinal = 1, target = "Lnet/minecraft/client/renderer/WeatherEffectRenderer;rainSoundTime:I"), cancellable = true)
    public void hookWeatherSounds(ClientLevel level, Camera camera, int ticks, ParticleStatus particleStatus,/^?>=1.21.11{^//^int weatherRadius,^//^?}^/ CallbackInfo ci, @Local(ordinal = 0) BlockPos blockPos, @Local(ordinal = 1) BlockPos blockPos2) {
        ParticleRain.doAdditionalWeatherSounds(level, blockPos, blockPos2, ci);
    }

    @WrapOperation(
            method = "tickRainParticles",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;getHeightmapPos(Lnet/minecraft/world/level/levelgen/Heightmap$Types;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"
            )
    )
    public BlockPos alterHeightmapPos(ClientLevel instance, Heightmap.Types types, BlockPos blockPos, Operation<BlockPos> original) {
        int newY = ParticleSpawner.getHeight(instance, blockPos.getX(), blockPos.getZ());
        return new BlockPos(blockPos.getX(), newY, blockPos.getZ());
    }

    // make rain sound use the mods rain volume slider
    @WrapOperation(method = "tickRainParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;playLocalSound(Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"))
    public void adjustRainVolume(ClientLevel instance, BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, boolean distanceDelay, Operation<Void> original) {
        original.call(instance, blockPos, soundEvent, soundSource, getConfig().sound.rainVolume, pitch, distanceDelay);
    }

    // particle status MINIMAL disables splash particles
    @Inject(method = "tickRainParticles", at = @At("HEAD"))
    public void tickRainParticles(ClientLevel level, Camera camera, int ticks, ParticleStatus particleStatus,/^?>=1.21.11{^//^int weatherRadius,^//^?}^/ CallbackInfo ci, @Local(argsOnly = true) LocalRef<ParticleStatus> particleStatusLocalRef) {
        if (!getConfig().compat.doDefaultSplashing) {
            particleStatusLocalRef.set(ParticleStatus.MINIMAL);
        }
    }
    //?}

    // prevent rendering weather column instances
    //? >=26.2 {
    /^@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    public void render(ClientLevel level, float partialTicks, Vec3 cameraPos, WeatherRenderState renderState, CallbackInfo ci) {
    ^///?} >=1.21.9 {
    /^@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    public void render(Level level, int ticks, float partialTick, Vec3 cameraPos, WeatherRenderState reusedState, CallbackInfo ci) {
    ^///?} else {
    @Inject(method = "render(Lnet/minecraft/world/level/Level;Lnet/minecraft/client/renderer/MultiBufferSource;IFLnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"), cancellable = true)
    public void render(Level level, MultiBufferSource bufferSource, int ticks, float partialTick, Vec3 cameraPosition, CallbackInfo ci) {
    //?}
        if (!getConfig().compat.renderDefaultRain && !getConfig().compat.renderDefaultSnow) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0))
    public <E> boolean cancelAddRainColumn(List<WeatherEffectRenderer.ColumnInstance> instance, E columnInstance, Operation<Boolean> original) {
        if (!getConfig().compat.renderDefaultRain) return false;
        return original.call(instance, columnInstance);
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 1))
    public <E> boolean cancelAddSnowColumn(List<WeatherEffectRenderer.ColumnInstance> instance, E columnInstance, Operation<Boolean> original) {
        if (!getConfig().compat.renderDefaultSnow) return false;
        return original.call(instance, columnInstance);
    }
}
*///?}
//? if <=1.21.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.biome.Biome;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.ParticleSpawner;

import static pigcart.particlerain.config.ConfigManager.getConfig;

@Mixin(LevelRenderer.class)
public class WeatherEffectRendererMixin {

    // bypass precipitation check so we can share the sound placement calculations with non-rain sounds
    @WrapOperation(method = "tickRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;getPrecipitationAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;"))
    public Biome.Precipitation bypassGetBiomePrecipitation(Biome instance, BlockPos pos, Operation<Biome.Precipitation> original) {
        return Biome.Precipitation.RAIN;
    }

    @WrapOperation(
            method = "tickRain",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/LevelReader;getHeightmapPos(Lnet/minecraft/world/level/levelgen/Heightmap$Types;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"
            )
    )
    public BlockPos alterHeightmapPos(net.minecraft.world.level.LevelReader instance, net.minecraft.world.level.levelgen.Heightmap.Types types, BlockPos blockPos, Operation<BlockPos> original) {
        ClientLevel level = (ClientLevel) instance;
        int newY = ParticleSpawner.getHeight(level, blockPos.getX(), blockPos.getZ());
        return new BlockPos(blockPos.getX(), newY, blockPos.getZ());
    }

    // insert additional sounds without replacing vanilla code block where rain sounds are played
    @Inject(method = "tickRain", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER, ordinal = 1, target = "Lnet/minecraft/client/renderer/LevelRenderer;rainSoundTime:I"), cancellable = true)
    public void hookWeatherSounds(Camera camera, CallbackInfo ci, @Local(ordinal = 0) BlockPos blockPos, @Local(ordinal = 1) BlockPos blockPos2) {
        ParticleRain.doAdditionalWeatherSounds(Minecraft.getInstance().level, blockPos, blockPos2, ci);
    }

    // make rain sound use the mods rain volume slider
    @WrapOperation(method = "tickRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;playLocalSound(Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"))
    public void adjustRainVolume(ClientLevel instance, BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, boolean distanceDelay, Operation<Void> original) {
        original.call(instance, blockPos, soundEvent, soundSource, getConfig().sound.rainVolume, pitch, distanceDelay);
    }

    // particle status MINIMAL disables splash particles
    @WrapOperation(method = "tickRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
    public Object preventSplashParticles(OptionInstance instance, Operation<ParticleStatus> original) {
        if (!getConfig().compat.doDefaultSplashing) {
            return ParticleStatus.MINIMAL;
        }
        return original.call(instance);
    }

    // prevent rendering weather column instances
    // returning null instead of NONE might prevent modded dry weather columns but could break mods that mixin here expecting a value
    @WrapOperation(method = "renderSnowAndRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;getPrecipitationAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;"))
    public Biome.Precipitation nullifyRenderPrecipitation(Biome instance, BlockPos pos, Operation<Biome.Precipitation> original) {
        Biome.Precipitation originalResult = original.call(instance, pos);
        if (originalResult == Biome.Precipitation.RAIN && !getConfig().compat.renderDefaultRain) {
            return null;
        } else if (originalResult == Biome.Precipitation.SNOW && !getConfig().compat.renderDefaultSnow) {
            return null;
        }
        return originalResult;
    }
}
//?}