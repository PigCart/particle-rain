//? if fabric {
package pigcart.particlerain.loaders.fabric;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import pigcart.particlerain.ParticleRainClient;

public class FabricEntrypoint implements ClientModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Hello from FabricEntrypoint!");
        ParticleRainClient.onInitializeClient();
    }
}
//?}