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
import pigcart.particlerain.StonecutterUtil;
import pigcart.particlerain.config.ModConfig;
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

        ParticleRain.onInitializeClient();

        for (ModConfig.ParticleOptions opts : ModConfig.CONFIG.customParticles) {
            try {
                SimpleParticleType particle = registerParticle(opts.id.toLowerCase().replace(" ", "_"));
                ParticleFactoryRegistry.getInstance().register(particle, new CustomParticle.DefaultFactory(opts));
            } catch (ResourceLocationException | IllegalStateException e) {
                ModConfig.CONFIG.customParticles = ModConfig.DEFAULT.customParticles;
                ParticleRain.LOGGER.error(e.getMessage());
            }
        }

        ParticleFactoryRegistry.getInstance().register(ParticleRain.SHRUB, ShrubParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRain.MIST, MistParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRain.RIPPLE, RippleParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRain.STREAK, StreakParticle.DefaultFactory::new);

        ClientTickEvents.END_CLIENT_TICK.register(ParticleRain::onTick);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ParticleRain.getCommands());
        });
    }
    private SimpleParticleType registerParticle(String name) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, StonecutterUtil.getResourceLocation(MOD_ID, name), FabricParticleTypes.simple(true));
    }
}
//?}