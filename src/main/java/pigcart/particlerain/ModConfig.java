package pigcart.particlerain;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "particlerain")
public class ModConfig implements ConfigData {
    public int particleDensity = 50;
    public int particleStormDensity = 150;
    public int particleRadius = 25;
    public float rainDropGravity = 0.7F;
    public float snowFlakeGravity = 0.15F;
    public float desertDustGravity = 0.2F;
    public boolean renderVanillaWeather = false;
    public boolean doRainParticles = true;
    public boolean doSnowParticles = true;
    public boolean doSandParticles = true;
    @ConfigEntry.Category(value = "particleColor")
    public float rainRed = 0;
    @ConfigEntry.Category(value = "particleColor")
    public float rainGreen = 0;
    @ConfigEntry.Category(value = "particleColor")
    public float rainBlue = 1;
    @ConfigEntry.Category(value = "particleColor")
    public float snowRed = 1;
    @ConfigEntry.Category(value = "particleColor")
    public float snowGreen = 1;
    @ConfigEntry.Category(value = "particleColor")
    public float snowBlue = 1;
    @ConfigEntry.Category(value = "particleColor")
    public float desertDustRed = 0.9F;
    @ConfigEntry.Category(value = "particleColor")
    public float desertDustGreen = 0.8F;
    @ConfigEntry.Category(value = "particleColor")
    public float desertDustBlue = 0.6F;
    @ConfigEntry.Category(value = "particleColor")
    public float mesaDustRed = 0.8F;
    @ConfigEntry.Category(value = "particleColor")
    public float mesaDustGreen = 0.4F;
    @ConfigEntry.Category(value = "particleColor")
    public float mesaDustBlue = 0;
}