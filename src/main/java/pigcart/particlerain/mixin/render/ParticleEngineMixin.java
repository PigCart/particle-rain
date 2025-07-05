package pigcart.particlerain.mixin.render;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.particlerain.WeatherParticleManager;
import pigcart.particlerain.particle.render.BlendedParticleRenderType;

import java.util.List;

// support custom particle render types
// https://github.com/FabricMC/fabric/issues/887
// https://github.com/VazkiiMods/Botania/blob/1.20.x/Fabric/src/main/java/vazkii/botania/fabric/mixin/client/ParticleEngineFabricMixin.java

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    @Mutable
    @Final
    @Shadow
    private static List<ParticleRenderType> RENDER_ORDER;

    @Inject(at = @At("RETURN"), method = "<clinit>")
    private static void addTypes(CallbackInfo ci) {
        RENDER_ORDER = ImmutableList.<ParticleRenderType>builder().addAll(RENDER_ORDER)
                .add(BlendedParticleRenderType.INSTANCE)
                .build();
    }

    @Inject(at = @At("HEAD"), method = "clearParticles")
    private void clearParticles(CallbackInfo ci) {
        WeatherParticleManager.resetParticleCount();
    }
}
