package pigcart.particlerain;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pigcart.particlerain.config.ConfigData;
import pigcart.particlerain.config.ConfigData.SpawnPos;
import pigcart.particlerain.mixin.access.ParticleEngineAccessor;
import pigcart.particlerain.particle.CustomParticle;
import pigcart.particlerain.particle.StreakParticle;
//? if >=1.21.9 {
/*import net.minecraft.core.particles.ParticleLimit;
*///?} else {
import net.minecraft.core.particles.ParticleGroup;
//?}

import static pigcart.particlerain.config.ConfigManager.config;

public final class WeatherParticleManager {
    private static final RandomSource random = RandomSource.create();
    //? if >=1.21.9 {
    /*public static ParticleLimit particleGroup = new ParticleLimit(config.perf.maxParticleAmount);
    *///?} else {
    public static ParticleGroup particleGroup = new ParticleGroup(config.perf.maxParticleAmount);
    //?}
    private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    private static final BlockPos.MutableBlockPos heightmapPos = new BlockPos.MutableBlockPos();
    public static int afterWeatherTicksLeft = 0;
    public static int spawnAttemptsUntilBlockFXIdle = 0;
    public static int ticksUntilSurfaceFXIdle = 0;
    public static int ticksUntilSkyFXIdle = 0;

    public static int getParticleCount() {
        final ParticleEngineAccessor particleEngine = (ParticleEngineAccessor) Minecraft.getInstance().particleEngine;
        return particleEngine.getTrackedParticleCounts().getInt(particleGroup);
    }

    public static void tick(ClientLevel level, Vec3 cameraPos) {
        ParticleEngineAccessor particleEngine = (ParticleEngineAccessor) Minecraft.getInstance().particleEngine;
        if (!particleEngine.callHasSpaceInParticleLimit(particleGroup)) return;
        tickSkyFX(level, cameraPos);
        tickSurfaceFX(level, cameraPos);
        if (afterWeatherTicksLeft > 0) afterWeatherTicksLeft--;
    }

    public static void onWeatherChange(boolean isRaining) {
        afterWeatherTicksLeft = isRaining ? 0 : random.nextInt(6000); // 'after weather' period lasts up to 5 minutes
    }

