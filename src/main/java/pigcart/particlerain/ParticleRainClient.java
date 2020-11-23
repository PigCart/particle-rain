package pigcart.particlerain;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.registry.Registry;
import pigcart.particlerain.particle.DesertDustParticle;
import pigcart.particlerain.particle.RainDropParticle;
import pigcart.particlerain.particle.SnowFlakeParticle;

public class ParticleRainClient implements ClientModInitializer {

    public static DefaultParticleType RAIN_DROP;
    public static DefaultParticleType SNOW_FLAKE;
    public static DefaultParticleType DESERT_DUST;
    public static final WeatherParticleSpawner particleSpawner = new WeatherParticleSpawner();

    @Override
    public void onInitializeClient() {
        RAIN_DROP = Registry.register(Registry.PARTICLE_TYPE, ParticleRain.id("rain_drop"), FabricParticleTypes.simple(true));
        ParticleFactoryRegistry.getInstance().register(RAIN_DROP, RainDropParticle.DefaultFactory::new);

        SNOW_FLAKE = Registry.register(Registry.PARTICLE_TYPE, ParticleRain.id("snow_flake"), FabricParticleTypes.simple(true));
        ParticleFactoryRegistry.getInstance().register(SNOW_FLAKE, SnowFlakeParticle.DefaultFactory::new);

        DESERT_DUST = Registry.register(Registry.PARTICLE_TYPE, ParticleRain.id("desert_dust"), FabricParticleTypes.simple(true));
        ParticleFactoryRegistry.getInstance().register(DESERT_DUST, DesertDustParticle.DefaultFactory::new);

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        if (!client.isPaused() && client.world != null && client.getCameraEntity() != null) {
            particleSpawner.update(client.world, client.getCameraEntity());
        }
    }
}
