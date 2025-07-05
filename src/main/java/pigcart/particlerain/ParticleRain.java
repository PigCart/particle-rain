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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pigcart.particlerain.config.ModConfig;
import pigcart.particlerain.config.ConfigScreens;

import java.text.DecimalFormat;

public class ParticleRain {
    public static int clientTicks = 0;
    public static final String MOD_ID = "particlerain";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    //TODO
    public static final TagKey<Block> GLASS_PANES = commonBlockTag("glass_panes");
    public static TagKey<Block> commonBlockTag(String tagId) {
        return TagKey.create(Registries.BLOCK, StonecutterUtil.getResourceLocation("c", tagId));
    }

    //TODO
    public static SimpleParticleType MIST;
    public static SimpleParticleType SHRUB;
    public static SimpleParticleType RIPPLE;
    public static SimpleParticleType STREAK;

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

    public static void onTick(Minecraft client) {
        if (!client.isPaused() && client.level != null && client.gameRenderer.getMainCamera().isInitialized()) {
            clientTicks++;
            WeatherParticleManager.tick(client.level, client.gameRenderer.getMainCamera().getPosition());
            TaskScheduler.tick();
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
                    // give minecraft a tick to close the chat screen
                    TaskScheduler.scheduleDelayed(1, () ->
                            Minecraft.getInstance().setScreen(ConfigScreens.generateMainConfigScreen(null))
                    );
                    return 0;
                })
                .then(LiteralArgumentBuilder.literal("debug")
                        .executes(ctx -> {
                            ClientLevel level = Minecraft.getInstance().level;
                            addChatMsg(String.format("Particle count: %d/%d", WeatherParticleManager.particleCount, ModConfig.CONFIG.perf.maxParticleAmount));
                            addChatMsg(String.format("Fog density: %d/%f", WeatherParticleManager.fogCount, ModConfig.CONFIG.mist.density));
                            BlockPos blockPos = BlockPos.containing(Minecraft.getInstance().player.position());
                            final Holder<Biome> holder = level.getBiome(blockPos);
                            String biomeStr = holder.unwrap().map((resourceKey) -> {
                                return resourceKey.location().toString();
                            }, (biome) -> {
                                return "[unregistered " + String.valueOf(biome) + "]";
                            });
                            addChatMsg("Biome: " + biomeStr);
                            Biome.Precipitation precipitation = StonecutterUtil.getPrecipitationAt(level, holder.value(), blockPos);
                            addChatMsg("Precipitation: " + precipitation);
                            addChatMsg("Base Temp: " + holder.value().getBaseTemperature());
                            addChatMsg("Cloud height: " + StonecutterUtil.getCloudHeight(level));
                            return 0;
                        })
                );
    }
    private static void addChatMsg(String message) {
        Minecraft.getInstance().gui.getChat().addMessage(Component.literal(message));
    }

    public static void debugValue(float value) {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        Minecraft.getInstance().gui.setOverlayMessage(Component.literal(df.format(value)), true);
    }
}