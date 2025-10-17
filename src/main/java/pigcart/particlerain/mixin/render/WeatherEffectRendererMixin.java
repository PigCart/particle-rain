package pigcart.particlerain.mixin.render;

//? if >1.21.1 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.particlerain.ParticleRain;
//? if >=1.21.9 {
/^import net.minecraft.client.renderer.state.WeatherRenderState;
^///?}

import static pigcart.particlerain.config.ConfigManager.config;

@Mixin(WeatherEffectRenderer.class)
public abstract class WeatherEffectRendererMixin {

    @Shadow protected abstract Biome.Precipitation getPrecipitationAt(Level level, BlockPos pos);

    // bypass precipitation check so we can share the sound placement calculations with non-rain sounds
    @WrapOperation(method = "tickRainParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WeatherEffectRenderer;getPrecipitationAt(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;"))
    public Biome.Precipitation getPrecipitationAt(WeatherEffectRenderer instance, Level level, BlockPos blockPos3, Operation<Biome.Precipitation> original) {
        return Biome.Precipitation.RAIN;
    }

    // insert additional sounds without replacing vanilla code block where rain sounds are played
    @Inject(method = "tickRainParticles", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER, ordinal = 1, target = "Lnet/minecraft/client/renderer/WeatherEffectRenderer;rainSoundTime:I"), cancellable = true)
    public void hookWeatherSounds(ClientLevel level, Camera camera, int ticks, ParticleStatus particleStatus, CallbackInfo ci, @Local(ordinal = 0) BlockPos blockPos, @Local(ordinal = 1) BlockPos blockPos2) {
        ParticleRain.doAdditionalWeatherSounds(level, blockPos, blockPos2, ci);
    }

    // make rain sound use the mods rain volume slider
    @WrapOperation(method = "tickRainParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;playLocalSound(Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"))
    public void adjustRainVolume(ClientLevel instance, BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, boolean distanceDelay, Operation<Void> original) {
        original.call(instance, blockPos, soundEvent, soundSource, config.sound.rainVolume, pitch, distanceDelay);
    }

    // particle status MINIMAL disables splash particles
    @Inject(method = "tickRainParticles", at = @At("HEAD"))
    public void tickRainParticles(ClientLevel level, Camera camera, int ticks, ParticleStatus particleStatus, CallbackInfo ci, @Local(argsOnly = true) LocalRef<ParticleStatus> particleStatusLocalRef) {
        if (!config.compat.doDefaultSplashing) {
            particleStatusLocalRef.set(ParticleStatus.MINIMAL);
        }
    }

    // prevent rendering weather column instances
    //? if >=1.21.9 {
    /^@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    public void render(Level level, int ticks, float partialTick, Vec3 cameraPosition, WeatherRenderState reusedState, CallbackInfo ci) {
    ^///?} else {
    @Inject(method = "render(Lnet/minecraft/world/level/Level;Lnet/minecraft/client/renderer/MultiBufferSource;IFLnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"), cancellable = true)
    public void render(Level level, MultiBufferSource bufferSource, int ticks, float partialTick, Vec3 cameraPosition, CallbackInfo ci) {
    //?}
        if (!config.compat.renderDefaultWeather) {
            ci.cancel();
        }
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
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.VersionUtil;
import pigcart.particlerain.config.ConfigManager;

import static pigcart.particlerain.config.ConfigManager.config;

@Mixin(LevelRenderer.class)
public class WeatherEffectRendererMixin {

    // bypass precipitation check so we can share the sound placement calculations with non-rain sounds
    @WrapOperation(method = "tickRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;"))
    public Holder<Biome> getBiomeValue(LevelReader instance, BlockPos pos, Operation<Holder<Biome>> original) {
        // mixin somehow can't resolve target getPrecipitationAt so lets just replace the gotten biome with a rainy one instead
        return Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS);
    }

    // insert additional sounds without replacing vanilla code block where rain sounds are played
    @Inject(method = "tickRain", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER, ordinal = 1, target = "Lnet/minecraft/client/renderer/LevelRenderer;rainSoundTime:I"), cancellable = true)
    public void hookWeatherSounds(Camera camera, CallbackInfo ci, @Local(ordinal = 0) BlockPos blockPos, @Local(ordinal = 1) BlockPos blockPos2) {
        ParticleRain.doAdditionalWeatherSounds(Minecraft.getInstance().level, blockPos, blockPos2, ci);
    }

    // make rain sound use the mods rain volume slider
    @WrapOperation(method = "tickRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;playLocalSound(Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"))
    public void adjustRainVolume(ClientLevel instance, BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, boolean distanceDelay, Operation<Void> original) {
        original.call(instance, blockPos, soundEvent, soundSource, config.sound.rainVolume, pitch, distanceDelay);
    }

    // particle status MINIMAL disables splash particles
    @WrapOperation(method = "tickRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
    public Object optionsParticlesGet(OptionInstance instance, Operation<ParticleStatus> original) {
        if (!ConfigManager.config.compat.doDefaultSplashing) {
            return ParticleStatus.MINIMAL;
        }
        return original.call(instance);
    }

    // prevent rendering weather column instances
    @Inject(method = "renderSnowAndRain", at = @At("HEAD"), cancellable = true)
    public void render(LightTexture lightTexture, float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
        if (!ConfigManager.config.compat.renderDefaultWeather) {
            ci.cancel();
        }
    }
}
//?}