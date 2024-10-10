package pigcart.particlerain;

import com.mojang.blaze3d.platform.NativeImage;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.sounds.SoundEvent;
import pigcart.particlerain.particle.*;

import java.io.IOException;
import java.io.InputStream;

public class ParticleRainClient implements ClientModInitializer {

    public static final String MOD_ID = "particlerain";

    public static SimpleParticleType RAIN_DROP;
    public static SimpleParticleType RAIN_SHEET;
    public static SimpleParticleType SNOW_FLAKE;
    public static SimpleParticleType SNOW_SHEET;
    public static SimpleParticleType DUST_MOTE;
    public static SimpleParticleType DUST_SHEET;
    public static SimpleParticleType FOG;
    public static SimpleParticleType DEAD_BUSH;

    public static SoundEvent WEATHER_SNOW;
    public static SoundEvent WEATHER_SNOW_ABOVE;
    public static SoundEvent WEATHER_SANDSTORM;
    public static SoundEvent WEATHER_SANDSTORM_ABOVE;

    public static ModConfig config;
    public static int particleCount;

    public static final ResourceLocation RAIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/rain.png");
    public static final ResourceLocation SNOW_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/snow.png");

    @Override
    public void onInitializeClient() {
        RAIN_DROP = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "rain_drop"), FabricParticleTypes.simple(true));
        RAIN_SHEET = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "rain_sheet"), FabricParticleTypes.simple(true));
        SNOW_FLAKE = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "snow_flake"), FabricParticleTypes.simple(true));
        SNOW_SHEET = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "snow_sheet"), FabricParticleTypes.simple(true));
        DUST_MOTE = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "dust_mote"), FabricParticleTypes.simple(true));
        DUST_SHEET = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "dust_sheet"), FabricParticleTypes.simple(true));
        DEAD_BUSH = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "dead_bush"), FabricParticleTypes.simple(true));
        FOG = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "fog"), FabricParticleTypes.simple(true));


        WEATHER_SNOW = createSoundEvent("weather.snow");
        WEATHER_SNOW_ABOVE = createSoundEvent("weather.snow.above");
        WEATHER_SANDSTORM = createSoundEvent("weather.sandstorm");
        WEATHER_SANDSTORM_ABOVE = createSoundEvent("weather.sandstorm.above");

        ParticleFactoryRegistry.getInstance().register(RAIN_DROP, RainParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(SNOW_FLAKE, SnowFlakeParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(SNOW_SHEET, SnowSheetParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(DUST_MOTE, DustMoteParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(DUST_SHEET, DustSheetParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(DEAD_BUSH, DeadBushParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(FOG, FogParticle.DefaultFactory::new);

        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        ClientPlayConnectionEvents.JOIN.register(this::onJoin);

        //AbstractTexture rainTexture = Minecraft.getInstance().getTextureManager().getTexture(ParticleRainClient.RAIN_LOCATION);

    }

    private void onJoin(ClientPacketListener clientPacketListener, PacketSender packetSender, Minecraft minecraft) {
        particleCount = 0;

        //ResourceLocation texLoc = ResourceLocation.withDefaultNamespace("textures/block/dirt.png");

        //TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        //AbstractTexture tex = textureManager.getTexture(texLoc);
        //SimpleTexture simpleTexture = new SimpleTexture(RAIN_LOCATION);
        //textureManager.register(ResourceLocation.fromNamespaceAndPath(MOD_ID, "uuhh"), abstractTexture);
        //DynamicTexture dynamicTexture = (DynamicTexture) tex;
        //if (dynamicTexture.getPixels() == null) System.out.println("NULLNULLUNKUNULNULLUNLUNLUNLUNNULUNLU");

        /*NativeImage nativeImage = null;
        try {
            nativeImage = getTexture(RAIN_LOCATION);
        } catch (IOException e) {
            e.printStackTrace();
        }

        var sc = new SpriteContents(RAIN_LOCATION, new FrameSize(64, 256), nativeImage, new ResourceMetadata.Builder().build());
        sprite = new TextureAtlasSprite(RAIN_LOCATION, sc, 64, 256, 0, 0){};

        //sprite = Minecraft.getInstance().getTextureAtlas(ResourceLocation.withDefaultNamespace("textures/atlas/blocks.png")).apply(ResourceLocation.fromNamespaceAndPath(MOD_ID, "uuhh"));
        //sprite = atlas.apply(RAIN_LOCATION);
        System.out.println(sprite.getX() + " " + sprite.getY());
        System.out.println(sprite.toString());*/
    }

    private void onTick(Minecraft client) {
        if (!client.isPaused() && client.level != null && client.getCameraEntity() != null)
            WeatherParticleSpawner.update(client.level, client.getCameraEntity(), client.getFrameTimeNs());
    }

    private static SoundEvent createSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
        return SoundEvent.createVariableRangeEvent(id);
    }

    public static NativeImage getTexture(ResourceLocation resourceLocation) throws IOException {
        Resource resource = Minecraft.getInstance().getResourceManager().getResourceOrThrow(resourceLocation);
        InputStream inputStream = resource.open();

        NativeImage nativeImage;
        try {
            nativeImage = NativeImage.read(inputStream);
        } catch (Throwable var9) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable var7) {
                    var9.addSuppressed(var7);
                }
            }

            throw var9;
        }

        if (inputStream != null) {
            inputStream.close();
        }
        return nativeImage;
    }
}