package pigcart.particlerain;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;

import java.util.Random;

public class WeatherParticleSpawner {

    private BlockPos randomSpherePoint(int radius, BlockPos playerPos) {
        double u = Math.random();
        double v = Math.random();
        double theta = 2 * Math.PI * u;
        double phi = Math.acos(2 * v - 1);
        double x = radius * Math.sin(phi) * Math.cos(theta);
        double y = radius * Math.sin(phi) * Math.sin(theta);
        double z = radius * Math.cos(phi);
        return new BlockPos(x, y, z).add(playerPos);
    }

    int density = 0;
    public void update(World world, Entity entity) {

        if (world.isRaining()) {

            if (world.isThundering()) {density = ParticleRainClient.config.particleStormDensity;}
            else {density = ParticleRainClient.config.particleDensity;}

            Random rand = world.getRandom();

            for (int pass = 0; pass < density; pass++) {
                BlockPos pos = randomSpherePoint(ParticleRainClient.config.particleRadius, entity.getBlockPos()); //pick a random block around the player
                Biome biome = world.getBiome(pos);
                BlockPos topPos = new BlockPos(pos.getX(), world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, pos).getY(), pos.getZ());

                if (topPos.getY() < pos.getY()) {
                    if (biome.getPrecipitation() != Biome.Precipitation.NONE && world.getDimension() != world.getRegistryManager().get(Registry.DIMENSION_TYPE_KEY).get(DimensionType.THE_END_ID)) {
                        if (biome.getTemperature(topPos) >= 0.15F) {
                            if (ParticleRainClient.config.doRainParticles) {
                                world.addParticle(ParticleRainClient.RAIN_DROP,
                                        pos.getX() + rand.nextFloat(),
                                        pos.getY() + rand.nextFloat(),
                                        pos.getZ() + rand.nextFloat(),
                                        0, 0, 0);
                            }
                        } else {
                            if (ParticleRainClient.config.doSnowParticles) {
                                world.addParticle(ParticleRainClient.SNOW_FLAKE,
                                        pos.getX() + rand.nextFloat(),
                                        pos.getY() + rand.nextFloat(),
                                        pos.getZ() + rand.nextFloat(),
                                        0, 0, 0);
                            }
                        }
                    } else if (ParticleRainClient.config.doSandParticles) {
                        if (world.getBiome(pos).getCategory() == Biome.Category.DESERT) {
                            world.addParticle(ParticleRainClient.DESERT_DUST,
                                    pos.getX() + rand.nextFloat(),
                                    pos.getY() + rand.nextFloat(),
                                    pos.getZ() + rand.nextFloat(),
                                    0.9, 0.8, 0.6);
                        } else if (world.getBiome(pos).getCategory() == Biome.Category.MESA) {
                            world.addParticle(ParticleRainClient.DESERT_DUST,
                                    pos.getX() + rand.nextFloat(),
                                    pos.getY() + rand.nextFloat(),
                                    pos.getZ() + rand.nextFloat(),
                                    0.8, 0.4, 0);
                        }
                    }
                }
            }
        }
    }
}