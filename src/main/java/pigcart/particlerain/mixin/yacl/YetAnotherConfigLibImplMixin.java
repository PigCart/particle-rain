package pigcart.particlerain.mixin.yacl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.isxander.yacl3.impl.YetAnotherConfigLibImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import pigcart.particlerain.ParticleRain;

@SuppressWarnings("UnstableApiUsage")
@Mixin(YetAnotherConfigLibImpl.class)
public abstract class YetAnotherConfigLibImplMixin {

    @Shadow(remap = false) public abstract Component title();

    @WrapOperation(method = "generateScreen", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;)V"), remap = false)
    public void stopLogSpam(Logger instance, String string, Operation<Void> original) {
        if (this.title().getContents().getClass().equals(TranslatableContents.class)) {
            if (!((TranslatableContents) this.title().getContents()).getKey().startsWith(ParticleRain.MOD_ID)) {
                original.call(instance, string);
            }
        }
    }
}
