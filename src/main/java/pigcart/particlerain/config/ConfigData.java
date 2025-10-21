package pigcart.particlerain.config;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.biome.Biome;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.VersionUtil;
import pigcart.particlerain.WeatherParticleManager;
import pigcart.particlerain.config.ConfigManager.Label;
import pigcart.particlerain.config.Whitelist.BiomeList;
import pigcart.particlerain.config.Whitelist.BlockList;
import pigcart.particlerain.mixin.access.ParticleEngineAccessor;
import pigcart.particlerain.particle.CustomParticle;
import pigcart.particlerain.particle.render.BlendedParticleRenderType;
import pigcart.particlerain.config.ConfigManager.*;
//? if >=1.21.9 {
/*import net.minecraft.client.renderer.state.QuadParticleRenderState;
*///?}


import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static pigcart.particlerain.config.ConfigManager.config;

public class ConfigData {
    @NoGUI public byte configVersion = 5;

    @Group
    public PerformanceOptions perf = new PerformanceOptions();
    public static class PerformanceOptions {
        @OnChange(ClearParticles.class)
        public int maxParticleAmount = 1500;
        public int particleDensity = 100;
        public int particleStormDensity = 200;
        public int particleDistance = 16;
        public int surfaceRange = 48;
    }

    @Group
    public SoundOptions sound = new SoundOptions();
    public static class SoundOptions {
        @Slider @Format(PercentOrOff.class) public float rainVolume = 0.2F;
        @Slider @Format(PercentOrOff.class) public float snowVolume = 0.1F;
        @Slider @Format(PercentOrOff.class) public float windVolume = 0.1F;
        @Slider @Format(PercentOrOff.class) public float blockVolume = 0.07F;
    }

    @Group
    public WindOptions wind = new WindOptions();
    public static class WindOptions {
        public float strength = 0.4F;
        public float strengthVariance = 0.3F;
        public float gustFrequency = 0.2F;
        public float modulationSpeed = 0.04F;
        public boolean yLevelAdjustment = true;
    }

    @Group
    public CompatibilityOptions compat = new CompatibilityOptions();
    public static class CompatibilityOptions {
        public boolean renderDefaultWeather = false;
        public boolean doDefaultSplashing = false;
        @OnChange(ReloadResources.class)
        public boolean waterTint = true;
        @Slider @Format(Percent.class)
        public float tintMix = 0.6F;
        @NoGUI public boolean shaderpackTint = true; //TODO
        public boolean syncRegistries = true;
        public boolean crossBiomeBorder = false;
        public boolean useHeightmapTemp = true;
        public boolean doSpawnHeightLimit = true;
        @Format(ZeroIsAutomatic.class)
        public int spawnHeightLimit = 0;
    }

