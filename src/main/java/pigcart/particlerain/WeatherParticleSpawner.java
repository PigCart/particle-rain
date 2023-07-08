package pigcart.particlerain;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

public final class WeatherParticleSpawner {

    private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    private WeatherParticleSpawner() {
    }

    private static void spawnParticle(ClientLevel level, Holder<Biome> biome, double x, double y, double z) {
        if (biome.value().hasPrecipitation()) {
            if (biome.value().getBaseTemperature() >= 0.15F) {
                if (ParticleRainClient.config.doRainParticles)
                    level.addParticle(ParticleRainClient.RAIN_DROP, x, y, z, 0, 0, 0);
            } else {
                if (ParticleRainClient.config.doSnowParticles && level.getRandom().nextFloat() < 0.2F)
                    level.addParticle(ParticleRainClient.SNOW_FLAKE, x, y, z, 0, 0, 0);
            }
        } else if (ParticleRainClient.config.doSandParticles && level.getRandom().nextFloat() < 0.5F) {
            if (biome.is(Biomes.DESERT)) {
                level.addParticle(ParticleRainClient.DESERT_DUST, x, y, z, 0, 0, 0);
            } else if (biome.is(BiomeTags.IS_BADLANDS)) {
                level.addParticle(ParticleRainClient.DESERT_DUST, x, y, z, 0, 0, 0);
            }
        }
    }

    public static void update(ClientLevel level, Entity entity, float partialTicks) {
        if (level.isRaining()) {
            int density = (int) ((level.isThundering() ? ParticleRainClient.config.particleStormDensity : ParticleRainClient.config.particleDensity) * level.getRainLevel(partialTicks));

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
    public static SoundEvent getBiomeSound(Holder<Biome> biome, boolean above) {
        if (biome.value().hasPrecipitation()) {
            if (biome.value().getBaseTemperature() >= 0.15F) {
                return above ? SoundEvents.WEATHER_RAIN_ABOVE : SoundEvents.WEATHER_RAIN;
            } else {
                return above ? ParticleRainClient.WEATHER_SNOW_ABOVE : ParticleRainClient.WEATHER_SNOW;
            }
        } else if (biome.is(Biomes.DESERT) || biome.is(BiomeTags.IS_BADLANDS)) {
            return above ? ParticleRainClient.WEATHER_SANDSTORM_ABOVE : ParticleRainClient.WEATHER_SANDSTORM;
        }
        return null;
    }
}