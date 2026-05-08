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
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import pigcart.particlerain.config.ParticleData;
import pigcart.particlerain.particle.CustomParticle;
import pigcart.particlerain.particle.StreakParticle;

import static pigcart.particlerain.config.ConfigManager.config;

public final class ParticleSpawner {
    private static final RandomSource RANDOM = RandomSource.create();
    private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    private static final BlockPos.MutableBlockPos heightmapPos = new BlockPos.MutableBlockPos();
    public static int afterWeatherTicksLeft = 0;
    public static int spawnAttemptsUntilBlockFXIdle = 0;
    public static int ticksUntilSurfaceFXIdle = 0;
    public static int ticksUntilSkyFXIdle = 0;
    public static int particleCount = 0;

    public static void tick(ClientLevel level, Vec3 cameraPos) {
        if (particleCount >= config.perf.maxParticleAmount) return;
        tickSkyFX(level, cameraPos);
        tickSurfaceFX(level, cameraPos);
        if (afterWeatherTicksLeft > 0) afterWeatherTicksLeft--;
    }

    //TODO: broken in 26.1
    public static void onWeatherChange(boolean isRaining) {
        afterWeatherTicksLeft = isRaining ? 0 : RandomSource.create().nextInt(6000); // 'after weather' period lasts up to 5 minutes
    }

    public static boolean isIgnored(BlockState state) {
        return config.compat.weatherIgnoreBlocks.contains(state.getBlockHolder());
    }

    public static int calculateHeight(ClientLevel level, int x, int z) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        if(y == 0 || y == -1) y=255; //Some servers (like wynncraft & hypixel) send a map of 0 or -1 for MOTION_BLOCKING;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(x, y, z);

        //? if >=1.21.9 {
        /*int minY = level.getMinY();
        *///?} else {
        int minY = -64;
        //?}

