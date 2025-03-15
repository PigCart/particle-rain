package pigcart.particlerain;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
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
        final BlockPos getPrecipitationFromBlockPos = CONFIG.spawn.useHeightmapTemp ? level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos) : pos ;
        Precipitation precipitation = CONFIG.spawn.doOverrideWeather ? CONFIG.spawn.overrideWeather : biome.value().getPrecipitationAt(getPrecipitationFromBlockPos,level.getSeaLevel());
        //biome.value().hasPrecipitation() isn't reliable for modded biomes and seasons
        if (precipitation == Precipitation.RAIN) {
            if (CONFIG.effect.doGroundFogParticles && fogCount < CONFIG.groundFog.density) {
                int height = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z);
                if (height <= CONFIG.groundFog.maxSpawnHeight && height >= CONFIG.groundFog.minSpawnHeight && level.getFluidState(BlockPos.containing(x, height - 1, z)).isEmpty()) {
                    level.addParticle(ParticleRainClient.GROUND_FOG, x, height + level.random.nextFloat(), z, 0, 0, 0);
                }
            }
            if (CONFIG.effect.doRainParticles && level.random.nextFloat() < CONFIG.rain.density) {
                level.addParticle(ParticleRainClient.RAIN, x, y, z, 0, 0, 0);
            }
        } else if (precipitation == Precipitation.SNOW && CONFIG.effect.doSnowParticles) {
            if (level.random.nextFloat() < CONFIG.snow.density) {
                level.addParticle(ParticleRainClient.SNOW, x, y, z, 0, 0, 0);
            }
        } else if (doesThisBlockHaveDustBlowing(precipitation, level, BlockPos.containing(x, y, z), biome)) {
            if (CONFIG.dust.spawnOnGround) y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z);
            if (CONFIG.effect.doDustParticles) {
                if (level.random.nextFloat() < CONFIG.dust.density) {
                    level.addParticle(ParticleRainClient.DUST, x, y, z, 0, 0, 0);
                }
            }
            if (CONFIG.effect.doShrubParticles) {
                if (level.random.nextFloat() < CONFIG.shrub.density) {
                    level.addParticle(ParticleRainClient.SHRUB, x, y, z, 0, 0, 0);
                }
            }
        }
    }

    public static void tick(ClientLevel level, Entity entity) {
        //TODO: twilight fog and skittering sand when not raining
        if (level.isRaining() || CONFIG.compat.alwaysRaining) {
            int density;
            if (level.isThundering())
                if (CONFIG.compat.alwaysRaining) {
                    density = CONFIG.perf.particleStormDensity;
                } else {
                    density = (int) (CONFIG.perf.particleStormDensity * level.getRainLevel(0));
                }
            else if (CONFIG.compat.alwaysRaining) {
                density = CONFIG.perf.particleDensity;
            } else {
                density = (int) (CONFIG.perf.particleDensity * level.getRainLevel(0));
            }

            //TODO: calculate vertical velocity and use it to switch which hemisphere is spawning particles
            // half of particle spawn calculations are wasted on checking blocks below ground
            // or blocks where the particle would immediately fall out of the particle render distance
            RandomSource rand = RandomSource.create();

            for (int pass = 0; pass < density; pass++) {

                //TODO: bias particle spawn weighting to center of current spawning hemisphere
                // current solution ends up being biased towards the edges since the particles fall vertically
                // in many scenes particles that dont spawn almost above the player end up out of view
                // having more particles closer to the player helps the texture planes be less noticable
                // and make the effect look less patchy.
                float theta = (float) (2 * Math.PI * rand.nextFloat());
                float phi = (float) Math.acos(2 * rand.nextFloat() - 1);
                double x = CONFIG.perf.particleRadius * Mth.sin(phi) * Math.cos(theta);
                double y = CONFIG.perf.particleRadius * Mth.sin(phi) * Math.sin(theta);
                double z = CONFIG.perf.particleRadius * Mth.cos(phi);

                pos.set(x + entity.getX(), y + entity.getY(), z + entity.getZ());
                if (level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) > pos.getY())
                    continue;

                spawnParticle(level, level.getBiome(pos), pos.getX() + rand.nextFloat(), pos.getY() + rand.nextFloat(), pos.getZ() + rand.nextFloat());
            }
        }
    }

    //TODO: better weather sounds
    @Nullable
    public static SoundEvent getBiomeSound(BlockPos blockPos, boolean above) {
        Holder<Biome> biome = Minecraft.getInstance().level.getBiome(blockPos);
        Precipitation precipitation = CONFIG.spawn.doOverrideWeather ? CONFIG.spawn.overrideWeather : biome.value().getPrecipitationAt(blockPos,Minecraft.getInstance().level.getSeaLevel());
        if (precipitation == Precipitation.RAIN && CONFIG.sound.doRainSounds) {
            return above ? SoundEvents.WEATHER_RAIN_ABOVE : SoundEvents.WEATHER_RAIN;
        } else if (precipitation == Precipitation.SNOW && CONFIG.sound.doSnowSounds) {
            return above ? ParticleRainClient.WEATHER_SNOW_ABOVE : ParticleRainClient.WEATHER_SNOW;
        } else if (doesThisBlockHaveDustBlowing(precipitation, Minecraft.getInstance().level, blockPos, biome) && CONFIG.sound.doSandSounds) {
        return above ? ParticleRainClient.WEATHER_SANDSTORM_ABOVE : ParticleRainClient.WEATHER_SANDSTORM;
        }
        return null;
    }

    public static boolean doesThisBlockHaveDustBlowing(Precipitation precipitation, ClientLevel level, BlockPos blockPos, Holder<Biome> biome) {
        boolean matchesTag = false;
        for (int i = 0; i < CONFIG.spawn.dustyBlockTags.size(); i++) {
            if (level.getBlockState(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).below()).is(TagKey.create(Registries.BLOCK, ResourceLocation.parse(CONFIG.spawn.dustyBlockTags.get(i))))) {
                matchesTag = true;
                break;
            }
        }
        return precipitation == Precipitation.NONE && matchesTag && biome.value().getBaseTemperature() > 0.25;
    }

    public static boolean canHostStreaks(BlockState state) {
        return state.is(BlockTags.IMPERMEABLE) || state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(ConventionalBlockTags.GLASS_PANES);
    }

    public static void resetParticleCount() {
        particleCount = 0;
        fogCount = 0;
    }
}