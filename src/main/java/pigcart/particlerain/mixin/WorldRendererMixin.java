package pigcart.particlerain.mixin;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(at = @At("HEAD"), method = "renderWeather", cancellable = true)
    private void renderWeather(LightmapTextureManager manager, float f, double d, double e, double g, CallbackInfo ci) {
        ci.cancel();
    }
}
