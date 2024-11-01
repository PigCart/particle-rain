package pigcart.particlerain;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

public final class WeatherParticleSpawner {

    private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    private static void spawnParticle(ClientLevel level, Holder<Biome> biome, double x, double y, double z) {
        //TODO: per biome overrides to whitelist/blacklist effects for specific biomes
        //TODO: make sand rise from the ground
        if (ParticleRainClient.particleCount > ParticleRainClient.config.maxParticleAmount) {
            return;
        }
        if (ParticleRainClient.config.doFogParticles && level.random.nextFloat() < ParticleRainClient.config.fog.density / 100F) {
            level.addParticle(ParticleRainClient.FOG, x, y, z, 0, 0, 0);
        }
        Precipitation precipitation = biome.value().getPrecipitationAt(pos);
        //biome.value().hasPrecipitation() isn't reliable for modded biomes and seasons
        if (precipitation == Precipitation.RAIN && ParticleRainClient.config.doRainParticles && level.random.nextFloat() < ParticleRainClient.config.rain.density / 100F) {
            level.addParticle(ParticleRainClient.RAIN, x, y, z, 0, 0, 0);
        } else if (precipitation == Precipitation.SNOW && ParticleRainClient.config.doSnowParticles) {
            if (level.random.nextFloat() < ParticleRainClient.config.snow.density / 100F) {
                level.addParticle(ParticleRainClient.SNOW, x, y, z, 0, 0, 0);
            }
        } else if (precipitation == Precipitation.NONE && String.valueOf(BuiltInRegistries.BLOCK.getKey(level.getBlockState(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(x, y, z)).below()).getBlock())).contains("sand") && biome.value().getBaseTemperature() > 0.25) {
            // this is for future me to optimize. offers decent out of the box support for modded biomes.
            if (ParticleRainClient.config.doSandParticles) {
                if (level.random.nextFloat() < ParticleRainClient.config.sand.density / 100F) {
                    level.addParticle(ParticleRainClient.DUST, x, y, z, 0, 0, 0);
                }
            }
            if (ParticleRainClient.config.doShrubParticles) {
                if (level.random.nextFloat() < ParticleRainClient.config.shrub.density / 1000F) {
                    level.addParticle(ParticleRainClient.SHRUB, x, y, z, 0, 0, 0);
                }
            }
    }
    }

    public static void update(ClientLevel level, Entity entity, float partialTicks) {
        if (level.isRaining() || ParticleRainClient.config.alwaysRaining) {
            int density;
            if (level.isThundering())
                if (ParticleRainClient.config.alwaysRaining) {
                    density = ParticleRainClient.config.particleStormDensity;
                } else {
                    density = (int) (ParticleRainClient.config.particleStormDensity * level.getRainLevel(partialTicks));
                }
            else if (ParticleRainClient.config.alwaysRaining) {
                density = ParticleRainClient.config.particleDensity;
            } else {
                density = (int) (ParticleRainClient.config.particleDensity * level.getRainLevel(partialTicks));
            }


            RandomSource rand = RandomSource.create();

            for (int pass = 0; pass < density; pass++) {

                float theta = (float) (2 * Math.PI * rand.nextFloat());
                float phi = (float) Math.acos(2 * rand.nextFloat() - 1);
                double x = ParticleRainClient.config.particleRadius * Mth.sin(phi) * Math.cos(theta);
                double y = ParticleRainClient.config.particleRadius * Mth.sin(phi) * Math.sin(theta);
                double z = ParticleRainClient.config.particleRadius * Mth.cos(phi);

                pos.set(x + entity.getX(), y + entity.getY(), z + entity.getZ());
                if (level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) > pos.getY())
                    continue;

                spawnParticle(level, level.getBiome(pos), pos.getX() + rand.nextFloat(), pos.getY() + rand.nextFloat(), pos.getZ() + rand.nextFloat());
            }
        }
    }

    @Nullable
    public static SoundEvent getBiomeSound(BlockPos blockPos, boolean above) {
        Holder<Biome> biome = Minecraft.getInstance().level.getBiome(blockPos);
        if (biome.value().hasPrecipitation()) {
            if (biome.value().getPrecipitationAt(blockPos) == Precipitation.RAIN) {
                return above ? SoundEvents.WEATHER_RAIN_ABOVE : SoundEvents.WEATHER_RAIN;
            } else if (biome.value().getPrecipitationAt(blockPos) == Precipitation.SNOW) {
                return above ? ParticleRainClient.WEATHER_SNOW_ABOVE : ParticleRainClient.WEATHER_SNOW;
            }
        } else if (biome.value().getPrecipitationAt(blockPos) == Precipitation.NONE && String.valueOf(BuiltInRegistries.BLOCK.getKey(Minecraft.getInstance().level.getBlockState(Minecraft.getInstance().level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).below()).getBlock())).contains("sand") && biome.value().getBaseTemperature() >= 1.0F) {
            return above ? ParticleRainClient.WEATHER_SANDSTORM_ABOVE : ParticleRainClient.WEATHER_SANDSTORM;
        }
        return null;
    }
}