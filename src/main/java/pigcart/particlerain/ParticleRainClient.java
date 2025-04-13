package pigcart.particlerain;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import pigcart.particlerain.config.ModConfig;

public class ParticleRainClient {

    public static final String MOD_ID = "particlerain";

    // conventional tags
    public static final TagKey<Block> GLASS_PANES = tagOf("glass_panes");
    public static TagKey<Block> tagOf(String tagId) {
        return TagKey.create(Registries.BLOCK, StonecutterUtil.getResourceLocation("c", tagId));
    }

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
    //TODO: vertical fog
    //TODO: light rays???? under the ocean surface

    //public static Block PUDDLE;
    //public static final ResourceKey<Block> PUDDLE_KEY = ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ParticleRainClient.MOD_ID, "puddle"));

    public static SoundEvent WEATHER_SNOW;
    public static SoundEvent WEATHER_SNOW_ABOVE;
    public static SoundEvent WEATHER_SANDSTORM;
    public static SoundEvent WEATHER_SANDSTORM_ABOVE;

    public static void onInitializeClient() {
        ModConfig.loadConfig();

        WEATHER_SNOW = createSoundEvent("weather.snow");
        WEATHER_SNOW_ABOVE = createSoundEvent("weather.snow.above");
        WEATHER_SANDSTORM = createSoundEvent("weather.sandstorm");
        WEATHER_SANDSTORM_ABOVE = createSoundEvent("weather.sandstorm.above");
    }

    public static void onJoin() {
        WeatherBlockManager.puddleThreshold = 0;
    }

    public static void onTick(Minecraft client) {
        if (!client.isPaused() && client.level != null && client.getCameraEntity() != null) {
            WeatherParticleManager.tick(client.level, client.getCameraEntity());
            WeatherBlockManager.tick(client.level);
        }
    }

    private static SoundEvent createSoundEvent(String name) {
        ResourceLocation id = StonecutterUtil.getResourceLocation(MOD_ID, name);
        return SoundEvent.createVariableRangeEvent(id);
    }

    @SuppressWarnings("unchecked")
    public static <S> LiteralArgumentBuilder<S> getCommands() {
        return (LiteralArgumentBuilder<S>) LiteralArgumentBuilder.literal(MOD_ID)
                .executes(ctx -> {
                    ClientLevel level = Minecraft.getInstance().level;
                    addChatMsg(String.format("Particle count: %d/%d", WeatherParticleManager.particleCount, ModConfig.CONFIG.perf.maxParticleAmount));
                    addChatMsg(String.format("Fog density: %d/%d", WeatherParticleManager.fogCount, ModConfig.CONFIG.groundFog.density));
                    BlockPos blockPos = BlockPos.containing(Minecraft.getInstance().player.position());
                    final Holder<Biome> holder = level.getBiome(blockPos);
                    String biomeStr = holder.unwrap().map((resourceKey) -> {
                        return resourceKey.location().toString();
                    }, (biome) -> {
                        return "[unregistered " + String.valueOf(biome) + "]";
                    });
                    addChatMsg("Biome: " + biomeStr);
                    //Biome.Precipitation precipitation = holder.value().getPrecipitationAt(blockPos, level.getSeaLevel());
                    //addChatMsg("Precipitation: " + precipitation);
                    addChatMsg("Base Temp: " + holder.value().getBaseTemperature());
                    //WeatherBlockSpawner.tick(level);
                    addChatMsg("Block has puddle: " + WeatherBlockManager.hasPuddle(level, blockPos.below()));
                    addChatMsg("Block is solid: " + level.getBlockState(blockPos.below()).isCollisionShapeFullBlock(level, blockPos.below()));
                    addChatMsg("Block is exposed: " + WeatherBlockManager.isExposed(level, blockPos));
                    addChatMsg("Puddle target level: " + WeatherBlockManager.puddleTargetLevel);
                    addChatMsg("Puddle threshold: " + WeatherBlockManager.puddleThreshold);
                    return 0;
                });
    }

    private static void addChatMsg(String message) {
        Minecraft.getInstance().gui.getChat().addMessage(Component.literal(message));
    }
}