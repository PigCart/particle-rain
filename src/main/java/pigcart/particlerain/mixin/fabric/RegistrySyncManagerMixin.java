//? if fabric {
package pigcart.particlerain.mixin.fabric;

import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.server.MinecraftServer;
//? if >1.20.1 {
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.particlerain.config.ModConfig;

// Allows for other fabricapi users to join LAN worlds if they do not have the mod but the host player does.
@Mixin(RegistrySyncManager.class)
public abstract class RegistrySyncManagerMixin {
    // couldnt figure out how versioned resources is supposed to work so...
    //? if >1.20.1 {
    @Inject(method = "configureClient", at = @At("HEAD"), cancellable = true)
    private static void configureClient(ServerConfigurationPacketListenerImpl handler, MinecraftServer server, CallbackInfo ci) {
        if (!ModConfig.CONFIG.compat.syncRegistry) {
            ci.cancel();
        }
    }
    //?}
}
//?}