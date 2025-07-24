package pigcart.particlerain.config;

import com.google.gson.*;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.world.level.biome.Biome;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.WeatherParticleManager;

import java.awt.*;
import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    @NoGUI public static final Path CONFIG_FILE = Path.of("config").resolve(ParticleRain.MOD_ID + ".json");
    @NoGUI public static ModConfig CONFIG = new ModConfig();
    @NoGUI public static ModConfig DEFAULT = new ModConfig();
    @NoGUI public byte configVersion = 3;

    @Group
    public PerformanceOptions perf = new PerformanceOptions();
    public static class PerformanceOptions {
        public int maxParticleAmount = 1500;
        public int particleDensity = 100;
        public int particleStormDensity = 200;
        public int particleDistance = 16;
    }

    @Group
    public SoundOptions sound = new SoundOptions();
    public static class SoundOptions {
        public boolean doRainSounds = true;
        public boolean doSnowSounds = true;
        public boolean doWindSounds = true;
        //public boolean doSleetSounds = true;
        //public boolean doHailSounds = true;
    }

    @Group
    public WindOptions wind = new WindOptions();
    public static class WindOptions {
        public float strength = 0.4F;
        public float strengthVariance = 0.3F;
        public float gustFrequency = 0.2F;
        public float modulationSpeed = 0.04F;
    }

    @Group
    public CompatibilityOptions compat = new CompatibilityOptions();
    public static class CompatibilityOptions {
        public boolean renderDefaultWeather = false;
        public boolean doDefaultSplashing = false;
        @ReloadsResources
        public boolean waterTint = true;
        @Percentage
        public float tintMix = 0.6F;
        @NoGUI public boolean shaderpackTint = true; //TODO
        public boolean yLevelWindAdjustment = true;
        public boolean syncRegistries = true;
        public boolean crossBiomeBorder = false;
        public boolean useHeightmapTemp = true;
        public boolean canSpawnAboveClouds = true;
        //public int cloudHeight = 191;
    }

    public enum RenderType {
        TERRAIN,
        OPAQUE,
        TRANSLUCENT,
        BLENDED
    }

    public enum TintType {
        WATER,
        FOG,
        FOLIAGE,
        MAP,
        CUSTOM,
        NONE
    }

    public enum RotationType {
        COPY_CAMERA,
        RELATIVE_VELOCITY,
        LOOKAT_PLAYER,
        FLAT_PLANES
    }

    @NoGUI
    public List<ParticleOptions> customParticles = new ArrayList<>(List.of(
            new ParticleOptions(
                    "rain",
                    true,
                    1.0F,
                    List.of(Biome.Precipitation.RAIN),
                    false,
                    new ArrayList<>(),
                    true,
                    new ArrayList<>(),
                    false,
                    0.9F,
                    0.5F,
                    1.0F,
                    0.0F,
                    1.0F,
                    1.5F,
                    false,
                    RenderType.TRANSLUCENT,
                    List.of("particlerain:rain_0", "particlerain:rain_1", "particlerain:rain_2", "particlerain:rain_3"),
                    TintType.WATER,
                    RotationType.RELATIVE_VELOCITY),
            new ParticleOptions(
                    "snow",
                    true,
                    0.4F,
                    List.of(Biome.Precipitation.SNOW),
                    false,
                    new ArrayList<>(),
                    true,
                    new ArrayList<>(),
                    false,
                    0.05F,
                    0.1F,
                    0.2F,
                    0.4F,
                    1.0F,
                    1.5F,
                    false,
                    RenderType.TRANSLUCENT,
                    List.of("particlerain:snow_0", "particlerain:snow_1", "particlerain:snow_2", "particlerain:snow_3"),
                    TintType.NONE,
                    RotationType.COPY_CAMERA),
            new ParticleOptions(
                    "dust",
                    true,
                    0.8F,
                    List.of(Biome.Precipitation.NONE),
                    false,
                    new ArrayList<>(),
                    true,
                    List.of("minecraft:camel_sand_step_sound_blocks", "minecraft:sand", "minecraft:terracotta", "c:sandstone_blocks"),
                    false,
                    0.2F,
                    2.0F,
                    3.0F,
                    0.0F,
                    1.0F,
                    1.5F,
                    false,
                    RenderType.TRANSLUCENT,
                    List.of("particlerain:dust"),
                    TintType.MAP,
                    RotationType.COPY_CAMERA),
            new ParticleOptions(
                    "rain_haze",
                    true,
                    0.1F,
                    List.of(Biome.Precipitation.RAIN),
                    false,
                    new ArrayList<>(),
                    false,
                    new ArrayList<>(),
                    false,
                    0.15F,
                    0.01F,
                    0.1F,
                    0.0F,
                    0.45F,
                    0.3F,
                    true,
                    RenderType.TRANSLUCENT,
                    List.of("particlerain:fog_dithered"),
                    TintType.FOG,
                    RotationType.LOOKAT_PLAYER),
            new ParticleOptions(
                    "snow_haze",
                    true,
                    0.2F,
                    List.of(Biome.Precipitation.SNOW),
                    false,
                    new ArrayList<>(),
                    false,
                    new ArrayList<>(),
                    false,
                    0.05F,
                    0.01F,
                    0.1F,
                    0.0F,
                    0.45F,
                    0.3F,
                    true,
                    RenderType.TRANSLUCENT,
                    List.of("particlerain:fog_dithered"),
                    TintType.FOG,
                    RotationType.LOOKAT_PLAYER),
            new ParticleOptions(
                    "dust_haze",
                    true,
                    0.1F,
                    List.of(Biome.Precipitation.NONE),
                    false,
                    new ArrayList<>(),
                    true,
                    List.of("minecraft:camel_sand_step_sound_blocks", "minecraft:sand", "minecraft:terracotta", "c:sandstone_blocks"),
                    false,
                    0.1F,
                    1.0F,
                    2.0F,
                    0.0F,
                    0.45F,
                    0.3F,
                    true,
                    RenderType.TRANSLUCENT,
                    List.of("particlerain:fog_dithered"),
                    TintType.MAP,
                    RotationType.LOOKAT_PLAYER),
            new ParticleOptions(
                    "rain_splashing",
                    true,
                    1.0F,
                    List.of(Biome.Precipitation.RAIN),
                    false,
                    new ArrayList<>(),
                    false,
                    new ArrayList<>(List.of("minecraft:lava")),
                    true,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.45F,
                    0.25F,
                    false,
                    RenderType.OPAQUE,
                    List.of(),
                    TintType.WATER,
                    RotationType.COPY_CAMERA),
            new ParticleOptions(
                    "rain_ripples",
                    true,
                    1.0F,
                    List.of(Biome.Precipitation.RAIN),
                    false,
                    new ArrayList<>(),
                    true,
                    new ArrayList<>(List.of("minecraft:water")),
                    true,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.45F,
                    0.25F,
                    false,
                    RenderType.OPAQUE,
                    List.of(),
                    TintType.WATER,
                    RotationType.COPY_CAMERA),
            new ParticleOptions(
                    "rain_smoke",
                    true,
                    1.0F,
                    List.of(Biome.Precipitation.RAIN),
                    false,
                    new ArrayList<>(),
                    true,
                    new ArrayList<>(List.of("minecraft:strider_warm_blocks", "minecraft:infiniburn_overworld")),
                    true,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.1F,
                    0.45F,
                    0.25F,
                    false,
                    RenderType.OPAQUE,
                    List.of(),
                    TintType.WATER,
                    RotationType.COPY_CAMERA),
            new ParticleOptions(
                    "shrubs",
                    true,
                    0.002F,
                    List.of(Biome.Precipitation.NONE),
                    false,
                    new ArrayList<>(),
                    true,
                    List.of("minecraft:camel_sand_step_sound_blocks", "minecraft:sand", "minecraft:terracotta", "c:sandstone_blocks"),
                    true,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.1F,
                    0.45F,
                    0.25F,
                    false,
                    RenderType.OPAQUE,
                    List.of(),
                    TintType.WATER,
                    RotationType.COPY_CAMERA)
    ));
    //TODO
    // heavy rain
    // heavy snow
    // sleet
    // hail
    // new shrub / block model particles
    // new mist
    // splash replacement - splatter
    // new streaks

    public static class ParticleOptions {
        ParticleOptions() {}
            ParticleOptions(String id, boolean enabled, float density, List<Biome.Precipitation> precipitation, boolean biomeWhitelist, List<String> biomeList, boolean blockWhitelist, List<String> blockList, boolean onGround, float gravity, float windStrength, float stormWindStrength, float rotationAmount, float opacity, float size, boolean constantScreenSize, RenderType renderType, List<String> spriteLocations, TintType tintType, RotationType rotationType) {
            this.id = id;
            this.enabled = enabled;

            this.density = density;
            this.precipitation = precipitation;
            this.biomeWhitelist = biomeWhitelist;
            this.biomeList = biomeList;
            this.blockWhitelist = blockWhitelist;
            this.blockList = blockList;
            this.onGround = onGround;

            this.gravity = gravity;
            this.windStrength = windStrength;
            this.stormWindStrength = stormWindStrength;
            this.rotationAmount = rotationAmount;

            this.opacity = opacity;
            this.size = size;
            this.constantScreenSize = constantScreenSize;
            this.renderType = renderType;
            this.spriteLocations = spriteLocations;
            this.tintType = tintType;
            this.rotationType = rotationType;
        }
        public String id = "new_particle";
        public boolean enabled = true;
        @Label(key="spawning")
        @Percentage
        public float density = 1.0F;
        public List<Biome.Precipitation> precipitation = List.of(Biome.Precipitation.RAIN);
        @BooleanFormat(t="whitelist", f="blacklist")
        public boolean biomeWhitelist = false;
        public List<String> biomeList = new ArrayList<>();
        public transient URI biomeTagsWiki = URI.create("https://wiki.fabricmc.net/community:common_tags#biome_tags");
        @BooleanFormat(t="whitelist", f="blacklist")
        public boolean blockWhitelist = false;
        public List<String> blockList = new ArrayList<>();
        public transient URI blockTagsWiki = URI.create("https://minecraft.wiki/w/Block_tag_(Java_Edition)");
        public boolean onGround = false;
        @Label(key="motion")
        public float gravity = 0.9F;
        public float windStrength = 0.5F;
        public float stormWindStrength = 1.0F;
        public float rotationAmount = 0F;
        public int lifetime = 3000;
        @Label(key="appearance")
        @Percentage
        public float opacity = 0.9F;
        public float size = 0.5F;
        public boolean constantScreenSize = false;
        public RenderType renderType = RenderType.TRANSLUCENT;
        public List<String> spriteLocations = List.of("particlerain:rain_0", "particlerain:rain_1", "particlerain:rain_2", "particlerain:rain_3");
        public TintType tintType = TintType.NONE;
        public Color customTint = Color.BLACK;
        public RotationType rotationType = RotationType.COPY_CAMERA;
    }

    @NoGUI
    public ShrubOptions shrub = new ShrubOptions();
    @OverrideName(newName = "ParticleOptions")
    public static class ShrubOptions {
        @Percentage
        public float density = 0.02F;
        public float gravity = 0.2F;
        public float windStrength = 0.2F;
        public float stormWindStrength = 0.3F;
        @Percentage
        public float opacity = 1F;
        public float size = 0.5F;

        public float rotationAmount = 0.2F;
        public float bounciness = 0.2F;
    }

    @NoGUI
    public RippleOptions ripple = new RippleOptions();
    @OverrideName(newName = "ParticleOptions")
    public static class RippleOptions {
        @Percentage
        public float opacity = 0.9F;
        public float size = 0.25F;

        @ReloadsResources
        public int resolution = 16;
        @ReloadsResources
        public boolean useResourcepackResolution = true;
    }

    @NoGUI
    public StreakOptions streak = new StreakOptions();
    @OverrideName(newName = "ParticleOptions")
    public static class StreakOptions {
        @Percentage
        public float opacity = 0.9F;
        public float size = 0.5F;
    }

    @NoGUI
    public MistOptions mist = new MistOptions();
    @OverrideName(newName = "ParticleOptions")
    public static class MistOptions {
        @Percentage
        public float density = 20;
        public float windStrength = 0;
        public float stormWindStrength = 0;
        @Percentage
        public float opacity = 0.9F;
        public float size = 8F;

        public int maxSpawnHeight = 64;
        public int minSpawnHeight = 60;
    }

    public static class ColorTypeAdapter implements JsonSerializer<Color>, JsonDeserializer<Color> {
        @Override
        public JsonElement serialize(Color src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getRGB());
        }
        @Override
        public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new Color(json.getAsInt());
        }
    }
    static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Color.class, new ColorTypeAdapter())
            .setPrettyPrinting()
            .create();

    public static void loadConfig() {
        File file = CONFIG_FILE.toFile();

        if (file.exists()) {
            try {
                BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
                ModConfig.CONFIG = GSON.fromJson(fileReader, ModConfig.class);
            } catch (Exception e) {
                ParticleRain.LOGGER.error("Failed to load config - " + e.getMessage());
                ModConfig.CONFIG = null;
            }
        } else {
            saveConfig();
        }
        if (ModConfig.CONFIG == null || ModConfig.CONFIG.configVersion < ModConfig.DEFAULT.configVersion) {
            ModConfig.CONFIG = ModConfig.DEFAULT;
            saveConfig();
        }
    }

    public static void saveConfig() {
        WeatherParticleManager.particleGroup = new ParticleGroup(CONFIG.perf.maxParticleAmount);
        //TODO: deserialize string values for use on save or see if yacl has something for this
        File file = CONFIG_FILE.toFile();
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(ModConfig.CONFIG, writer);
        } catch (IOException e) {
            ParticleRain.LOGGER.error(e.getMessage());
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface Percentage {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface OverrideName { String newName(); }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface ReloadsResources {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface Label {String key();}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface BooleanFormat {String t(); String f();}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface Group {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface NoGUI {}
}