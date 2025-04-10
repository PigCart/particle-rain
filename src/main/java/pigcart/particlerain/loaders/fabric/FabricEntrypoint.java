//? if fabric {
/*package pigcart.particlerain.loaders.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import pigcart.particlerain.ParticleRainClient;
import pigcart.particlerain.particle.*;

import static pigcart.particlerain.ParticleRainClient.MOD_ID;

public class FabricEntrypoint implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ParticleRainClient.onInitializeClient();

        ParticleRainClient.RAIN = registerParticle("rain");
        ParticleRainClient.SNOW = registerParticle("snow");
        ParticleRainClient.DUST_MOTE = registerParticle("dust_mote");
        ParticleRainClient.DUST = registerParticle("dust");
        ParticleRainClient.SHRUB = registerParticle("shrub");
        ParticleRainClient.FOG = registerParticle("fog");
        ParticleRainClient.GROUND_FOG = registerParticle("ground_fog");
        ParticleRainClient.RIPPLE = registerParticle("ripple");
        ParticleRainClient.STREAK = registerParticle("streak");

        //PUDDLE = Registry.register(BuiltInRegistries.BLOCK, PUDDLE_KEY, new PuddleBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).setId(PUDDLE_KEY)));

        ParticleFactoryRegistry.getInstance().register(ParticleRainClient.RAIN, RainParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRainClient.SNOW, SnowParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRainClient.DUST_MOTE, DustMoteParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRainClient.DUST, DustParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRainClient.SHRUB, ShrubParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRainClient.FOG, FogParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRainClient.GROUND_FOG, GroundFogParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRainClient.RIPPLE, RippleParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRainClient.STREAK, StreakParticle.DefaultFactory::new);

        ClientTickEvents.END_CLIENT_TICK.register(ParticleRainClient::onTick);
        ClientPlayConnectionEvents.JOIN.register((packetListener, packetSender, minecraft) -> ParticleRainClient.onJoin());
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ParticleRainClient.getCommands());
        });
    }
    private SimpleParticleType registerParticle(String name) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, name), FabricParticleTypes.simple(true));
    }
}
*///?}