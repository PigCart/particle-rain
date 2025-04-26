//? if fabric {
package pigcart.particlerain.loaders.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import pigcart.particlerain.ParticleRainClient;
import pigcart.particlerain.StonecutterUtil;
import pigcart.particlerain.particle.*;

import static pigcart.particlerain.ParticleRainClient.MOD_ID;

public class FabricEntrypoint implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ParticleRainClient.onInitializeClient();

        ParticleRainClient.RAIN = registerParticle("rain");
        ParticleRainClient.SNOW = registerParticle("snow");
        ParticleRainClient.DUST = registerParticle("dust");
        ParticleRainClient.SHRUB = registerParticle("shrub");
        ParticleRainClient.FOG = registerParticle("fog");
        ParticleRainClient.MIST = registerParticle("mist");
        ParticleRainClient.RIPPLE = registerParticle("ripple");
        ParticleRainClient.STREAK = registerParticle("streak");

        ParticleFactoryRegistry.getInstance().register(ParticleRainClient.RAIN, RainParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRainClient.SNOW, SnowParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRainClient.DUST, DustParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRainClient.SHRUB, ShrubParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRainClient.FOG, FogParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRainClient.MIST, MistParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRainClient.RIPPLE, RippleParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRainClient.STREAK, StreakParticle.DefaultFactory::new);

        ClientTickEvents.END_CLIENT_TICK.register(ParticleRainClient::onTick);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ParticleRainClient.getCommands());
        });
    }
    private SimpleParticleType registerParticle(String name) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, StonecutterUtil.getResourceLocation(MOD_ID, name), FabricParticleTypes.simple(true));
    }
}
//?}