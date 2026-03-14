package pigcart.particlerain.config;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import org.joml.Vector3f;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.ParticleSpawner;
import pigcart.particlerain.VersionUtil;
import pigcart.particlerain.particle.CustomParticle;
import pigcart.particlerain.particle.render.BlendedParticleRenderType;
//? if >=1.21.9 {
/*import net.minecraft.client.renderer.state./^?>=26.1{^//^level.^//^?}^/QuadParticleRenderState;
 *///?} else {
import net.minecraft.client.particle.ParticleRenderType;
//?}

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static pigcart.particlerain.config.ConfigManager.config;
import static pigcart.particlerain.config.ConfigResponders.*;
import static pigcart.particlerain.config.gui.Annotations.*;

public class ParticleData {
    
    public void updateTransientVariables() {
        biomeList.populateInternalLists();
        blockList.populateInternalLists();
        setPresetParticle();
    }
    public void setPresetParticle() {
        final Optional<ParticleType<?>> optional = BuiltInRegistries.PARTICLE_TYPE.getOptional(VersionUtil.parseId(presetParticleId));
        if (optional.isEmpty()) {
            ParticleRain.LOGGER.error("Incorrect configuration: {} is not a valid particle", presetParticleId);
        } else {
            presetParticle = (ParticleOptions) optional.get();
        }
    }

    //TODO @Dropdown(SupplyParticleTypes.class)
    @OnlyVisibleIf(ParticleNotCustom.class)
    public String presetParticleId = "minecraft:flame";
    @NoGUI
    public transient ParticleOptions presetParticle = ParticleTypes.CLOUD;
    @OnChange(RefreshScreen.class)
    public Boolean usePresetParticle = false;
    @OnlyVisibleIf(ParticleIsNotDefault.class)
    public transient String id = "new_particle";
    @Label(key = "spawning")
    public Boolean enabled = true;
    @Slider(step = 0.001F)
    @Format(Percent.class)
    public Float density = 1.0F;
    public Weather weather = Weather.DURING_WEATHER;
    public ArrayList<Biome.Precipitation> precipitation = new ArrayList<>(List.of(Biome.Precipitation.RAIN));
    public Whitelist.BiomeList biomeList = new Whitelist.BiomeList(true, new ArrayList<>(1));
    public Whitelist.BlockList blockList = new Whitelist.BlockList(true, new ArrayList<>(1));
    public Boolean needsSkyAccess = true;
    public SpawnPos spawnPos = SpawnPos.SKY;
    //public Vector3f spawnVelocity = new Vector3f(0, 0, 0);
    @Label(key = "motion")
    @OnlyVisibleIf(ParticleIsCustom.class) public Float gravity = 0.1F;
    @OnlyVisibleIf(ParticleIsCustom.class) public Float windStrength = 0.1F;
    @OnlyVisibleIf(ParticleIsCustom.class) public Float stormWindStrength = 0.5F;
    @OnlyVisibleIf(ParticleIsCustom.class) public Float rotationAmount = 0F;
    @OnlyVisibleIf(ParticleIsCustom.class) public Float bounciness = 0F;
    @OnlyVisibleIf(ParticleIsCustom.class) public Integer lifetime = 3000;
    @Label(key = "appearance")
    @Slider @Format(Percent.class)
    @OnlyVisibleIf(ParticleIsCustom.class) public Float opacity = 1.0F;
    @OnlyVisibleIf(ParticleIsCustom.class) public Float size = 0.5F;
    @OnlyVisibleIf(ParticleIsCustom.class) public Boolean constantScreenSize = false;
    @OnlyVisibleIf(ParticleIsCustom.class) public RenderType renderType = RenderType.TRANSLUCENT;
    @OnlyVisibleIf(ParticleIsCustom.class) public ArrayList<String> spriteLocations = new ArrayList<>(List.of("particlerain:new_custom_particle"));
    @OnlyVisibleIf(ParticleIsCustom.class)
    @OnChange(RefreshScreen.class)
    public TintType tintType = TintType.NONE;
    @OnlyVisibleIf(ParticleIsCustomAndAlsoUsesCustomTint.class)
    public Color customTint = Color.BLACK;
    @OnlyVisibleIf(ParticleIsCustom.class) public RotationType rotationType = RotationType.COPY_CAMERA;

    public enum Weather {
        DURING_WEATHER {
            public Boolean isCurrent(ClientLevel level) {
                return level.isRaining();
            }
        },
        ONLY_DURING_NORMAL_WEATHER {
            public Boolean isCurrent(ClientLevel level) {
                return level.isRaining() && !level.isThundering();
            }
        },
        ONLY_DURING_STORMY_WEATHER {
            public Boolean isCurrent(ClientLevel level) {
                return level.isThundering();
            }
        },
        AFTER_WEATHER {
            public Boolean isCurrent(ClientLevel level) {
                return ParticleSpawner.afterWeatherTicksLeft > 0;
            }
        },
        CLEAR {
            public Boolean isCurrent(ClientLevel level) {
                return !level.isRaining();
            }
        },
        ALWAYS;
        public Boolean isCurrent(ClientLevel level) {
            return true;
        }
    }

    public enum SpawnPos {
        SKY,
        BLOCK_SIDES,
        BLOCK_BOTTOM,
        BLOCK_TOP,
        WORLD_SURFACE,
        UNDERGROUND
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
                final Color fogColor = VersionUtil.getFogColor(level, pos);
                float rCol = Mth.lerp(config.compat.tintMix, waterColor.getRed(), fogColor.getRed());
                float gCol = Mth.lerp(config.compat.tintMix, waterColor.getGreen(), fogColor.getGreen());
                float bCol = Mth.lerp(config.compat.tintMix, waterColor.getBlue(), fogColor.getBlue());
                p.setColor(rCol / 255F, gCol / 255F, bCol / 255F);
            }
        },
        FOG {
            public void applyTint(SingleQuadParticle p, ClientLevel level, BlockPos pos, ParticleData opts) {
                Color color = VersionUtil.getFogColor(level, pos).darker();
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
}
