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
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.StonecutterUtil;
import pigcart.particlerain.WeatherParticleManager;
import pigcart.particlerain.config.ModConfig;

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
    public void doAdditionalWeatherSounds(ClientLevel level, Camera camera, int ticks, ParticleStatus particleStatus, CallbackInfo ci, @Local(ordinal = 0) BlockPos blockPos, @Local(ordinal = 1) BlockPos blockPos2) {
        if (blockPos2.getY() > blockPos.getY() + 1 && level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).getY() > Mth.floor((float)blockPos.getY())) {
            SoundEvent sound = WeatherParticleManager.getAdditionalWeatherSounds(level, blockPos2, true);
            if (sound != null) level.playLocalSound(blockPos2, sound, SoundSource.WEATHER, 0.1F, 0.5F, false);
        } else {
            SoundEvent sound = WeatherParticleManager.getAdditionalWeatherSounds(level, blockPos2, false);
            if (sound != null) level.playLocalSound(blockPos2, sound, SoundSource.WEATHER, 0.2F, 1.0F, false);
        }
        // have to cancel rain sounds when necessary because of bypassing the initial precipitation check
        if (!ModConfig.CONFIG.sound.doRainSounds || !StonecutterUtil.getPrecipitationAt(level, level.getBiome(blockPos2).value(), blockPos2).equals(Biome.Precipitation.RAIN)) {
            ci.cancel();
        }
    }

    // particle status MINIMAL disables splash particles
    @Inject(method = "tickRainParticles", at = @At("HEAD"))
    public void tickRainParticles(ClientLevel level, Camera camera, int ticks, ParticleStatus particleStatus, CallbackInfo ci, @Local(argsOnly = true) LocalRef<ParticleStatus> particleStatusLocalRef) {
        if (!ModConfig.CONFIG.compat.doDefaultSplashing) {
            particleStatusLocalRef.set(ParticleStatus.MINIMAL);
        }
    }

    // prevent rendering weather column instances
    @Inject(method = "render(Lnet/minecraft/world/level/Level;Lnet/minecraft/client/renderer/MultiBufferSource;IFLnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"), cancellable = true)
    public void render(Level level, MultiBufferSource bufferSource, int ticks, float partialTick, Vec3 cameraPosition, CallbackInfo ci) {
        if (!ModConfig.CONFIG.compat.renderDefaultWeather) {
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
import net.minecraft.world.level.levelgen.Heightmap;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.particlerain.StonecutterUtil;
import pigcart.particlerain.WeatherParticleManager;
import pigcart.particlerain.config.ModConfig;

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
    public void doAdditionalWeatherSounds(Camera camera, CallbackInfo ci, @Local(ordinal = 0) BlockPos blockPos, @Local(ordinal = 1) BlockPos blockPos2) {
        ClientLevel level = Minecraft.getInstance().level;
        if (blockPos2.getY() > blockPos.getY() + 1 && level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).getY() > Mth.floor((float)blockPos.getY())) {
            SoundEvent sound = WeatherParticleManager.getAdditionalWeatherSounds(level, blockPos2, true);
            if (sound != null) level.playLocalSound(blockPos2, sound, SoundSource.WEATHER, 0.1F, 0.5F, false);
        } else {
            SoundEvent sound = WeatherParticleManager.getAdditionalWeatherSounds(level, blockPos2, false);
            if (sound != null) level.playLocalSound(blockPos2, sound, SoundSource.WEATHER, 0.2F, 1.0F, false);
        }
        // have to cancel rain sounds when necessary because of bypassing the initial precipitation check
        if (!ModConfig.CONFIG.sound.doRainSounds || !StonecutterUtil.getPrecipitationAt(level, level.getBiome(blockPos2).value(), blockPos2).equals(Biome.Precipitation.RAIN)) {
            ci.cancel();
        }
    }

    // particle status MINIMAL disables splash particles
    @WrapOperation(method = "tickRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
    public Object optionsParticlesGet(OptionInstance instance, Operation<ParticleStatus> original) {
        if (!ModConfig.CONFIG.compat.doDefaultSplashing) {
            return ParticleStatus.MINIMAL;
        }
        return original.call(instance);
    }

    /*@WrapOperation(method = "tickRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    public void addParticle(ClientLevel level, ParticleOptions particleOptions, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, Operation<Void> original, @Local FluidState fluidState, @Local(ordinal = 0) BlockPos blockPos, @Local(ordinal = 1) BlockPos blockPos2, @Local Biome biome) {
        if (blockPos.distToCenterSqr(x, y, z) / 100 < level.random.nextFloat()) {
            if (ModConfig.CONFIG.effect.doRippleParticles && fluidState.is(Fluids.WATER)) {
                level.addParticle(ParticleRain.RIPPLE, x, y, z, 0, 0, 0);
                if (level.isThundering()) original.call(level, particleOptions, x, y, z, xSpeed, ySpeed, zSpeed);
            } else if (level.getBiome(blockPos2).value().getPrecipitationAt(blockPos2).equals(Biome.Precipitation.RAIN)) {
                //TODO if ParticleRain.isHailingAt
                //TODO if ParticleRain.isSleetingAt
                original.call(level, particleOptions, x, y, z, xSpeed, ySpeed, zSpeed);
            }
        }
    }*/

    // prevent rendering weather column instances
    @Inject(method = "renderSnowAndRain", at = @At("HEAD"), cancellable = true)
    public void render(LightTexture lightTexture, float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
        if (!ModConfig.CONFIG.compat.renderDefaultWeather) {
            ci.cancel();
        }
    }
}
//?}