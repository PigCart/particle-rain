package pigcart.particlerain;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "particlerain")
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public int particleDensity = 200;
    @ConfigEntry.Gui.Tooltip
    public int particleStormDensity = 400;
    @ConfigEntry.Gui.Tooltip
    public int maxParticleAmount = 3000;
    @ConfigEntry.Gui.Tooltip
    public int particleRadius = 30;

    public boolean doRainParticles = true;
    @ConfigEntry.Gui.CollapsibleObject
    public RainOptions rain = new RainOptions();
    public static class RainOptions {
        @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
        public int density = 50;
        public float gravity = 1.0F;
        public float windStrength = 0.5F;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
        public int lod = 80;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 100) @ConfigEntry.Gui.Tooltip
        public int mix = 70;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 100) @ConfigEntry.Gui.Tooltip
        public int opacity = 100;
        public float dropSize = 0.5F;
        public float sheetSize = 2F;
    }
    public boolean doSnowParticles = true;
    @ConfigEntry.Gui.CollapsibleObject
    public SnowOptions snow = new SnowOptions();
    public static class SnowOptions {
        @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
        public int density = 30;
        public float gravity = 0.1F;
        public float rotationAmount = 0.03F;
        public float windStrength = 0.1F;
        public float flakeSize = 0.1F;
        public float sheetSize = 2F;
    }
    public boolean doSandParticles = true;
    @ConfigEntry.Gui.CollapsibleObject
    public SandOptions sand = new SandOptions();
    public static class SandOptions {
        @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
        public int density = 90;
        public float gravity = 0.2F;
        public float windStrength = 0.3F;
        public float moteSize = 0.1F;
        public float sheetSize = 1.5F;
    }
    public boolean doShrubParticles = true;
    @ConfigEntry.Gui.CollapsibleObject
    public ShrubOptions shrub = new ShrubOptions();
    public static class ShrubOptions {
        @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
        public int density = 1;
        public float gravity = 0.1F;
        public float rotationAmount = 0.2F;
        public float bounciness = 0.1F;
    }
    public boolean doFogParticles = false;
    @ConfigEntry.Gui.CollapsibleObject
    public FogOptions fog = new FogOptions();
    public static class FogOptions {
        @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
        public int density = 20;
        public float gravity = 0.2F;
        public float size = 0.5F;
    }
    public boolean renderVanillaWeather = false;
    public boolean tickVanillaWeather = false;
    public boolean alwaysRaining = false;
}