package pigcart.particlerain.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.world.level.biome.Biome;
import pigcart.particlerain.ParticleRainClient;

import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModConfig {
    public static final Path CONFIG_FILE = Path.of("config").resolve(ParticleRainClient.MOD_ID + ".json");

    public static ModConfig CONFIG = new ModConfig();
    public static ModConfig DEFAULT = new ModConfig();

    /*
    public enum Preset {
        FAST,
        FANCY,
        CUSTOM;
    }
    public void setPresetOptions(Preset preset) {
        switch (preset) {
            case FAST -> {
                this.perf.maxParticleAmount = 1000;
                this.perf.particleDensity = 80;
                this.perf.particleStormDensity = 150;
                this.perf.particleRadius = 15;
            }
            case FANCY -> {
                this.perf.maxParticleAmount = 1500;
                this.perf.particleDensity = 100;
                this.perf.particleStormDensity = 200;
                this.perf.particleRadius = 25;
            }
        }
    }
    @SerialEntry
    public Preset preset = Preset.FANCY;
     */

    public PerformanceOptions perf = new PerformanceOptions();
    public static class PerformanceOptions {
        public int maxParticleAmount = 1500;
        public int particleDensity = 100;
        public int particleStormDensity = 200;
        public int particleRadius = 25;
        public int fogParticleRadius = 90;
    }

    public EffectOptions effect = new EffectOptions();
    public static class EffectOptions {
        public boolean doRainParticles = true;
        public boolean doSplashParticles = true;
        public boolean doSmokeParticles = true;
        public boolean doRippleParticles = true;
        public boolean doStreakParticles = true;
        public boolean doSnowParticles = true;
        public boolean doDustParticles = true;
        public boolean doShrubParticles = true;
        public boolean doFogParticles = false;
        public boolean doGroundFogParticles = true;
        public boolean doPuddles = false;
    }

    public SoundOptions sound = new SoundOptions();
    public static class SoundOptions {
        //public boolean doMaterialSounds = true;
        public boolean doRainSounds = true;
        public boolean doSnowSounds = true;
        public boolean doSandSounds = true;
    }

    public CompatibilityOptions compat = new CompatibilityOptions();
    public static class CompatibilityOptions {
        public boolean renderVanillaWeather = false;
        public boolean tickVanillaWeather = false;
        public boolean alwaysRaining = false;
        @ReloadsResources
        public boolean biomeTint = true;
        @Percentage
        public float tintMix = 0.5F;
        public boolean yLevelWindAdjustment = true;
        public boolean syncRegistry = true;
    }
    public SpawnOptions spawn = new SpawnOptions();
    public static class SpawnOptions {
        public boolean doOverrideWeather = false;
        public Biome.Precipitation overrideWeather = Biome.Precipitation.SNOW;
        public boolean useHeightmapTemp = true;
        public boolean canSpawnAboveClouds = true;
        public int cloudHeight = 191;
        public transient URI openWiki = URI.create("https://minecraft.wiki/w/Block_tags_(Java_Edition)#camel_sand_step_sound_blocks");
        public List<String> dustyBlockTags = new ArrayList<>(Collections.singleton("minecraft:camel_sand_step_sound_blocks"));
        //public List<String> hotBlockTags = new ArrayList<>();
        //public List<String> biomeWeatherOverrides = new ArrayList<>(Collections.singleton("minecraft:ice_spikes HAIL"));
    }

    public RainOptions rain = new RainOptions();
    @OverrideName(newName = "ParticleOptions")
    public static class RainOptions {
        @Percentage
        public float density = 1.0F;
        public float gravity = 1.0F;
        public float windStrength = 0.3F;
        public float stormWindStrength = 0.7F;
        @Percentage
        public float opacity = 1;
        public float size = 2F;
        public int impactEffectAmount = 5;
    }
    public SnowOptions snow = new SnowOptions();
    @OverrideName(newName = "ParticleOptions")
    public static class SnowOptions {
        @Percentage
        public float density = 0.4F;
        public float gravity = 0.05F;
        public float rotationAmount = 0.02F;
        public float stormRotationAmount = 0.05F;
        public float windStrength = 1F;
        public float stormWindStrength = 3F;
        public float size = 2F;
    }
    public DustOptions dust = new DustOptions();
    @OverrideName(newName = "ParticleOptions")
    public static class DustOptions {
        @Percentage
        public float density = 0.8F;
        public float gravity = 0.2F;
        public float windStrength = 0.2F;
        public float stormWindStrength = 0.3F;
        //public float moteSize = 0.1F;
        public float size = 2F;
        public boolean spawnOnGround = true;
    }
    public ShrubOptions shrub = new ShrubOptions();
    @OverrideName(newName = "ParticleOptions")
    public static class ShrubOptions {
        @Percentage
        public float density = 0.02F;
        public float gravity = 0.2F;
        public float rotationAmount = 0.2F;
        public float windStrength = 0.2F;
        public float stormWindStrength = 0.3F;
        public float bounciness = 0.2F;
    }
    public RippleOptions ripple = new RippleOptions();
    @OverrideName(newName = "ParticleOptions")
    public static class RippleOptions {
        //public int amount = 5;
        @ReloadsResources
        public int resolution = 16;
        @ReloadsResources
        public boolean useResourcepackResolution = true;
    }
    public FogOptions fog = new FogOptions();
    @OverrideName(newName = "ParticleOptions")
    public static class FogOptions {
        @Percentage
        public float density = 0.2F;
        public float gravity = 0.2F;
        public float size = 0.5F;
    }
    public GroundFogOptions groundFog = new GroundFogOptions();
    @OverrideName(newName = "ParticleOptions")
    public static class GroundFogOptions {
        public int density = 20;
        public int maxSpawnHeight = 64;
        public int minSpawnHeight = 60;
        public float size = 8F;
    }
    public PuddleOptions puddle = new PuddleOptions();
    public static class PuddleOptions {
        public int updateDelay = 100;
        public int updateStep = 5;
        public int rainLevel = 90;
        public int stormLevel = 130;
    }

    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void loadConfig() {
        File file = CONFIG_FILE.toFile();

        if (file.exists()) {
            try (BufferedReader fileReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
            )) {
                ModConfig.CONFIG = GSON.fromJson(fileReader, ModConfig.class);
            } catch (IOException e) {
                throw new RuntimeException("Particle Rain config failed to load: ", e);
            }
        }
        if (ModConfig.CONFIG == null) {
            ModConfig.CONFIG = new ModConfig();
        }
    }

    public static void saveConfig() {
        File file = CONFIG_FILE.toFile();
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(ModConfig.CONFIG, writer);
        } catch (IOException e) {
            e.printStackTrace();
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
}