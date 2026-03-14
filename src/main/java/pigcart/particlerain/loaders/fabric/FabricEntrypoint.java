//? if fabric {
package pigcart.particlerain.loaders.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//~ if >=26.1 'ParticleFactoryRegistry' -> 'ParticleProviderRegistry' {
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
//~}
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.VersionUtil;
import pigcart.particlerain.ParticleLoader;
import pigcart.particlerain.particle.*;

public class FabricEntrypoint implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        //TODO
        ParticleRain.SHRUB = registerParticle("shrub");
        ParticleRain.MIST = registerParticle("mist");
        ParticleRain.RIPPLE = registerParticle("ripple");
        ParticleRain.STREAK = registerParticle("streak");
        //~ if >=26.1 'ParticleFactoryRegistry' -> 'ParticleProviderRegistry' {
        ParticleFactoryRegistry.getInstance().register(ParticleRain.SHRUB, ShrubParticle.Provider::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRain.MIST, MistParticle.Provider::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRain.RIPPLE, RippleParticle.Provider::new);
        ParticleFactoryRegistry.getInstance().register(ParticleRain.STREAK, StreakParticle.Provider::new);
        //~}

        ParticleRain.onInitializeClient();

        ClientTickEvents.END_CLIENT_TICK.register(ParticleRain::onTick);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ParticleRain.getCommands());
        });
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                ParticleLoader.onResourceManagerReload(resourceManager);
            }

            @Override
            public ResourceLocation getFabricId() {
                return VersionUtil.getId("reload");
            }
        });
    }
    private SimpleParticleType registerParticle(String name) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, VersionUtil.getId(name), FabricParticleTypes.simple(true));
    }
}
//?}