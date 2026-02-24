package pigcart.particlerain.config;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import pigcart.particlerain.VersionUtil;
import pigcart.particlerain.particle.render.BlendedParticleRenderType;
import pigcart.particlerain.config.gui.Annotations.*;
//? if >=1.21.9 {
/*import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.client.particle.SingleQuadParticle;
*///?} else {
import net.minecraft.client.particle.ParticleRenderType;
//?}


import java.util.HashMap;
import java.util.Map;

import static pigcart.particlerain.config.ConfigResponders.*;

public class ConfigData {
    @NoGUI public byte configVersion = 5;

    public PerformanceOptions perf = new PerformanceOptions();
    public static class PerformanceOptions {
        @OnChange(ClearParticles.class)
        public int maxParticleAmount = 1500;
        public int particleDensity = 100;
        public int particleStormDensity = 200;
        public int particleDistance = 16;
        public int surfaceRange = 64;
    }

    public SoundOptions sound = new SoundOptions();
    public static class SoundOptions {
        @Slider @Format(PercentOrOff.class) public float rainVolume = 0.2F;
        @Slider @Format(PercentOrOff.class) public float snowVolume = 0.1F;
        @Slider @Format(PercentOrOff.class) public float windVolume = 0.1F;
        @Slider @Format(PercentOrOff.class) public float blockVolume = 0.07F;
    }

    public WindOptions wind = new WindOptions();
    public static class WindOptions {
        public float strength = 0.4F;
        public float strengthVariance = 0.3F;
        public float gustFrequency = 0.2F;
        public float modulationSpeed = 0.04F;
        public boolean yLevelAdjustment = true;
    }

    public CompatibilityOptions compat = new CompatibilityOptions();
    public static class CompatibilityOptions {
        public boolean renderDefaultWeather = false;
        public boolean doDefaultSplashing = false;
        @OnChange(ReloadResources.class)
        public boolean waterTint = true;
        @Slider @Format(Percent.class)
        public float tintMix = 0.6F;
        @NoGUI
        public boolean shaderpackTint = true; //TODO
        public boolean syncRegistries = true;
        public boolean crossBiomeBorder = false;
        public boolean useHeightmapTemp = true;
        public boolean doSpawnHeightLimit = false;
        @Format(ZeroIsAutomatic.class)
        public int spawnHeightLimit = 0;
    }

    @NoGUI
    public ShrubOptions shrub = new ShrubOptions();
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
    public static class StreakOptions {
        @Slider @Format(Percent.class)
        public float opacity = 0.9F;
        public float size = 0.5F;
    }

    @NoGUI
    public MistOptions mist = new MistOptions();
    public static class MistOptions {
        public int lifetime = 250;
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