    public static void tickBlockFX(BlockPos.MutableBlockPos sourcePos, BlockState state) {
        ClientLevel level = Minecraft.getInstance().level;
        if (spawnAttemptsUntilBlockFXIdle <= 0 && random.nextFloat() < 0.9F) {
            return;
        }
        spawnAttemptsUntilBlockFXIdle--;
        if (!state.getCollisionShape(level, sourcePos).isEmpty()) return;
        for (ConfigData.ParticleData opts : config.particles) {
            if (!opts.enabled || !opts.weather.isCurrent(level)) continue;
            final Holder<Biome> biome = level.getBiome(sourcePos);
            final Direction direction = switch (opts.spawnPos) {
                case BLOCK_SIDES -> Direction.Plane.HORIZONTAL.getRandomDirection(random);
                case BLOCK_BOTTOM -> Direction.DOWN;
                case BLOCK_TOP -> Direction.UP;
                default -> null;
            };
            if (direction == null) continue;
            Direction opposite = direction.getOpposite();
            pos.set(sourcePos.getX() + opposite.getStepX(), sourcePos.getY() + opposite.getStepY(), sourcePos.getZ() + opposite.getStepZ());
            final BlockState blockState = level.getBlockState(pos);
            final FluidState fluidState = blockState.getFluidState();
            if (blockState.getCollisionShape(level, pos).isEmpty() && fluidState.isEmpty()) continue;
            if ((opts.spawnPos == SpawnPos.BLOCK_BOTTOM || opts.spawnPos == SpawnPos.BLOCK_SIDES || opts.spawnPos == SpawnPos.BLOCK_TOP)
                    && opts.precipitation.contains(VersionUtil.getPrecipitationAt(level, biome, sourcePos))
                    && opts.density > random.nextFloat()
                    && opts.biomeList.contains(biome)
                    && opts.blockList.contains(level.getBlockState(pos).getBlockHolder())
            ) {
                if (opts.needsSkyAccess && sourcePos.getY() < level.getHeight(Heightmap.Types.MOTION_BLOCKING, sourcePos.getX(), sourcePos.getZ())) continue;
                // get position on block face
                float p1 = random.nextFloat();
                float p2 = random.nextFloat();
                Vector3f relativePos;
                if (direction.getAxisDirection().equals(Direction.AxisDirection.POSITIVE)) {
                    double max = blockState.getCollisionShape(level, pos).max(direction.getAxis(), p1, p2);
                    if (direction == Direction.UP) {
                        max = Math.max(max, fluidState.getHeight(level, pos));
                    }
                    if (max == Double.NEGATIVE_INFINITY) continue;
                    max += 0.01; // avoid z-fighting
                    relativePos = new Vector3f(p2 - 0.5F, (float) max - 0.5F, p1 - 0.5F);
                } else {
                    double min = blockState.getCollisionShape(level, pos).min(direction.getAxis(), p1, p2);
                    if (min == Double.POSITIVE_INFINITY) continue;
                    min -= 0.01;
                    relativePos = new Vector3f(p2 - 0.5F, (float) min - 0.5F, p1 - 0.5F);
                }
                relativePos.rotate(switch (direction) {
                    case UP, DOWN -> new Quaternionf();
                    case SOUTH, NORTH -> new Quaternionf().rotationXYZ(Mth.HALF_PI, Mth.HALF_PI, 0);
                    case EAST, WEST -> new Quaternionf().rotationXYZ(-Mth.HALF_PI, 0, -Mth.HALF_PI);
                });
                float x = pos.getX() + relativePos.x + 0.5F;
                float y = pos.getY() + relativePos.y + 0.5F;
                float z = pos.getZ() + relativePos.z + 0.5F;
                if (opts.usePresetParticle) {
                    if (opts.presetParticleId.equals("particlerain:streak")) {
                        // edge cases upon edge cases upon edge cases upon
                        Minecraft.getInstance().particleEngine.add(new StreakParticle(level, x, y, z, direction, opts.blockList));
                    } else {
                        level.addParticle(opts.presetParticle, x, y, z, 0, 0, 0);
                    }
                } else {
                    Minecraft.getInstance().particleEngine.add(new CustomParticle(level, x, y, z, opts));
                }
                spawnAttemptsUntilBlockFXIdle = 10000;
            }

        }
    }
    public static void tickSkyFX(ClientLevel level, Vec3 cameraPos) {
        //TODO: twilight fog and skittering sand when not raining
        int density;
        float speed;
        if (ticksUntilSkyFXIdle <= 0) {
            density = 4;
            speed = 0;
        } else {
            ticksUntilSkyFXIdle--;
            density = (int) (Mth.lerpInt(level.getThunderLevel(1), config.perf.particleDensity, config.perf.particleStormDensity) * level.getRainLevel(1));
            speed = (float) Minecraft.getInstance().getCameraEntity().getDeltaMovement().length();
            // mul density by speed to maintain visual density
            density = (int) (density * (1 + speed));
        }
        for (int i = 0; i < density; i++) {
            float height;
            float x;
            float y;
            float z;
            if (speed < 0.8) {
                // use a center-weighted spawn pattern if moving slowly and limit it to top half of sphere
                height = Mth.abs(Mth.square(random.nextFloat()) - Mth.square(random.nextFloat())) * -1 + 1;
                height *= 0.4F + 0.6F;
            } else {
                // use the whole sphere if moving quickly (falling, flying)
                height = random.nextFloat();
            }
            float theta = Mth.TWO_PI * random.nextFloat();
            float phi = (float) Math.acos((2 * height) - 1);
            x = config.perf.particleDistance * Mth.sin(phi) * Mth.cos(theta) + (float) cameraPos.x;
            y = config.perf.particleDistance * Mth.cos(phi)                  + (float) cameraPos.y;
            z = config.perf.particleDistance * Mth.sin(phi) * Mth.sin(theta) + (float) cameraPos.z;
            if (config.compat.doSpawnHeightLimit) {
                int cloudHeight = config.compat.spawnHeightLimit == 0 ? VersionUtil.getCloudHeight(level) : config.compat.spawnHeightLimit;
                if (cloudHeight != 0 && y > cloudHeight) {
                    y = cloudHeight;
                }
            }
            pos.set(x, y, z);
            int heightmapY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
            heightmapPos.set(x, heightmapY - 1, z);
            if (heightmapY > pos.getY()) continue;
            Holder<Biome> biome = level.getBiome(pos);
            Precipitation precipitation = VersionUtil.getPrecipitationAt(level, biome, config.compat.useHeightmapTemp ? heightmapPos : pos);
            for (ConfigData.ParticleData data : config.particles) {
                if (data.enabled
                    && data.spawnPos.equals(SpawnPos.SKY)
                    && data.weather.isCurrent(level)
                    && data.precipitation.contains(precipitation)
                    && data.density > random.nextFloat()
                    && data.biomeList.contains(biome)
                    && data.blockList.contains(level.getBlockState(heightmapPos).getBlockHolder())
                ) {
                    if (data.usePresetParticle) {
                        level.addParticle(data.presetParticle, x, y, z, 0, 0, 0);
                    } else {
                        Minecraft.getInstance().particleEngine.add(new CustomParticle(level, x, y, z, data));
                    }
                    ticksUntilSkyFXIdle = 100;
                }
            }
        }
    }

    public static void tickSurfaceFX(ClientLevel level, Vec3 cameraPos) {
        int density;
        if (ticksUntilSurfaceFXIdle <= 0) {
            density = 1;
        } else {
            density = config.perf.particleDensity;
            ticksUntilSurfaceFXIdle--;
        }
        for (int i = 0; i < density; i++) {
            double x = random.triangle(cameraPos.x, config.perf.surfaceRange);
            double z = random.triangle(cameraPos.z, config.perf.surfaceRange);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z);
            pos.set(x, y - 1, z);
            BlockState blockState = level.getBlockState(pos);
            Holder<Biome> biome = level.getBiome(pos);
            Biome.Precipitation precipitation = VersionUtil.getPrecipitationAt(level, biome, pos);
            for (ConfigData.ParticleData data : config.particles) {
                if (data.enabled
                        && data.spawnPos.equals(SpawnPos.WORLD_SURFACE)
                        && data.weather.isCurrent(level)
                        && data.precipitation.contains(precipitation)
                        && data.density > random.nextFloat()
                        && data.biomeList.contains(biome)
                        && data.blockList.contains(blockState.getBlockHolder())
                ) {
                    if (data.usePresetParticle) {
                        level.addParticle(data.presetParticle, x, y, z, 0, 0, 0);
                    } else {
                        Minecraft.getInstance().particleEngine.add(new CustomParticle(level, x, y, z, data));
                    }
                    ticksUntilSurfaceFXIdle = 100;
                }
            }
        }
    }
}