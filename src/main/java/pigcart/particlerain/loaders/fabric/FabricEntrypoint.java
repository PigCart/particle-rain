//? if fabric {
package pigcart.particlerain.loaders.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.VersionUtil;
import pigcart.particlerain.config.ConfigData;
import pigcart.particlerain.config.ConfigManager;
import pigcart.particlerain.particle.*;

import static pigcart.particlerain.ParticleRain.MOD_ID;

public class FabricEntrypoint implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        //TODO
        ParticleRain.SHRUB = registerParticle("shrub");
        ParticleRain.MIST = registerParticle("mist");
        ParticleRain.RIPPLE = registerParticle("ripple");
        ParticleRain.STREAK = registerParticle("streak");
        ParticleFactoryRegistry.getInstance().register(ParticleRain.SHRUB, ShrubParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRain.MIST, MistParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRain.RIPPLE, RippleParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRain.STREAK, StreakParticle.DefaultFactory::new);

        ParticleRain.onInitializeClient();

        for (ConfigData.ParticleData data : ConfigManager.config.particles) {
            if (!data.usePresetParticle) {
                String particleId = data.id.toLowerCase().replace(" ", "_");
                if (BuiltInRegistries.PARTICLE_TYPE.containsKey(VersionUtil.getId(particleId))) {
                    ParticleRain.LOGGER.warn("{} is already registered! please choose a different id for this particle", particleId);
                } else {
                    try {
                        SimpleParticleType particle = registerParticle(particleId);
                        ParticleFactoryRegistry.getInstance().register(particle, new CustomParticle.DefaultFactory(data));
                    } catch (ResourceLocationException | IllegalStateException e) {
                        ConfigManager.config.particles = ConfigManager.defaultConfig.particles;
                        ParticleRain.LOGGER.error(e.getMessage());
                    }
                }
            }
        }
        ConfigManager.updateTransientVariables();


        ClientTickEvents.END_CLIENT_TICK.register(ParticleRain::onTick);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ParticleRain.getCommands());
        });
    }
    private SimpleParticleType registerParticle(String name) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, VersionUtil.getId(name), FabricParticleTypes.simple(true));
    }
}
//?}