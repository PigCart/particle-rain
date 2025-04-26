package pigcart.particlerain;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
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
        final BlockPos getPrecipitationFromBlockPos = CONFIG.spawn.useHeightmapTemp ? level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos) : pos ;
        Precipitation precipitation = CONFIG.spawn.doOverrideWeather ? CONFIG.spawn.overrideWeather : StonecutterUtil.getPrecipitationAt(level, biome.value(), getPrecipitationFromBlockPos);
        //biome.value().hasPrecipitation() isn't reliable for modded biomes and seasons
        if (precipitation == Precipitation.RAIN) {
            if (CONFIG.effect.doMistParticles && fogCount < CONFIG.mist.density) {
                int height = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z);
                int distance = (int) new Vec3(x, height, z).distanceToSqr(Minecraft.getInstance().cameraEntity.position());
                if (distance > CONFIG.perf.particleDistance - 1 && height <= CONFIG.mist.maxSpawnHeight && height >= CONFIG.mist.minSpawnHeight && level.getFluidState(BlockPos.containing(x, height - 1, z)).isEmpty()) {
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
        } else if (doesThisBlockHaveDustBlowing(precipitation, level, BlockPos.containing(x, y, z), biome)) {
            if (CONFIG.dust.spawnOnGround) y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z);
            if (CONFIG.effect.doDustParticles) {
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

    public static void tick(ClientLevel level, Entity entity) {
        //TODO: twilight fog and skittering sand when not raining
        if (level.isRaining()) {
            int density = (int) ((level.isThundering() ? CONFIG.perf.particleStormDensity : CONFIG.perf.particleDensity) * level.getRainLevel(0));
            final float speed = (float) Minecraft.getInstance().getCameraEntity().getDeltaMovement().length();
            density = (int) (density * ((speed * 2) + 1));

            RandomSource rand = RandomSource.create();

            for (int pass = 0; pass < density; pass++) {

                float theta = (float) (2 * Math.PI * rand.nextFloat());
                float phi = (float) Math.acos(2 * rand.nextFloat() - 1);
                double x = CONFIG.perf.particleDistance * Mth.sin(phi) * Math.cos(theta);
                double y = CONFIG.perf.particleDistance * Mth.sin(phi) * Math.sin(theta);
                double z = CONFIG.perf.particleDistance * Mth.cos(phi);

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
        //TODO: this should be a more precise mixin instead of a whole reimplementation
        ClientLevel level = Minecraft.getInstance().level;
        Holder<Biome> biome = level.getBiome(blockPos);
        Precipitation precipitation = CONFIG.spawn.doOverrideWeather ? CONFIG.spawn.overrideWeather : StonecutterUtil.getPrecipitationAt(level, biome.value(), blockPos);
        if (precipitation == Precipitation.RAIN && CONFIG.sound.doRainSounds) {
            return above ? SoundEvents.WEATHER_RAIN_ABOVE : SoundEvents.WEATHER_RAIN;
        } else if (precipitation == Precipitation.SNOW && CONFIG.sound.doSnowSounds) {
            return above ? ParticleRainClient.WEATHER_SNOW_ABOVE : ParticleRainClient.WEATHER_SNOW;
        } else if (doesThisBlockHaveDustBlowing(precipitation, level, blockPos, biome) && CONFIG.sound.doSandSounds) {
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