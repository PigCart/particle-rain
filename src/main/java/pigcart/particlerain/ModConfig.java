package pigcart.particlerain;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;

@Config(name = "particlerain")
public class ModConfig implements ConfigData {
    public int particleDensity = 30;
    public int particleRadius = 20;
}
