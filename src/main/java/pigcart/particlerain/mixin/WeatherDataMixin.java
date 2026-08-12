package pigcart.particlerain.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.ParticleSpawner;
//? >=26.1 {
/*import net.minecraft.world.level.saveddata.WeatherData;
 *///?} else {
import net.minecraft.client.multiplayer.ClientLevel;
//?}

//? >=26.1 {
/*@Mixin(WeatherData.class)
*///?} else {
@Mixin(ClientLevel.ClientLevelData.class)
//?}
public abstract class WeatherDataMixin {

    @Shadow
    public abstract boolean isRaining();

    @Inject(method = "setRaining", at = @At("HEAD"))
    public void hookSetRaining(boolean raining, CallbackInfo ci) {
        if (this.isRaining() != raining) ParticleSpawner.onWeatherChange(raining);
    }
}
