package pigcart.particlerain;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

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
}