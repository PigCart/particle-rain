package pigcart.particlerain;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.particlerain.config.ConfigManager;
import pigcart.particlerain.config.ConfigScreens;
import pigcart.particlerain.particle.CustomParticle;
//? if >=1.21.9 {
/*import net.minecraft.client.gui.components.debug.DebugScreenEntries;
*///?}

import java.util.List;
import java.util.Set;

import static pigcart.particlerain.config.ConfigManager.config;

public class ParticleRain {
    public static int clientTicks = 0;
    public static final String MOD_ID = "particlerain";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static SimpleParticleType MIST;
    public static SimpleParticleType SHRUB;
    public static SimpleParticleType RIPPLE;
    public static SimpleParticleType STREAK;

    public static Set<String> particleConfigIds = Set.of("shrub", "ripple", "streak", "mist");

    public static SoundEvent WEATHER_SNOW;
    public static SoundEvent WEATHER_SNOW_ABOVE;
    public static SoundEvent WEATHER_SANDSTORM;
    public static SoundEvent WEATHER_SANDSTORM_ABOVE;

    private static List<String> getDebugLines() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return List.of();
        BlockPos blockPos = BlockPos.containing(Minecraft.getInstance().player.position());
        final Holder<Biome> biome = level.getBiome(blockPos);
        Biome.Precipitation precipitation = VersionUtil.getPrecipitationAt(level, biome, blockPos);
        return List.of(
                String.format("Tracked particles: %d/%d",WeatherParticleManager.getParticleCount(), WeatherParticleManager.particleGroup./*? if >=1.21.9 {*//*limit*//*?} else {*/getLimit/*?}*/()),
                "after Weather Ticks Left: " + WeatherParticleManager.afterWeatherTicksLeft,
                "spawn Attempts Until Block FX Idle: " + WeatherParticleManager.spawnAttemptsUntilBlockFXIdle,
                "ticks Until Sky FX Idle: " + WeatherParticleManager.ticksUntilSkyFXIdle,
                "ticks Until Surface FX Idle: " + WeatherParticleManager.ticksUntilSurfaceFXIdle,
                "is Raining: " + level.isRaining(),
                "Biome Precipitation: " + precipitation,
                "Wind multiplier: " + CustomParticle.yLevelWindMultiplier(blockPos.getY())
        );
    }

    public static void onInitializeClient() {
        ConfigManager.load();

        WEATHER_SNOW = createSoundEvent("weather.snow");
        WEATHER_SNOW_ABOVE = createSoundEvent("weather.snow.above");
        WEATHER_SANDSTORM = createSoundEvent("weather.sandstorm");
        WEATHER_SANDSTORM_ABOVE = createSoundEvent("weather.sandstorm.above");

        //? if >=1.21.9 {
        /*DebugScreenEntries.register(VersionUtil.getId("debug"),
                (display, level, levelChunk, levelChunk2) ->
                        display.addToGroup(VersionUtil.getId("debuglines"), getDebugLines())
        );
        *///?}
    }

    public static void onTick(Minecraft client) {
        final Camera camera = client.gameRenderer.getMainCamera();
        if (!client.isPaused() && client.level != null && camera.isInitialized()) {
            clientTicks++;
            WeatherParticleManager.tick(client.level, camera.getPosition());
        }
    }

    private static SoundEvent createSoundEvent(String name) {
        ResourceLocation id = VersionUtil.getId(name);
        return SoundEvent.createVariableRangeEvent(id);
    }
    @SuppressWarnings("unchecked")
    public static <S> LiteralArgumentBuilder<S> getCommands() {
        return (LiteralArgumentBuilder<S>) LiteralArgumentBuilder.literal(MOD_ID)
                .executes(ctx -> {
                    // give minecraft a tick to close the chat screen
                    VersionUtil.schedule(() -> Minecraft.getInstance().setScreen(ConfigScreens.generateMainConfigScreen(null)));
                    return 0;
                })
                .then(LiteralArgumentBuilder.literal("debug")
                        .executes(ctx -> {
                            getDebugLines().forEach(ParticleRain::addChatMsg);
                            return 0;
                        })
                );
    }
    private static void addChatMsg(String message) {
        Minecraft.getInstance().gui.getChat().addMessage(Component.literal(message));
    }
    public static void doAdditionalWeatherSounds(ClientLevel level, BlockPos cameraPos, BlockPos rainPos, CallbackInfo ci) {
        if (config.compat.doSpawnHeightLimit) {
            int cloudHeight = config.compat.spawnHeightLimit == 0 ? VersionUtil.getCloudHeight(level) : config.compat.spawnHeightLimit;
            if (rainPos.getY() > cloudHeight) {
                ci.cancel();
                return;
            }
        }
        boolean above = rainPos.getY() > cameraPos.getY() + 1
                && level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, cameraPos).getY() > Mth.floor((float)cameraPos.getY());
        Holder<Biome> biome = level.getBiome(rainPos);
        Biome.Precipitation precipitation = VersionUtil.getPrecipitationAt(level, biome, rainPos);
        if (precipitation == Biome.Precipitation.SNOW && config.sound.snowVolume > 0) {
            SoundEvent sound = above ? ParticleRain.WEATHER_SNOW_ABOVE : ParticleRain.WEATHER_SNOW;
            level.playLocalSound(rainPos, sound, SoundSource.WEATHER, config.sound.snowVolume, above ? 0.5F : 1.0F, false);
        } else if (precipitation == Biome.Precipitation.NONE && biome.value().getBaseTemperature() > 0.25 && config.sound.windVolume > 0) {
            SoundEvent sound = above ? ParticleRain.WEATHER_SANDSTORM_ABOVE : ParticleRain.WEATHER_SANDSTORM;
            level.playLocalSound(rainPos, sound, SoundSource.WEATHER, config.sound.windVolume, above ? 0.5F : 1.0F, false);
        } else if (config.sound.blockVolume > 0) {
            final BlockState state = level.getBlockState(rainPos);
            final SoundType soundType = state.getSoundType();
            if (!soundType.equals(SoundType.STONE)) {
                // stone type sounds awful. hypixel lobby ASMR
                if (state.is(Blocks.NOTE_BLOCK)) {
                    final SoundEvent sound = state.getValue(NoteBlock.INSTRUMENT).getSoundEvent().value();
                    final float pitch = NoteBlock.getPitchFromNote(level.random.nextInt(24));
                    level.playLocalSound(rainPos, sound, SoundSource.WEATHER, config.sound.blockVolume, above ? pitch / 2 : pitch, false);
                } else {
                    final SoundEvent sound = soundType.getHitSound();
                    level.playLocalSound(rainPos, sound, SoundSource.WEATHER, config.sound.blockVolume, above ? 0.5F : 1.5F, false);
                }
            }
        }

        // have to cancel rain sounds when necessary because of bypassing the initial precipitation check
        if (config.sound.rainVolume == 0 || !precipitation.equals(Biome.Precipitation.RAIN)) {
            ci.cancel();
        }
    }
}