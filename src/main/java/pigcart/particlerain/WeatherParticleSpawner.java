package pigcart.particlerain;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

import static pigcart.particlerain.config.ModConfig.INSTANCE;

public final class WeatherParticleSpawner {

    private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    private static void spawnParticle(ClientLevel level, Holder<Biome> biome, double x, double y, double z) {
        if (ParticleRainClient.particleCount > INSTANCE.perf.maxParticleAmount) {
            return;
        } else if (!INSTANCE.spawn.canSpawnAboveClouds && y > INSTANCE.spawn.cloudHeight) {
            y = INSTANCE.spawn.cloudHeight;
        }
        if (INSTANCE.effect.doFogParticles && level.random.nextFloat() < INSTANCE.fog.density) {
            level.addParticle(ParticleRainClient.FOG, x, y, z, 0, 0, 0);
        }
        //TODO: add toggle for clamping to heightmap position
        final BlockPos getPrecipitationFromBlockPos = INSTANCE.spawn.useHeightmapTemp ? level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos) : pos ;
        Precipitation precipitation = biome.value().getPrecipitationAt(getPrecipitationFromBlockPos,level.getSeaLevel());
        //biome.value().hasPrecipitation() isn't reliable for modded biomes and seasons
        if (precipitation == Precipitation.RAIN) {
            if (INSTANCE.effect.doGroundFogParticles && ParticleRainClient.fogCount < INSTANCE.groundFog.density) {
                int height = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z);
                if (height <= INSTANCE.groundFog.spawnHeight && height >= INSTANCE.groundFog.spawnHeight - 4 && level.getFluidState(BlockPos.containing(x, height - 1, z)).isEmpty()) {
                    level.addParticle(ParticleRainClient.GROUND_FOG, x, height + level.random.nextFloat(), z, 0, 0, 0);
                }
            }
            if (INSTANCE.effect.doRainParticles && level.random.nextFloat() < INSTANCE.rain.density) {
                level.addParticle(ParticleRainClient.RAIN, x, y, z, 0, 0, 0);
            }
        } else if (precipitation == Precipitation.SNOW && INSTANCE.effect.doSnowParticles) {
            if (level.random.nextFloat() < INSTANCE.snow.density) {
                level.addParticle(ParticleRainClient.SNOW, x, y, z, 0, 0, 0);
            }
        } else if (doesThisBlockHaveDustBlowing(precipitation, level, BlockPos.containing(x, y, z), biome)) {
            if (INSTANCE.dust.spawnOnGround) y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z);
            if (INSTANCE.effect.doDustParticles) {
                if (level.random.nextFloat() < INSTANCE.dust.density) {
                    level.addParticle(ParticleRainClient.DUST, x, y, z, 0, 0, 0);
                }
            }
            if (INSTANCE.effect.doShrubParticles) {
                if (level.random.nextFloat() < INSTANCE.shrub.density) {
                    level.addParticle(ParticleRainClient.SHRUB, x, y, z, 0, 0, 0);
                }
            }
    }
    }

    public static void update(ClientLevel level, Entity entity, float partialTicks) {
        //TODO: twilight fog and skittering sand when not raining
        if (level.isRaining() || INSTANCE.compat.alwaysRaining) {
            int density;
            if (level.isThundering())
                if (INSTANCE.compat.alwaysRaining) {
                    density = INSTANCE.perf.particleStormDensity;
                } else {
                    density = (int) (INSTANCE.perf.particleStormDensity * level.getRainLevel(partialTicks));
                }
            else if (INSTANCE.compat.alwaysRaining) {
                density = INSTANCE.perf.particleDensity;
            } else {
                density = (int) (INSTANCE.perf.particleDensity * level.getRainLevel(partialTicks));
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
                double x = INSTANCE.perf.particleRadius * Mth.sin(phi) * Math.cos(theta);
                double y = INSTANCE.perf.particleRadius * Mth.sin(phi) * Math.sin(theta);
                double z = INSTANCE.perf.particleRadius * Mth.cos(phi);

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
        Precipitation precipitation = biome.value().getPrecipitationAt(blockPos, Minecraft.getInstance().level.getSeaLevel());
        if (precipitation == Precipitation.RAIN && INSTANCE.sound.doRainSounds) {
            return above ? SoundEvents.WEATHER_RAIN_ABOVE : SoundEvents.WEATHER_RAIN;
        } else if (precipitation == Precipitation.SNOW && INSTANCE.sound.doSnowSounds) {
            return above ? ParticleRainClient.WEATHER_SNOW_ABOVE : ParticleRainClient.WEATHER_SNOW;
        } else if (doesThisBlockHaveDustBlowing(precipitation, Minecraft.getInstance().level, blockPos, biome) && INSTANCE.sound.doSandSounds) {
        return above ? ParticleRainClient.WEATHER_SANDSTORM_ABOVE : ParticleRainClient.WEATHER_SANDSTORM;
        }
        return null;
    }

    public static boolean doesThisBlockHaveDustBlowing(Precipitation precipitation, ClientLevel level, BlockPos blockPos, Holder<Biome> biome) {
        boolean matchesTag = false;
        for (int i = 0; i < INSTANCE.spawn.dustyBlockTags.size(); i++) {
            if (level.getBlockState(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).below()).is(TagKey.create(Registries.BLOCK, ResourceLocation.parse(INSTANCE.spawn.dustyBlockTags.get(i))))) {
                matchesTag = true;
                break;
            }
        }
        return precipitation == Precipitation.NONE && matchesTag && biome.value().getBaseTemperature() > 0.25;
    }
}