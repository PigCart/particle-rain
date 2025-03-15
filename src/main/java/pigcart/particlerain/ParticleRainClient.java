package pigcart.particlerain;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.biome.Biome;
import pigcart.particlerain.config.ModConfig;
import pigcart.particlerain.particle.*;

public class ParticleRainClient implements ClientModInitializer {

    public static final String MOD_ID = "particlerain";

    public static SimpleParticleType RAIN;
    public static SimpleParticleType SNOW;
    public static SimpleParticleType DUST_MOTE;
    public static SimpleParticleType DUST;
    public static SimpleParticleType FOG;
    public static SimpleParticleType GROUND_FOG;
    public static SimpleParticleType SHRUB;
    public static SimpleParticleType RIPPLE;
    public static SimpleParticleType STREAK;
    //TODO: hail particles
    //TODO: puddle blocks
    //TODO: vertical fog
    //TODO: light rays???? under the ocean surface

    //public static Block PUDDLE;
    //public static final ResourceKey<Block> PUDDLE_KEY = ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ParticleRainClient.MOD_ID, "puddle"));

    public static SoundEvent WEATHER_SNOW;
    public static SoundEvent WEATHER_SNOW_ABOVE;
    public static SoundEvent WEATHER_SANDSTORM;
    public static SoundEvent WEATHER_SANDSTORM_ABOVE;

    public static int weatherIntensity = 0;

    @Override
    public void onInitializeClient() {
        ModConfig.loadConfig();
        RAIN = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "rain"), FabricParticleTypes.simple(true));
        SNOW = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "snow"), FabricParticleTypes.simple(true));
        DUST_MOTE = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "dust_mote"), FabricParticleTypes.simple(true));
        DUST = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "dust"), FabricParticleTypes.simple(true));
        SHRUB = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "shrub"), FabricParticleTypes.simple(true));
        FOG = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "fog"), FabricParticleTypes.simple(true));
        GROUND_FOG = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "ground_fog"), FabricParticleTypes.simple(true));
        RIPPLE = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "ripple"), FabricParticleTypes.simple(true));
        STREAK = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "streak"), FabricParticleTypes.simple(true));

        //PUDDLE = Registry.register(BuiltInRegistries.BLOCK, PUDDLE_KEY, new PuddleBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).setId(PUDDLE_KEY)));

        WEATHER_SNOW = createSoundEvent("weather.snow");
        WEATHER_SNOW_ABOVE = createSoundEvent("weather.snow.above");
        WEATHER_SANDSTORM = createSoundEvent("weather.sandstorm");
        WEATHER_SANDSTORM_ABOVE = createSoundEvent("weather.sandstorm.above");

        ParticleFactoryRegistry.getInstance().register(RAIN, RainParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(SNOW, SnowParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(DUST_MOTE, DustMoteParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(DUST, DustParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(SHRUB, ShrubParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(FOG, FogParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(GROUND_FOG, GroundFogParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(RIPPLE, RippleParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(STREAK, StreakParticle.DefaultFactory::new);

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register(this::onLevelChange);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> cmd = ClientCommandManager.literal(ParticleRainClient.MOD_ID)
                    .executes(ctx -> {
                        ClientLevel level = Minecraft.getInstance().level;
                        ctx.getSource().sendFeedback(Component.literal(String.format("Particle count: %d/%d", WeatherParticleManager.particleCount, ModConfig.CONFIG.perf.maxParticleAmount)));
                        ctx.getSource().sendFeedback(Component.literal(String.format("Fog density: %d/%d", WeatherParticleManager.fogCount, ModConfig.CONFIG.groundFog.density)));
                        BlockPos blockPos = BlockPos.containing(ctx.getSource().getPlayer().position());
                        final Holder<Biome> holder = level.getBiome(blockPos);
                        String biomeStr = holder.unwrap().map((resourceKey) -> {
                            return resourceKey.location().toString();
                        }, (biome) -> {
                            return "[unregistered " + String.valueOf(biome) + "]";
                        });
                        ctx.getSource().sendFeedback(Component.literal(String.format("Biome: " + biomeStr)));
                        Biome.Precipitation precipitation = holder.value().getPrecipitationAt(blockPos, level.getSeaLevel());
                        ctx.getSource().sendFeedback(Component.literal(String.format("Precipitation: " + precipitation)));
                        ctx.getSource().sendFeedback(Component.literal(String.format("Base Temp: " + holder.value().getBaseTemperature())));
                        //WeatherBlockSpawner.tick(level);
                        ctx.getSource().sendFeedback(Component.literal(String.format("Block has puddle: " + WeatherBlockManager.hasPuddle(level, blockPos.below()))));
                        ctx.getSource().sendFeedback(Component.literal(String.format("Block is solid: " + level.getBlockState(blockPos.below()).isCollisionShapeFullBlock(level, blockPos.below()))));
                        ctx.getSource().sendFeedback(Component.literal(String.format("Block is exposed: " + WeatherBlockManager.isExposed(level, blockPos))));
                        ctx.getSource().sendFeedback(Component.literal(String.format("Puddle target level: " + WeatherBlockManager.puddleTargetLevel)));
                        ctx.getSource().sendFeedback(Component.literal(String.format("Puddle threshold: " + WeatherBlockManager.puddleThreshold)));
                        return 0;
                    });
            dispatcher.register(cmd);
        });
    }

    private void onLevelChange(Minecraft minecraft, ClientLevel clientLevel) {
        WeatherParticleManager.resetParticleCount();
    }

    private void onTick(Minecraft client) {
        if (!client.isPaused() && client.level != null && client.getCameraEntity() != null) {
            WeatherParticleManager.tick(client.level, client.getCameraEntity());
            WeatherBlockManager.tick(client.level);
        }
    }

    private static SoundEvent createSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
        return SoundEvent.createVariableRangeEvent(id);
    }
}