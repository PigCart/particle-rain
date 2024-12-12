package pigcart.particlerain;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "particlerain")
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip @ConfigEntry.Gui.PrefixText
    public int maxParticleAmount = 1500;
    @ConfigEntry.Gui.Tooltip
    public int particleDensity = 80;
    @ConfigEntry.Gui.Tooltip
    public int particleStormDensity = 200;
    @ConfigEntry.Gui.Tooltip
    public int particleRadius = 25;

    @ConfigEntry.Gui.PrefixText
    public boolean doRainParticles = true;
    public boolean doSplashParticles = true;
    public boolean doRippleParticles = true;
    public boolean doStreakParticles = true;
    public boolean doSnowParticles = true;
    public boolean doSandParticles = true;
    public boolean doShrubParticles = true;
    public boolean doFogParticles = false;
    public boolean doGroundFogParticles = true;
    //TODO: sound options

    @ConfigEntry.Gui.CollapsibleObject
    public RainOptions rain = new RainOptions();
    public static class RainOptions {
        @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
        public int density = 100;
        public float gravity = 1.0F;
        public float windStrength = 0.3F;
        public float stormWindStrength = 0.5F;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 100) @ConfigEntry.Gui.Tooltip
        public int opacity = 100;
        public int splashDensity = 5;
        public float size = 2F;
    }
    @ConfigEntry.Gui.CollapsibleObject
    public SnowOptions snow = new SnowOptions();
    public static class SnowOptions {
        @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
        public int density = 40;
        public float gravity = 0.1F;
        public float rotationAmount = 0.03F;
        public float stormRotationAmount = 0.05F;
        public float windStrength = 0.5F;
        public float stormWindStrength = 2F;
        public float size = 2F;
    }
    @ConfigEntry.Gui.CollapsibleObject
    public SandOptions sand = new SandOptions();
    public static class SandOptions {
        @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
        public int density = 80;
        public float gravity = 0.2F;
        public float windStrength = 0.3F;
        public float moteSize = 0.1F;
        public float size = 2F;
        public boolean spawnOnGround = true;
        public String matchIds = "sand";
    }
    @ConfigEntry.Gui.CollapsibleObject
    public ShrubOptions shrub = new ShrubOptions();
    public static class ShrubOptions {
        @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
        public int density = 2;
        public float gravity = 0.2F;
        public float rotationAmount = 0.2F;
        public float bounciness = 0.15F;
    }
    @ConfigEntry.Gui.CollapsibleObject
    public FogOptions fog = new FogOptions();
    public static class FogOptions {
        @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
        public int density = 20;
        public float gravity = 0.2F;
        public float size = 0.5F;
    }
    @ConfigEntry.Gui.CollapsibleObject
    public GroundFogOptions groundFog = new GroundFogOptions();
    public static class GroundFogOptions {
        @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
        public int density = 20;
        public int spawnHeight = 64;
        public float size = 8F;
    }
    @ConfigEntry.Gui.PrefixText
    public boolean renderVanillaWeather = false;
    public boolean tickVanillaWeather = false;
    public boolean biomeTint = true;
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100) @ConfigEntry.Gui.Tooltip
    public int tintMix = 50;
    public boolean alwaysRaining = false;
    public boolean yLevelWindAdjustment = true;
    @ConfigEntry.Gui.Tooltip
    public boolean syncRegistry = true;
}