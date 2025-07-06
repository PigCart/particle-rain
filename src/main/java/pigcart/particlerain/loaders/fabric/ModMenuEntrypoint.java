//? if fabric {
package pigcart.particlerain.loaders.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import pigcart.particlerain.config.ConfigScreens;

public class ModMenuEntrypoint implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreens::generateMainConfigScreen;
    }

}
//?}