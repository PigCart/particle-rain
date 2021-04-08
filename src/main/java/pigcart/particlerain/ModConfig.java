package pigcart.particlerain;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "particlerain")
public class ModConfig implements ConfigData {
    public int particleDensity = 50;
    public int particleRadius = 20;
}
