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
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import pigcart.particlerain.ParticleRain;
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
        event.registerSpriteSet(SHRUB.get(), ShrubParticle.DefaultFactory::new);
        event.registerSpriteSet(MIST.get(), MistParticle.DefaultFactory::new);
        event.registerSpriteSet(RIPPLE.get(), RippleParticle.DefaultFactory::new);
        event.registerSpriteSet(STREAK.get(), StreakParticle.DefaultFactory::new);
        ParticleRain.SHRUB = SHRUB.get();
        ParticleRain.MIST = MIST.get();
        ParticleRain.RIPPLE = RIPPLE.get();
        ParticleRain.STREAK = STREAK.get();
    }

    public NeoforgeEntrypoint(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(NeoforgeEntrypoint::onTick);
        NeoForge.EVENT_BUS.addListener(NeoforgeEntrypoint::onRegisterCommands);
        PARTICLE_TYPES.register(eventBus);
        eventBus.addListener(NeoforgeEntrypoint::onRegisterParticleProviders);
        ParticleRain.onInitializeClient();
    }
}
*///?}