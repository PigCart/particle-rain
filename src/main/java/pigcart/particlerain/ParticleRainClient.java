package pigcart.particlerain;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import pigcart.particlerain.particle.*;

public class ParticleRainClient implements ClientModInitializer {

    public static final String MOD_ID = "particlerain";

    public static SimpleParticleType RAIN_DROP;
    public static SimpleParticleType RAIN_SHEET;
    public static SimpleParticleType SNOW_FLAKE;
    public static SimpleParticleType SNOW_SHEET;
    public static SimpleParticleType DUST_MOTE;
    public static SimpleParticleType DUST_SHEET;
    public static SimpleParticleType FOG;

    public static SoundEvent WEATHER_SNOW;
    public static SoundEvent WEATHER_SNOW_ABOVE;
    public static SoundEvent WEATHER_SANDSTORM;
    public static SoundEvent WEATHER_SANDSTORM_ABOVE;

    public static ModConfig config;

    @Override
    public void onInitializeClient() {
        RAIN_DROP = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "rain_drop"), FabricParticleTypes.simple(true));
        RAIN_SHEET = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "rain_sheet"), FabricParticleTypes.simple(true));
        SNOW_FLAKE = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "snow_flake"), FabricParticleTypes.simple(true));
        SNOW_SHEET = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "snow_sheet"), FabricParticleTypes.simple(true));
        DUST_MOTE = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "dust_mote"), FabricParticleTypes.simple(true));
        DUST_SHEET = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "dust_sheet"), FabricParticleTypes.simple(true));
        FOG = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "fog"), FabricParticleTypes.simple(true));

        WEATHER_SNOW = createSoundEvent("weather.snow");
        WEATHER_SNOW_ABOVE = createSoundEvent("weather.snow.above");
        WEATHER_SANDSTORM = createSoundEvent("weather.sandstorm");
        WEATHER_SANDSTORM_ABOVE = createSoundEvent("weather.sandstorm.above");

        ParticleFactoryRegistry.getInstance().register(RAIN_DROP, RainDropParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(RAIN_SHEET, RainSheetParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(SNOW_FLAKE, SnowFlakeParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(SNOW_SHEET, SnowSheetParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(DUST_MOTE, DustMoteParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(DUST_SHEET, DustSheetParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(FOG, FogParticle.DefaultFactory::new);

        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);

    }

    private void onTick(Minecraft client) {
        if (!client.isPaused() && client.level != null && client.getCameraEntity() != null)
            WeatherParticleSpawner.update(client.level, client.getCameraEntity(), client.getFrameTimeNs());
    }

    private static SoundEvent createSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
        return SoundEvent.createVariableRangeEvent(id);
    }
}