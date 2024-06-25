package pigcart.particlerain.mixin;

import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pigcart.particlerain.ParticleRainClient;

import java.util.Objects;

// Allows for other fabricapi users to join LAN worlds if they do not have the mod but the host player does.
@Mixin(RegistrySyncManager.class)
public abstract class RegistrySyncManagerMixin {

    @Redirect(method = "createAndPopulateRegistryMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;getKey(Ljava/lang/Object;)Lnet/minecraft/resources/ResourceLocation;"))
    private static @Nullable <T> ResourceLocation skipParticles(Registry<T> instance, T t) {
        final ResourceLocation id = instance.getKey(t);
        if (Objects.equals(id.getNamespace(), ParticleRainClient.MOD_ID)) {
            return null;
        } else {
            return id;
        }
    }

}
