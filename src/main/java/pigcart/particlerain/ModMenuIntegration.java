package pigcart.particlerain;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    private static Screen create(Screen parent) {
        ParticleRainClient.previousBiomeTintOption = ParticleRainClient.config.biomeTint;
        ParticleRainClient.previousUseResolutionOption = ParticleRainClient.config.ripple.useResourcepackResolution;
        ParticleRainClient.previousResolutionOption = ParticleRainClient.config.ripple.resolution;
        return AutoConfig.getConfigScreen(ModConfig.class, parent).get();
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModMenuIntegration::create;
    }
}
