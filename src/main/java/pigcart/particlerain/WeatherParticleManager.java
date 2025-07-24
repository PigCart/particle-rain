package pigcart.particlerain;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import pigcart.particlerain.config.ModConfig;
import pigcart.particlerain.mixin.access.ParticleEngineAccessor;
import pigcart.particlerain.particle.CustomParticle;

import java.util.List;

import static pigcart.particlerain.config.ModConfig.CONFIG;

public final class WeatherParticleManager {

    public static ParticleGroup particleGroup = new ParticleGroup(CONFIG.perf.maxParticleAmount);

    public static int fogCount;
    private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    private static final BlockPos.MutableBlockPos heightmapPos = new BlockPos.MutableBlockPos();

    public static int getParticleCount() {
        final ParticleEngineAccessor particleEngine = (ParticleEngineAccessor) Minecraft.getInstance().particleEngine;
        return particleEngine.getTrackedParticleCounts().getInt(particleGroup);
    }

    private static void spawnParticles(ClientLevel level, Holder<Biome> biome, double x, double y, double z) {
        Precipitation precipitation = StonecutterUtil.getPrecipitationAt(level, biome.value(), CONFIG.compat.useHeightmapTemp ? heightmapPos : pos);
        for (ModConfig.ParticleOptions opts : CONFIG.customParticles) {
            if (opts.enabled
                && opts.precipitation.contains(precipitation)
                && opts.density > level.random.nextFloat()
                && meetsRequirements(opts.biomeList, opts.biomeWhitelist, Registries.BIOME, biome)
                && meetsRequirements(opts.blockList, opts.blockWhitelist, Registries.BLOCK, level.getBlockState(heightmapPos).getBlockHolder())
            ) {
                if (opts.onGround) {
                    double localBlockX = x - pos.getX();
                    double localBlockZ = z - pos.getZ();
                    BlockState blockState = level.getBlockState(heightmapPos);
                    FluidState fluidState = level.getFluidState(heightmapPos);
                    VoxelShape voxelShape = blockState.getCollisionShape(level, heightmapPos);
                    double blockHeight = voxelShape.max(Direction.Axis.Y, localBlockX, localBlockZ);
                    double fluidHeight = fluidState.getHeight(level, heightmapPos);
                    y = heightmapPos.getY() + Math.max(blockHeight, fluidHeight);
                }
                //TODO
                switch (opts.id) {
                    case "rain_splashing" -> level.addParticle(ParticleTypes.RAIN, x, y, z, 0, 0, 0);
                    case "rain_ripples" -> level.addParticle(ParticleRain.RIPPLE, x, y, z, 0, 0, 0);
                    case "rain_smoke" -> level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
                    case "shrubs" -> level.addParticle(ParticleRain.SHRUB, x, y, z, 0, 0, 0);
                    default -> Minecraft.getInstance().particleEngine.add(new CustomParticle(level, x, y, z, opts));
                }
            }
        }
    }

    public static <T> boolean meetsRequirements(List<String> list, boolean isWhitelist, ResourceKey<? extends Registry<T>> registry, Holder<T> holder) {
        if (!list.isEmpty()) {
            for (String string : list) {
                ResourceLocation location = StonecutterUtil.parseResourceLocation(string);
                if (location != null) {
                    TagKey<T> tag = TagKey.create(registry, location);
                    boolean hasMatch = (holder.is(location) || holder.is(tag));
                    if (isWhitelist && hasMatch) {
                        return true;
                    } else if (hasMatch) {
                        return false;
                    }
                }
            }
            return !isWhitelist;
        }
        return true;
    }

    public static void tick(ClientLevel level, Vec3 cameraPos) {
        //TODO: twilight fog and skittering sand when not raining
        ParticleEngineAccessor particleEngine = (ParticleEngineAccessor) Minecraft.getInstance().particleEngine;
        if (level.isRaining() && particleEngine.callHasSpaceInParticleLimit(particleGroup)) {
            int density = (int) (Mth.lerpInt(level.getThunderLevel(1), CONFIG.perf.particleDensity, CONFIG.perf.particleStormDensity) * level.getRainLevel(1));
            final float speed = (float) Minecraft.getInstance().getCameraEntity().getDeltaMovement().lengthSqr();
            // mul density by speed to maintain visual density
            density *= (int) (speed * 2 + 1);

            for (int i = 0; i < density; i++) {
                float height;
                float x;
                float y;
                float z;
                if (speed < 0.8) {
                    // use a center-weighted spawn pattern if moving slowly and limit it to top half of sphere
                    height = Mth.abs(Mth.square(level.random.nextFloat()) - Mth.square(level.random.nextFloat())) * -1 + 1;
                    height *= 0.4F + 0.6F;
                } else {
                    // use the whole sphere if moving quickly (falling, flying)
                    height = level.random.nextFloat();
                }
                float theta = Mth.TWO_PI * level.random.nextFloat();
                float phi = (float) Math.acos((2 * height) - 1);
                x = CONFIG.perf.particleDistance * Mth.sin(phi) * Mth.cos(theta) + (float) cameraPos.x;
                y = CONFIG.perf.particleDistance * Mth.cos(phi)                  + (float) cameraPos.y;
                z = CONFIG.perf.particleDistance * Mth.sin(phi) * Mth.sin(theta) + (float) cameraPos.z;
                if (!CONFIG.compat.canSpawnAboveClouds) {
                    int cloudHeight = StonecutterUtil.getCloudHeight(level);
                    if (cloudHeight != 0 && y > cloudHeight) {
                        y = cloudHeight;
                    }
                }
                pos.set(x, y, z);
                heightmapPos.set(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) - 1, z);
                if (heightmapPos.getY() > pos.getY()) continue;
                spawnParticles(level, level.getBiome(pos), x, y, z);
            }
        }
    }

    //TODO
    public static SoundEvent getAdditionalWeatherSounds(ClientLevel level, BlockPos blockPos, boolean above) {
        Holder<Biome> biome = level.getBiome(blockPos);
        Precipitation precipitation = StonecutterUtil.getPrecipitationAt(level, biome.value(), blockPos);
        if (precipitation == Precipitation.SNOW && CONFIG.sound.doSnowSounds) {
            return above ? ParticleRain.WEATHER_SNOW_ABOVE : ParticleRain.WEATHER_SNOW;
        } else if (doesThisBlockHaveDustBlowing(precipitation, level, blockPos, biome) && CONFIG.sound.doWindSounds) {
            return above ? ParticleRain.WEATHER_SANDSTORM_ABOVE : ParticleRain.WEATHER_SANDSTORM;
        }
        return null;
    }

    //TODO
    public static boolean doesThisBlockHaveDustBlowing(Precipitation precipitation, ClientLevel level, BlockPos blockPos, Holder<Biome> biome) {
        boolean matchesTag = false;
        List<String> dustyBlockTags = List.of("minecraft:camel_sand_step_sound_blocks", "minecraft:sand");
        for (int i = 0; i < dustyBlockTags.size(); i++) {
            if (level.getBlockState(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).below()).is(TagKey.create(Registries.BLOCK, StonecutterUtil.parseResourceLocation(dustyBlockTags.get(i))))) {
                matchesTag = true;
                break;
            }
        }
        return precipitation == Precipitation.NONE && matchesTag && biome.value().getBaseTemperature() > 0.25;
    }

    //TODO
    public static boolean canHostStreaks(BlockState state) {
        return state.is(BlockTags.IMPERMEABLE) || state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(ParticleRain.GLASS_PANES);
    }
}