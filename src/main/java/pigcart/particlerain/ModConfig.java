package pigcart.particlerain;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "particlerain")
public class ModConfig implements ConfigData {

    public int particleDensity = 200;
    public int particleStormDensity = 800;
    public int particleRadius = 25;
    public float rainDropGravity = 1.0F;
    public float snowFlakeGravity = 0.1F;
    public float snowRotationAmount = 0.1F;
    public float snowWindDampening = 3F;
    public float desertDustGravity = 0.2F;
    public boolean doRainParticles = true;
    public boolean doSnowParticles = true;
    public boolean doSandParticles = true;
    public boolean renderVanillaWeather = false;
}