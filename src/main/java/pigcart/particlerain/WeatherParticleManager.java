package pigcart.particlerain;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import static pigcart.particlerain.config.ModConfig.CONFIG;

public final class WeatherParticleManager {

    public static int particleCount;
    public static int fogCount;
    private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    private static void spawnParticle(ClientLevel level, Holder<Biome> biome, double x, double y, double z) {
        if (particleCount > CONFIG.perf.maxParticleAmount) {
            return;
        } else if (!CONFIG.spawn.canSpawnAboveClouds && y > CONFIG.spawn.cloudHeight) {
            y = CONFIG.spawn.cloudHeight;
        }
        if (CONFIG.effect.doFogParticles && level.random.nextFloat() < CONFIG.fog.density) {
            level.addParticle(ParticleRainClient.FOG, x, y, z, 0, 0, 0);
        }
        final BlockPos precipitationSamplePos = CONFIG.spawn.useHeightmapTemp ? level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos) : pos ;
        Precipitation precipitation = CONFIG.spawn.doOverrideWeather ? CONFIG.spawn.overrideWeather : StonecutterUtil.getPrecipitationAt(level, biome.value(), precipitationSamplePos);
        //biome.value().hasPrecipitation() isn't reliable for modded biomes and seasons
        if (precipitation == Precipitation.RAIN) {
            if (CONFIG.effect.doMistParticles && fogCount < CONFIG.mist.density) {
                int height = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z);
                int distance = (int) new Vec3(x, height, z).distanceToSqr(Minecraft.getInstance().cameraEntity.position());
                if (distance > Mth.square(CONFIG.perf.particleDistance) - 2 && height <= CONFIG.mist.maxSpawnHeight && height >= CONFIG.mist.minSpawnHeight && level.getFluidState(BlockPos.containing(x, height - 1, z)).isEmpty()) {
                    level.addParticle(ParticleRainClient.MIST, x, height + level.random.nextFloat(), z, 0, 0, 0);
                }
            }
            if (CONFIG.effect.doRainParticles && level.random.nextFloat() < CONFIG.rain.density) {
                level.addParticle(ParticleRainClient.RAIN, x, y, z, 0, 0, 0);
            }
        } else if (precipitation == Precipitation.SNOW && CONFIG.effect.doSnowParticles) {
            if (level.random.nextFloat() < CONFIG.snow.density) {
                level.addParticle(ParticleRainClient.SNOW, x, y, z, 0, 0, 0);
            }
        } else if (doesThisBlockHaveDustBlowing(precipitation, level, pos, biome)) {
            if (CONFIG.effect.doDustParticles) {
                level.addParticle(ParticleRainClient.DUST, x, y, z, 0, 0, 0);
                if (CONFIG.dust.spawnOnGround) y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z);
                if (level.random.nextFloat() < CONFIG.dust.density) {
                    level.addParticle(ParticleRainClient.DUST, x, y, z, 0, 0, 0);
                }
            }
            if (CONFIG.effect.doShrubParticles) {
                if (level.random.nextFloat() < (CONFIG.shrub.density / 10)) {
                    level.addParticle(ParticleRainClient.SHRUB, x, y, z, 0, 0, 0);
                }
            }
        }
    }

    public static void tick(ClientLevel level, Entity cameraEntity) {
        //TODO: twilight fog and skittering sand when not raining
        if (level.isRaining()) {
            int density = (int) (Mth.lerpInt(level.getThunderLevel(1.0F), CONFIG.perf.particleDensity, CONFIG.perf.particleStormDensity) * level.getRainLevel(1.0F));
            final float speed = (float) Minecraft.getInstance().getCameraEntity().getDeltaMovement().length();
            density = (int) (density * ((speed * 2) + 1));

            for (int i = 0; i < density; i++) {

                float theta = Mth.TWO_PI * level.random.nextFloat();
                float h = level.random.nextFloat();
                if (h < level.random.nextFloat()) continue; // bias spawning to top of sphere
                float phi = (float) Math.acos((2 * h) - 1);
                double x = CONFIG.perf.particleDistance * Mth.sin(phi) * Math.cos(theta);
                double y = CONFIG.perf.particleDistance * Mth.cos(phi);
                double z = CONFIG.perf.particleDistance * Mth.sin(phi) * Math.sin(theta);

                pos.set(x + cameraEntity.getX(), y + cameraEntity.getY(), z + cameraEntity.getZ());
                if (level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) > pos.getY())
                    continue;

                spawnParticle(level, level.getBiome(pos), pos.getX() + level.random.nextFloat(), pos.getY() + level.random.nextFloat(), pos.getZ() + level.random.nextFloat());

            }
        }
    }

    @Nullable
    public static SoundEvent getAdditionalWeatherSounds(ClientLevel level, BlockPos blockPos, boolean above) {
        Holder<Biome> biome = level.getBiome(blockPos);
        Precipitation precipitation = CONFIG.spawn.doOverrideWeather ? CONFIG.spawn.overrideWeather : StonecutterUtil.getPrecipitationAt(level, biome.value(), blockPos);
        if (precipitation == Precipitation.SNOW && CONFIG.sound.doSnowSounds) {
            return above ? ParticleRainClient.WEATHER_SNOW_ABOVE : ParticleRainClient.WEATHER_SNOW;
        } else if (doesThisBlockHaveDustBlowing(precipitation, level, blockPos, biome) && CONFIG.sound.doWindSounds) {
            return above ? ParticleRainClient.WEATHER_SANDSTORM_ABOVE : ParticleRainClient.WEATHER_SANDSTORM;
        }
        return null;
    }

    public static boolean doesThisBlockHaveDustBlowing(Precipitation precipitation, ClientLevel level, BlockPos blockPos, Holder<Biome> biome) {
        boolean matchesTag = false;
        for (int i = 0; i < CONFIG.spawn.dustyBlockTags.size(); i++) {
            if (level.getBlockState(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).below()).is(TagKey.create(Registries.BLOCK, StonecutterUtil.parseResourceLocation(CONFIG.spawn.dustyBlockTags.get(i))))) {
                matchesTag = true;
                break;
            }
        }
        return precipitation == Precipitation.NONE && matchesTag && biome.value().getBaseTemperature() > 0.25;
    }

    public static boolean canHostStreaks(BlockState state) {
        return state.is(BlockTags.IMPERMEABLE) || state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(ParticleRainClient.GLASS_PANES);
    }

    public static void resetParticleCount() {
        particleCount = 0;
        fogCount = 0;
    }
}