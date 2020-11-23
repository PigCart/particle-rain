package pigcart.particlerain;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class ParticleRain implements ModInitializer {
    public static Identifier id(String path) {
        return new Identifier("particlerain", path);
    }

    public static ModConfig config;

    @Override
    public void onInitialize() {
        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }
}
