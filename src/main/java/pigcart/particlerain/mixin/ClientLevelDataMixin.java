package pigcart.particlerain.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.particlerain.WeatherParticleManager;

@Mixin(ClientLevel.ClientLevelData.class)
public abstract class ClientLevelDataMixin {

    @Inject(method = "setRaining", at = @At("HEAD"))
    public void hookSetRaining(boolean raining, CallbackInfo ci) {
        WeatherParticleManager.onWeatherChange(raining);
    }
}
