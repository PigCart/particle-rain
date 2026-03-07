//? if neoforge {
/*package pigcart.particlerain.loaders.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import pigcart.particlerain.ParticleLoader;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.VersionUtil;
import pigcart.particlerain.config.ConfigManager;
import pigcart.particlerain.particle.*;

@Mod(ParticleRain.MOD_ID)
public class NeoforgeEntrypoint {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, ParticleRain.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SHRUB = registerParticle("shrub");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MIST = registerParticle("mist");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RIPPLE = registerParticle("ripple");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> STREAK = registerParticle("streak");

    private static DeferredHolder<ParticleType<?>, SimpleParticleType> registerParticle(String name) {
        return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(true));
    }

    public static void onTick(ClientTickEvent.Post event) {
        ParticleRain.onTick(Minecraft.getInstance());
    }

    public static void onRegisterCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(ParticleRain.getCommands());
    }

    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        //TODO
        event.registerSpriteSet(SHRUB.get(), ShrubParticle.DefaultFactory::new);
        event.registerSpriteSet(MIST.get(), MistParticle.DefaultFactory::new);
        event.registerSpriteSet(RIPPLE.get(), RippleParticle.DefaultFactory::new);
        event.registerSpriteSet(STREAK.get(), StreakParticle.DefaultFactory::new);
        ParticleRain.SHRUB = SHRUB.get();
        ParticleRain.MIST = MIST.get();
        ParticleRain.RIPPLE = RIPPLE.get();
        ParticleRain.STREAK = STREAK.get();
    }

    public static void onRegisterReloadListeners(
            //? >=1.21.4 {
            /^net.neoforged.neoforge.client.event.AddClientReloadListenersEvent event
            ^///?} else {
            net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent event
            //?}
    ) {
        //? >=1.21.4 {
        /^event.addListener(VersionUtil.getId("reload"),
        ^///?} else {
        event.registerReloadListener(
        //?}
            new SimplePreparableReloadListener<>() {
                @Override
                protected Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
                    ParticleLoader.onResourceManagerReload(resourceManager); //yeah whatever
                    return null;
                }

                @Override
                protected void apply(Object o, ResourceManager resourceManager, ProfilerFiller profilerFiller) {}
        });
    }

    public NeoforgeEntrypoint(IEventBus eventBus) {
        if (FMLEnvironment.dist.isDedicatedServer()) return;

        NeoForge.EVENT_BUS.addListener(NeoforgeEntrypoint::onTick);
        NeoForge.EVENT_BUS.addListener(NeoforgeEntrypoint::onRegisterCommands);
        PARTICLE_TYPES.register(eventBus);
        eventBus.addListener(NeoforgeEntrypoint::onRegisterParticleProviders);
        eventBus.addListener(NeoforgeEntrypoint::onRegisterReloadListeners);
        ModLoadingContext.get().registerExtensionPoint(
                IConfigScreenFactory.class,
                () -> (modContainer, parent) -> ConfigManager.screenPlease(parent)
        );
        ParticleRain.onInitializeClient();
    }
}
*///?}