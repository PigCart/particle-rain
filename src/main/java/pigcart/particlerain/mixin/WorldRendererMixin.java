package pigcart.particlerain.mixin;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    /**
     * @author pigcart
     * @reason can't be bothered to figure out a more elegant solution. this works. bye renderWeather!
     */
    @Overwrite
    private void renderWeather(LightmapTextureManager manager, float f, double d, double e, double g) {
    }
}
