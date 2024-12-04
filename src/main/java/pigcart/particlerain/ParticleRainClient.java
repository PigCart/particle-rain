package pigcart.particlerain;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import it.unimi.dsi.fastutil.ints.IntUnaryOperator;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import org.joml.Math;
import pigcart.particlerain.particle.*;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

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

    public static SoundEvent WEATHER_SNOW;
    public static SoundEvent WEATHER_SNOW_ABOVE;
    public static SoundEvent WEATHER_SANDSTORM;
    public static SoundEvent WEATHER_SANDSTORM_ABOVE;

    public static ModConfig config;
    public static int particleCount;
    public static int fogCount;
    public static boolean previousBiomeTintOption;

    @Override
    public void onInitializeClient() {
        RAIN = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "rain"), FabricParticleTypes.simple(true));
        SNOW = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "snow"), FabricParticleTypes.simple(true));
        DUST_MOTE = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "dust_mote"), FabricParticleTypes.simple(true));
        DUST = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "dust"), FabricParticleTypes.simple(true));
        SHRUB = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "shrub"), FabricParticleTypes.simple(true));
        FOG = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "fog"), FabricParticleTypes.simple(true));
        GROUND_FOG = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "ground_fog"), FabricParticleTypes.simple(true));
        RIPPLE = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "ripple"), FabricParticleTypes.simple(true));
        STREAK = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "streak"), FabricParticleTypes.simple(true));

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

        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        AutoConfig.getConfigHolder(ModConfig.class).registerSaveListener(ParticleRainClient::saveListener);

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        ClientPlayConnectionEvents.JOIN.register(this::onJoin);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> cmd = ClientCommandManager.literal(ParticleRainClient.MOD_ID)
                    .executes(ctx -> {
                        ctx.getSource().sendFeedback(Component.literal(String.format("Particle count: %d/%d", particleCount, config.maxParticleAmount)));
                        ctx.getSource().sendFeedback(Component.literal(String.format("Fog density: %d/%d", fogCount, config.groundFog.density)));
                        return 0;
                    });
            dispatcher.register(cmd);
        });
    }

    private static InteractionResult saveListener(ConfigHolder<ModConfig> modConfigConfigHolder, ModConfig modConfig) {
        if (ParticleRainClient.config.rain.biomeTint != previousBiomeTintOption) {
            Minecraft.getInstance().reloadResourcePacks();
        }
        return InteractionResult.PASS;
    }

    //TODO: recolor the vanilla drop and splash water particles
    public static IntUnaryOperator desaturateOperation = (int rgba) -> {
            Color col = new Color(rgba, true);
            int gray = (col.getRed() + col.getGreen() + col.getBlue()) / 3;
            return ((col.getAlpha() & 0xFF) << 24) |
                    ((gray & 0xFF) << 16) |
                    ((gray & 0xFF) << 8)  |
                    ((gray & 0xFF));
    };

    private void onJoin(ClientPacketListener clientPacketListener, PacketSender packetSender, Minecraft minecraft) {
        particleCount = 0;
        fogCount = 0;
    }

    private void onTick(Minecraft client) {
        if (!client.isPaused() && client.level != null && client.getCameraEntity() != null)
            WeatherParticleSpawner.update(client.level, client.getCameraEntity(), client.getFrameTimeNs());
    }

    private static SoundEvent createSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
        return SoundEvent.createVariableRangeEvent(id);
    }

    public static NativeImage loadTexture(ResourceLocation resourceLocation) throws IOException {
        Resource resource = Minecraft.getInstance().getResourceManager().getResourceOrThrow(resourceLocation);
        InputStream inputStream = resource.open();
        NativeImage nativeImage;
        try {
            nativeImage = NativeImage.read(inputStream);
        } catch (Throwable owo) {
            try {
                inputStream.close();
            } catch (Throwable uwu) {
                owo.addSuppressed(uwu);
            }
            throw owo;
        }
        inputStream.close();
        return nativeImage;
    }

    public static SpriteContents splitImage(NativeImage image, int segment, String id) {
        int size = image.getWidth();
        NativeImage sprite = new NativeImage(size, size, false);
        image.copyRect(sprite, 0, size * segment, 0, 0, size, size, true, true);
        return(new SpriteContents(ResourceLocation.fromNamespaceAndPath(ParticleRainClient.MOD_ID, id + segment), new FrameSize(size, size), sprite, new ResourceMetadata.Builder().build()));
    }

    public static double yLevelWindAdjustment(double y) {
        return Math.clamp(0.01, 1, (y - 64) / 40);
    }

    public static SpriteContents generateRipple(int i) {
        int size = 16;
        float radius = ((size / 2F) / 8) * (i + 1);
        NativeImage image = new NativeImage(size, size, true);
        Color color = Color.WHITE;
        int colorint = ((color.getAlpha() & 0xFF) << 24) |
                ((color.getRed() & 0xFF) << 16) |
                ((color.getGreen() & 0xFF) << 8)  |
                ((color.getBlue() & 0xFF));

        generateBresenhamCircle(image, size, (int) Math.clamp(1, (size / 2F) - 1, radius), colorint);
        return(new SpriteContents(ResourceLocation.fromNamespaceAndPath(ParticleRainClient.MOD_ID, "ripple" + i), new FrameSize(size, size), image, new ResourceMetadata.Builder().build()));
    }

    public static void generateBresenhamCircle(NativeImage image, int imgSize, int radius, int colorint) {
        int centerX = imgSize / 2;
        int centerY = imgSize / 2;
        int x = 0, y = radius;
        int d = 3 - 2 * radius;
        drawCirclePixel(centerX, centerY, x, y, image, colorint);
        while (y >= x){

            // check for decision parameter
            // and correspondingly
            // update d, y
            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            }
            else
                d = d + 4 * x + 6;

            // Increment x after updating decision parameter
            x++;

            // Draw the circle using the new coordinates
            drawCirclePixel(centerX, centerY, x, y, image, colorint);
        }
    }

    static void drawCirclePixel(int xc, int yc, int x, int y, NativeImage img, int col){
        img.setPixelRGBA(xc+x, yc+y, col);
        img.setPixelRGBA(xc-x, yc+y, col);
        img.setPixelRGBA(xc+x, yc-y, col);
        img.setPixelRGBA(xc-x, yc-y, col);
        img.setPixelRGBA(xc+y, yc+x, col);
        img.setPixelRGBA(xc-y, yc+x, col);
        img.setPixelRGBA(xc+y, yc-x, col);
        img.setPixelRGBA(xc-y, yc-x, col);
    }
}