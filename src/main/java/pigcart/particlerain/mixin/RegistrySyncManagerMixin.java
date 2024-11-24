package pigcart.particlerain.mixin;

import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.particlerain.ParticleRainClient;

// Allows for other fabricapi users to join LAN worlds if they do not have the mod but the host player does.
@Mixin(RegistrySyncManager.class)
public abstract class RegistrySyncManagerMixin {

    @Inject(method = "configureClient", at = @At("HEAD"), cancellable = true)
    private static void configureClient(ServerConfigurationPacketListenerImpl handler, MinecraftServer server, CallbackInfo ci) {
        if (!ParticleRainClient.config.syncRegistry) {
            ci.cancel();
        }
    }

}
