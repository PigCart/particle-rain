package pigcart.particlerain;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Random;

public class WeatherParticleSpawner {

    private BlockPos randomSpherePoint(int radius) {
        double u = Math.random();
        double v = Math.random();
        double theta = 2 * Math.PI * u;
        double phi = Math.acos(2 * v - 1);
        double x = radius * Math.sin(phi) * Math.cos(theta);
        double y = radius * Math.sin(phi) * Math.sin(theta);
        double z = radius * Math.cos(phi);
        return new BlockPos(x,y,z);
    }
    public void update(World world, Entity entity) {

        if (world.isRaining() || world.isThundering()) {

            BlockPos playerPos = entity.getBlockPos();
            Random rand = world.getRandom();

            for (int pass = 0; pass < ParticleRainClient.config.particleDensity; pass++) {
                BlockPos pos = randomSpherePoint(ParticleRainClient.config.particleRadius).add(playerPos);
                Biome biome = world.getBiome(pos);

                if (world.hasRain(pos)) {
                    world.addParticle(ParticleRainClient.RAIN_DROP,
                            pos.getX() + rand.nextFloat(),
                            pos.getY() + rand.nextFloat(),
                            pos.getZ() + rand.nextFloat(),
                            0, 0, 0);
                } else if (world.isSkyVisible(pos)) {
                    if (biome.getTemperature(pos) < 0.15F) {
                        world.addParticle(ParticleRainClient.SNOW_FLAKE,
                                pos.getX() + rand.nextFloat(),
                                pos.getY() + rand.nextFloat(),
                                pos.getZ() + rand.nextFloat(),
                                0, 0, 0);
                    } else if (world.getBiome(pos).getCategory() == Biome.Category.DESERT) {
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