        while (y > minY) {
            BlockState state = level.getBlockState(mutablePos);

            boolean noCollision = state.getCollisionShape(level, pos).isEmpty();
            boolean nonFluid = state.getFluidState().isEmpty();

            //TODO: why is it one block lower than vanilla
            if (nonFluid && (noCollision || isIgnored(state))) {
                y--;
                mutablePos.setY(y);
            } else {
                break;
            }
        }
        return y + 1;
    }

    private static final Long2IntMap heightCache = new Long2IntOpenHashMap();
    private static int lastTick = 0;

    public static int getHeight(ClientLevel level, int x, int z) {
        if (config.compat.weatherIgnoreBlocks.getEntries().isEmpty()) {
            return level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        }

        if (level.getGameTime() != lastTick) {
            if (level.getGameTime() % 80 == 0) heightCache.clear();
            lastTick = (int) level.getGameTime();
        }
        long key = ((long) x << 32) | (z & 0xFFFFFFFFL);

        if (heightCache.containsKey(key)) {
            return heightCache.get(key);
        }

        int customY = calculateHeight(level, x, z);
        heightCache.put(key, customY);
        return customY;
    }

    public static void tickBlockFX(BlockPos.MutableBlockPos sourcePos, BlockState state, RandomSource random) {
        ClientLevel level = Minecraft.getInstance().level;
        if (spawnAttemptsUntilBlockFXIdle <= 0 && level.getRandom().nextFloat() < 0.9F) {
            return;
        }
        spawnAttemptsUntilBlockFXIdle--;
        if ( !state.getCollisionShape(level, sourcePos).isEmpty() && !isIgnored(state) ) return;
        for (ParticleData opts : ParticleLoader.particles.values()) {
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
            final VoxelShape collision = blockState.getCollisionShape(level, pos);
            if ((collision.isEmpty() && fluidState.isEmpty()) || isIgnored(state) ) continue;
            if ((opts.spawnPos == ParticleData.SpawnPos.BLOCK_BOTTOM || opts.spawnPos == ParticleData.SpawnPos.BLOCK_SIDES || opts.spawnPos == ParticleData.SpawnPos.BLOCK_TOP)
                    && opts.precipitation.contains(VersionUtil.getPrecipitationAt(level, biome, sourcePos))
                    && opts.density > random.nextFloat()
                    && opts.biomeList.contains(biome)
                    && opts.blockList.contains(level.getBlockState(pos).getBlockHolder())
            ) {
                if (opts.needsSkyAccess && sourcePos.getY() < getHeight(level, sourcePos.getX(), sourcePos.getZ())    ) continue;
                // get position on block face
                float p1 = random.nextFloat();
                float p2 = random.nextFloat();
                Vector3f relativePos;
                if (direction.getAxisDirection().equals(Direction.AxisDirection.POSITIVE)) {
                    double max = collision.max(direction.getAxis(), p1, p2);
                    if (direction == Direction.UP) {
                        max = Math.max(max, fluidState.getHeight(level, pos));
                    }
                    if (max == Double.NEGATIVE_INFINITY) continue;
                    max += 0.01; // avoid z-fighting
                    relativePos = new Vector3f(p2 - 0.5F, (float) max - 0.5F, p1 - 0.5F);
                } else {
                    double min = collision.min(direction.getAxis(), p1, p2);
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
                height = Mth.abs(Mth.square(RANDOM.nextFloat()) - Mth.square(RANDOM.nextFloat())) * -1 + 1;
                height *= 0.4F + 0.6F;
            } else {
                // use the whole sphere if moving quickly (falling, flying)
                height = RANDOM.nextFloat();
            }
            float theta = Mth.TWO_PI * RANDOM.nextFloat();
            float phi = (float) Math.acos((2 * height) - 1);
            x = config.perf.particleDistance * Mth.sin(phi) * Mth.cos(theta) + (float) cameraPos.x;
            y = config.perf.particleDistance * Mth.cos(phi)                  + (float) cameraPos.y;
            z = config.perf.particleDistance * Mth.sin(phi) * Mth.sin(theta) + (float) cameraPos.z;
            pos.set(x, y, z);
            if (config.compat.doSpawnHeightLimit) {
                int cloudHeight = config.compat.spawnHeightLimit == 0 ? VersionUtil.getCloudHeight(level, pos) : config.compat.spawnHeightLimit;
                if (cloudHeight != 0 && y > cloudHeight) {
                    y = cloudHeight;
                    pos.setY(cloudHeight);
                }
            }
            int heightmapY = getHeight(level, pos.getX(), pos.getZ());
            heightmapPos.set(x, heightmapY - 1, z);
            if (heightmapY >= pos.getY()) continue;
            Holder<Biome> biome = level.getBiome(pos);
            Precipitation precipitation = VersionUtil.getPrecipitationAt(level, biome, config.compat.useHeightmapTemp ? heightmapPos : pos);
            for (ParticleData data : ParticleLoader.particles.values()) {
                if (data.enabled
                    && data.spawnPos.equals(ParticleData.SpawnPos.SKY)
                    && data.weather.isCurrent(level)
                    && data.precipitation.contains(precipitation)
                    && data.density > RANDOM.nextFloat()
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
            double x = RANDOM.triangle(cameraPos.x, config.perf.surfaceRange);
            double z = RANDOM.triangle(cameraPos.z, config.perf.surfaceRange);
            int y = getHeight(level, (int) x, (int) z);
            pos.set(x, y - 1, z);
            BlockState blockState = level.getBlockState(pos);
            Holder<Biome> biome = level.getBiome(pos);
            Biome.Precipitation precipitation = VersionUtil.getPrecipitationAt(level, biome, pos);
            for (ParticleData data : ParticleLoader.particles.values()) {
                if (data.enabled
                        && data.spawnPos.equals(ParticleData.SpawnPos.WORLD_SURFACE)
                        && data.weather.isCurrent(level)
                        && data.precipitation.contains(precipitation)
                        && data.density > RANDOM.nextFloat()
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