//? if forge {
/*package pigcart.particlerain.loaders.forge;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.config.ConfigScreens;
import pigcart.particlerain.particle.*;

@Mod(ParticleRain.MOD_ID)
public class ForgeEntrypoint {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, ParticleRain.MOD_ID);

    public static final RegistryObject<SimpleParticleType> SHRUB = registerParticle("shrub");
    public static final RegistryObject<SimpleParticleType> MIST = registerParticle("mist");
    public static final RegistryObject<SimpleParticleType> RIPPLE = registerParticle("ripple");
    public static final RegistryObject<SimpleParticleType> STREAK = registerParticle("streak");

    private static RegistryObject<SimpleParticleType> registerParticle(String name) {
        return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(true));
    }

    public static void onTick(TickEvent.ClientTickEvent event) {
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

    @SuppressWarnings("removal")
    public ForgeEntrypoint() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.addListener(ForgeEntrypoint::onTick);
        MinecraftForge.EVENT_BUS.addListener(ForgeEntrypoint::onRegisterCommands);
        PARTICLE_TYPES.register(eventBus);
        eventBus.addListener(ForgeEntrypoint::onRegisterParticleProviders);
        ParticleRain.onInitializeClient();
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (client, parent) -> ConfigScreens.generateMainConfigScreen(parent)
                )
        );
    }
}
*///?}