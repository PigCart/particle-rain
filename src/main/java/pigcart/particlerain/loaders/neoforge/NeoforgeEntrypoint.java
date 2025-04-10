//? if neoforge {
/*package pigcart.particlerain.loaders.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import pigcart.particlerain.ParticleRainClient;
import pigcart.particlerain.particle.*;

@Mod(ParticleRainClient.MOD_ID)
public class NeoforgeEntrypoint {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, ParticleRainClient.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RAIN = registerParticle("rain");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SNOW = registerParticle("snow");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DUST_MOTE = registerParticle("dust_mote");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DUST = registerParticle("dust");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SHRUB = registerParticle("shrub");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FOG = registerParticle("fog");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GROUND_FOG = registerParticle("ground_fog");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RIPPLE = registerParticle("ripple");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> STREAK = registerParticle("streak");

    private static DeferredHolder<ParticleType<?>, SimpleParticleType> registerParticle(String name) {
        return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(true));
    }

    public static void onTick(ClientTickEvent.Post event) {
        ParticleRainClient.onTick(Minecraft.getInstance());
    }

    public static void onJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        ParticleRainClient.onJoin();
    }

    public static void onRegisterCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(ParticleRainClient.getCommands());
    }

    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(RAIN.get(), RainParticle.DefaultFactory::new);
        event.registerSpriteSet(SNOW.get(), SnowParticle.DefaultFactory::new);
        event.registerSpriteSet(DUST_MOTE.get(), DustMoteParticle.DefaultFactory::new);
        event.registerSpriteSet(DUST.get(), DustParticle.DefaultFactory::new);
        event.registerSpriteSet(SHRUB.get(), ShrubParticle.DefaultFactory::new);
        event.registerSpriteSet(FOG.get(), FogParticle.DefaultFactory::new);
        event.registerSpriteSet(GROUND_FOG.get(), GroundFogParticle.DefaultFactory::new);
        event.registerSpriteSet(RIPPLE.get(), RippleParticle.DefaultFactory::new);
        event.registerSpriteSet(STREAK.get(), StreakParticle.DefaultFactory::new);
        ParticleRainClient.RAIN = RAIN.get();
        ParticleRainClient.SNOW = SNOW.get();
        ParticleRainClient.DUST_MOTE = DUST_MOTE.get();
        ParticleRainClient.DUST = DUST.get();
        ParticleRainClient.SHRUB = SHRUB.get();
        ParticleRainClient.FOG = FOG.get();
        ParticleRainClient.GROUND_FOG = GROUND_FOG.get();
        ParticleRainClient.RIPPLE = RIPPLE.get();
        ParticleRainClient.STREAK = STREAK.get();
    }

    public NeoforgeEntrypoint(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(NeoforgeEntrypoint::onTick);
        NeoForge.EVENT_BUS.addListener(NeoforgeEntrypoint::onJoin);
        NeoForge.EVENT_BUS.addListener(NeoforgeEntrypoint::onRegisterCommands);
        PARTICLE_TYPES.register(eventBus);
        eventBus.addListener(NeoforgeEntrypoint::onRegisterParticleProviders);
        ParticleRainClient.onInitializeClient();
    }
}
*///?}