    @NoGUI
    public List<ParticleData> particles = new ArrayList<>(List.of(
            new ParticleData(
                    "rain",
                    true,
                    1.0F,
                    Weather.DURING_WEATHER,
                    List.of(Biome.Precipitation.RAIN),
                    new BiomeList(),
                    new BlockList(),
                    true,
                    SpawnPos.SKY,
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
            new ParticleData(
                    "snow",
                    true,
                    0.4F,
                    Weather.DURING_WEATHER,
                    List.of(Biome.Precipitation.SNOW),
                    new BiomeList(),
                    new BlockList(),
                    true,
                    SpawnPos.SKY,
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
            new ParticleData(
                    "dust",
                    true,
                    0.8F,
                    Weather.DURING_WEATHER,
                    List.of(Biome.Precipitation.NONE),
                    new BiomeList(),
                    new BlockList(true, List.of("#minecraft:camel_sand_step_sound_blocks", "#minecraft:sand", "#minecraft:terracotta", "#c:sandstone_blocks")),
                    true,
                    SpawnPos.SKY,
                    0.2F,
                    0.7F,
                    1F,
                    0.0F,
                    1.0F,
                    1.5F,
                    false,
                    RenderType.TRANSLUCENT,
                    List.of("particlerain:dust"),
                    TintType.MAP,
                    RotationType.COPY_CAMERA),
            new ParticleData(
                    "rain_haze",
                    true,
                    0.1F,
                    Weather.DURING_WEATHER,
                    List.of(Biome.Precipitation.RAIN),
                    new BiomeList(),
                    new BlockList(),
                    true,
                    SpawnPos.SKY,
                    0.15F,
                    0.01F,
                    0.1F,
                    0.0F,
                    0.35F,
                    0.3F,
                    true,
                    RenderType.TRANSLUCENT,
                    List.of("particlerain:fog_dithered"),
                    TintType.FOG,
                    RotationType.COPY_CAMERA),
            new ParticleData(
                    "snow_haze",
                    true,
                    0.2F,
                    Weather.DURING_WEATHER,
                    List.of(Biome.Precipitation.SNOW),
                    new BiomeList(),
                    new BlockList(),
                    true,
                    SpawnPos.SKY,
                    0.05F,
                    0.01F,
                    0.1F,
                    0.0F,
                    0.35F,
                    0.3F,
                    true,
                    RenderType.TRANSLUCENT,
                    List.of("particlerain:fog_dithered"),
                    TintType.FOG,
                    RotationType.COPY_CAMERA),
            new ParticleData(
                    "dust_haze",
                    true,
                    0.1F,
                    Weather.DURING_WEATHER,
                    List.of(Biome.Precipitation.NONE),
                    new BiomeList(),
                    new BlockList(true, List.of("#minecraft:camel_sand_step_sound_blocks", "#minecraft:sand", "#minecraft:terracotta", "#c:sandstone_blocks")),
                    true,
                    SpawnPos.SKY,
                    0.1F,
                    0.25F,
                    0.5F,
                    0.0F,
                    0.35F,
                    0.3F,
                    true,
                    RenderType.TRANSLUCENT,
                    List.of("particlerain:fog_dithered"),
                    TintType.MAP,
                    RotationType.COPY_CAMERA),
            new ParticleData(
                    "minecraft:rain",
                    "splatter",
                    true,
                    1.0F,
                    Weather.DURING_WEATHER,
                    List.of(Biome.Precipitation.RAIN),
                    new BiomeList(),
                    new BlockList(false, List.of("minecraft:lava")),
                    true,
                    SpawnPos.BLOCK_TOP),
            new ParticleData(
                    "particlerain:ripple",
                    "ripple",
                    true,
                    1.0F,
                    Weather.DURING_WEATHER,
                    List.of(Biome.Precipitation.RAIN),
                    new BiomeList(),
                    new BlockList(true, List.of("minecraft:water")),
                    true,
                    SpawnPos.BLOCK_TOP),
            new ParticleData(
                    "minecraft:smoke",
                    "smoke",
                    true,
                    1.0F,
                    Weather.DURING_WEATHER,
                    List.of(Biome.Precipitation.RAIN),
                    new BiomeList(),
                    new BlockList(true, List.of("#minecraft:strider_warm_blocks", "#minecraft:infiniburn_overworld")),
                    true,
                    SpawnPos.BLOCK_TOP),
            new ParticleData(
                    "particlerain:streak",
                    "streak",
                    true,
                    0.2F,
                    Weather.DURING_WEATHER,
                    List.of(Biome.Precipitation.RAIN),
                    new BiomeList(),
                    new BlockList(true, List.of("#minecraft:impermeable", "#c:glass_panes", "#minecraft:mineable/pickaxe")),
                    true,
                    SpawnPos.BLOCK_SIDES),
            new ParticleData(
                    "particlerain:shrub",
                    "shrub",
                    true,
                    0.002F,
                    Weather.DURING_WEATHER,
                    List.of(Biome.Precipitation.NONE),
                    new BiomeList(),
                    new BlockList(true, List.of("#minecraft:camel_sand_step_sound_blocks", "#minecraft:sand", "#minecraft:terracotta", "#c:sandstone_blocks")),
                    true,
                    SpawnPos.WORLD_SURFACE),
            new ParticleData(
                    "particlerain:mist",
                    "mist_after_rain",
                    true,
                    0.1F,
                    Weather.AFTER_WEATHER,
                    List.of(Biome.Precipitation.RAIN),
                    new BiomeList(),
                    new BlockList(true, List.of("#minecraft:dirt")),
                    true,
                    SpawnPos.WORLD_SURFACE),
            new ParticleData(
                    "particlerain:mist",
                    "mist",
                    true,
                    0.1F,
                    Weather.ALWAYS,
                    List.of(Biome.Precipitation.RAIN),
                    new BiomeList(true, List.of("#c:is_wet/overworld", "#c:is_spooky")),
                    new BlockList(true, List.of("#minecraft:dirt")),
                    true,
                    SpawnPos.WORLD_SURFACE)
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

    public static class ParticleData {
        ParticleData() {}
        ParticleData(String particle, String id, boolean enabled, float density, Weather weather, List<Biome.Precipitation> precipitation, BiomeList biomeList, BlockList blockList, boolean needsSkyAccess, SpawnPos spawnPos) {
            this.presetParticleId = particle;
            this.usePresetParticle = true;
            this.id = id;

            this.enabled = enabled;
            this.density = density;
            this.weather = weather;
            this.precipitation = precipitation;
            this.biomeList = biomeList;
            this.blockList = blockList;
            this.needsSkyAccess = needsSkyAccess;
            this.spawnPos = spawnPos;
        }
        public ParticleData(String id, boolean enabled, float density, Weather weather, List<Biome.Precipitation> precipitation, BiomeList biomeList, BlockList blockList, boolean needsSkyAccess, SpawnPos spawnPos, float gravity, float windStrength, float stormWindStrength, float rotationAmount, float opacity, float size, boolean constantScreenSize, RenderType renderType, List<String> spriteLocations, TintType tintType, RotationType rotationType) {
            this.id = id;

            this.enabled = enabled;
            this.density = density;
            this.weather = weather;
            this.precipitation = precipitation;
            this.biomeList = biomeList;
            this.blockList = blockList;
            this.needsSkyAccess = needsSkyAccess;
            this.spawnPos = spawnPos;

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
        public void setPresetParticle() {
            final Optional<ParticleType<?>> optional = BuiltInRegistries.PARTICLE_TYPE.getOptional(VersionUtil.parseId(presetParticleId));
            if (optional.isEmpty()) {
                ParticleRain.LOGGER.error("Incorrect configuration: {} is not a valid particle", presetParticleId);
            } else {
                presetParticle = (ParticleOptions) optional.get();
            }
        }
        @Dropdown(SupplyParticleTypes.class)
        @OnlyEditableIf(ParticleNotCustom.class)
        public String presetParticleId = "minecraft:flame";
        @NoGUI
        public transient ParticleOptions presetParticle = ParticleTypes.CLOUD;
        @RegenScreen
        public boolean usePresetParticle = false;
        @OnlyEditableIf(ParticleIsNotDefault.class)
        public String id = "new_particle";
        @Label(key="spawning")
        public boolean enabled = true;
        @Slider(step = 0.001F) @Format(Percent.class)
        public float density = 1.0F;
        public Weather weather = Weather.DURING_WEATHER;
        public List<Biome.Precipitation> precipitation = List.of(Biome.Precipitation.RAIN);
        public BiomeList biomeList = new BiomeList(true, new ArrayList<>());
        public BlockList blockList = new BlockList(true, new ArrayList<>());
        public boolean needsSkyAccess = false;
        public SpawnPos spawnPos = SpawnPos.SKY;
        @Label(key="motion")
        @OnlyVisibleIf(ParticleIsCustom.class) public float gravity = 0.9F;
        @OnlyVisibleIf(ParticleIsCustom.class) public float windStrength = 0.5F;
        @OnlyVisibleIf(ParticleIsCustom.class) public float stormWindStrength = 1.0F;
        @OnlyVisibleIf(ParticleIsCustom.class) public float rotationAmount = 0F;
        @OnlyVisibleIf(ParticleIsCustom.class) public int lifetime = 3000;
        @Label(key="appearance")
        @Slider @Format(Percent.class)
        @OnlyVisibleIf(ParticleIsCustom.class) public float opacity = 0.9F;
        @OnlyVisibleIf(ParticleIsCustom.class) public float size = 0.5F;
        @OnlyVisibleIf(ParticleIsCustom.class) public boolean constantScreenSize = false;
        @OnlyVisibleIf(ParticleIsCustom.class) public RenderType renderType = RenderType.TRANSLUCENT;
        @OnlyVisibleIf(ParticleIsCustom.class) public List<String> spriteLocations = List.of("particlerain:rain_0", "particlerain:rain_1", "particlerain:rain_2", "particlerain:rain_3");
        @OnlyVisibleIf(ParticleIsCustom.class) public TintType tintType = TintType.NONE;
        @OnlyVisibleIf(ParticleIsCustom.class) public Color customTint = Color.BLACK;
        @OnlyVisibleIf(ParticleIsCustom.class) public RotationType rotationType = RotationType.COPY_CAMERA;
    }

    public enum Weather {
        DURING_WEATHER {
            public boolean isCurrent(ClientLevel level) {
                return level.isRaining();
            }
        },
        ONLY_DURING_NORMAL_WEATHER {
            public boolean isCurrent(ClientLevel level) {
                return level.isRaining() && !level.isThundering();
            }
        },
        ONLY_DURING_STORMY_WEATHER {
            public boolean isCurrent(ClientLevel level) {
                return level.isThundering();
            }
        },
        AFTER_WEATHER {
            public boolean isCurrent(ClientLevel level) {
                return WeatherParticleManager.afterWeatherTicksLeft > 0;
            }
        },
        CLEAR {
            public boolean isCurrent(ClientLevel level) {
                return !level.isRaining();
            }
        },
        ALWAYS;
        public boolean isCurrent(ClientLevel level) {
            return true;
        }
    }

    public enum SpawnPos {
        SKY,
        BLOCK_SIDES,
        BLOCK_BOTTOM,
        BLOCK_TOP,
        WORLD_SURFACE
    }

    public enum RenderType {
        OPAQUE {
            @Override
            //? if >=1.21.9 {
            /*public SingleQuadParticle.Layer get() {
                return SingleQuadParticle.Layer.OPAQUE;
            }
            *///?} else {
            public ParticleRenderType get() {
                return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
            }
            //?}
        },
        TRANSLUCENT {
            @Override
                    //? if >=1.21.9 {
            /*public SingleQuadParticle.Layer get() {
                return SingleQuadParticle.Layer.TRANSLUCENT;
            }
            *///?} else {
            public ParticleRenderType get() {
                return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
            }
            //?}
        },
        BLENDED;

        //? if >=1.21.9 {
        /*public SingleQuadParticle.Layer get() {
            return BlendedParticleRenderType.INSTANCE;
        }
        *///?} else {
        public ParticleRenderType get() {
            return BlendedParticleRenderType.INSTANCE;
        }
        //?}
    }

    public enum TintType {
        WATER {
            public void applyTint(SingleQuadParticle p, ClientLevel level, BlockPos pos, ParticleData opts) {
                // TODO: IrisApi.isShaderPackInUse()
                final Color waterColor = new Color(BiomeColors.getAverageWaterColor(level, pos));
                final Color fogColor = new Color(level.getBiome(pos).value().getFogColor());
                float rCol = Mth.lerp(config.compat.tintMix, waterColor.getRed(), fogColor.getRed());
                float gCol = Mth.lerp(config.compat.tintMix, waterColor.getGreen(), fogColor.getGreen());
                float bCol = Mth.lerp(config.compat.tintMix, waterColor.getBlue(), fogColor.getBlue());
                p.setColor(rCol / 255F, gCol / 255F, bCol / 255F);
            }
        },
        FOG {
            public void applyTint(SingleQuadParticle p, ClientLevel level, BlockPos pos, ParticleData opts) {
                Color color = new Color(level.getBiome(pos).value().getFogColor()).darker();
                p.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
            }
        },
        MAP {
            public void applyTint(SingleQuadParticle p, ClientLevel level, BlockPos pos, ParticleData opts) {
                Color color = VersionUtil.getMapColor(level, pos);
                p.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
            }
        },
        CUSTOM {
            public void applyTint(SingleQuadParticle p, ClientLevel level, BlockPos pos, ParticleData opts) {
                p.setColor(opts.customTint.getRed() / 255F, opts.customTint.getGreen() / 255F, opts.customTint.getBlue() / 255F);
            }
        },
        NONE;
        public void applyTint(SingleQuadParticle p, ClientLevel level, BlockPos pos, ParticleData opts) {}
    }

    public enum RotationType {
        COPY_CAMERA {
            @Override
            public void render(/*? if >=1.21.9 {*//*QuadParticleRenderState*//*?} else {*/VertexConsumer/*?}*/ h, Camera camera, float tickPercent, CustomParticle p) {
                p.renderCameraCopyQuad(h, camera, tickPercent);
            }
        },
        RELATIVE_VELOCITY {
            @Override
            public void render(/*? if >=1.21.9 {*//*QuadParticleRenderState*//*?} else {*/VertexConsumer/*?}*/ h, Camera camera, float tickPercent, CustomParticle p) {
                p.renderRelativeVelocityQuad(h, camera, tickPercent);
            }
        },
        WORLD_VELOCITY {
            @Override
            public void render(/*? if >=1.21.9 {*//*QuadParticleRenderState*//*?} else {*/VertexConsumer/*?}*/ h, Camera camera, float tickPercent, CustomParticle p) {
                p.renderWorldVelocityQuad(h, camera, tickPercent);
            }
        },
        LOOKAT_PLAYER {
            @Override
            public void render(/*? if >=1.21.9 {*//*QuadParticleRenderState*//*?} else {*/VertexConsumer/*?}*/ h, Camera camera, float tickPercent, CustomParticle p) {
                p.renderLookingQuad(h, camera, tickPercent);
            }
        };
        public void render(/*? if >=1.21.9 {*//*QuadParticleRenderState*//*?} else {*/VertexConsumer/*?}*/ h, Camera camera, float tickPercent, CustomParticle p) {}
    }

    @NoGUI
    public ShrubOptions shrub = new ShrubOptions();
    @OverrideName("ParticleData")
    public static class ShrubOptions {
        public float gravity = 0.2F;
        public float windStrength = 0.2F;
        public float stormWindStrength = 0.3F;
        public float size = 0.5F;

        public float rotationAmount = 0.6F;
        public float bounciness = 0.2F;
    }

    @NoGUI
    public RippleOptions ripple = new RippleOptions();
    @OverrideName("ParticleData")
    public static class RippleOptions {
        @Slider @Format(Percent.class)
        public float opacity = 0.8F;
        public float size = 0.25F;

        @OnChange(ReloadResources.class)
        public int resolution = 16;
        @OnChange(ReloadResources.class)
        public boolean useResourcepackResolution = true;
    }

    @NoGUI
    public StreakOptions streak = new StreakOptions();
    @OverrideName("ParticleData")
    public static class StreakOptions {
        @Slider @Format(Percent.class)
        public float opacity = 0.9F;
        public float size = 0.5F;
    }

    @NoGUI
    public MistOptions mist = new MistOptions();
    @OverrideName("ParticleData")
    public static class MistOptions {
        public int lifetime = 200;
        @Slider @Format(Percent.class)
        public float opacity = 1.0F;
        public float size = 3F;
        public RenderStyle renderStyle = RenderStyle.BLENDED;
        public enum RenderStyle {
            BLENDED {
                public TextureAtlasSprite getSprite() {
                    return VersionUtil.getSprite(VersionUtil.getId("fog_translucent"));
                }
                public /*? if >=1.21.9 {*//*SingleQuadParticle.Layer*//*?} else {*/ParticleRenderType/*?}*/ getRenderType() {
                    return BlendedParticleRenderType.INSTANCE;
                }
            },
            DITHERED;
            public TextureAtlasSprite getSprite() {
                return VersionUtil.getSprite(VersionUtil.getId("fog_dithered"));
            }
            //? if >=1.21.9 {
            /*public SingleQuadParticle.Layer getRenderType() {
                return SingleQuadParticle.Layer.TRANSLUCENT;
            }
            *///?} else {
            public ParticleRenderType getRenderType() {
                return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
            }
            //?}
        }
    }
}