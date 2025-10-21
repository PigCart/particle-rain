//? if <1.21.9 {
package pigcart.particlerain.mixin.tint;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pigcart.particlerain.TextureUtil;
import pigcart.particlerain.config.ConfigManager;

import net.minecraft.client.particle.TextureSheetParticle;

@Mixin(DripParticle.class)
public abstract class DripParticleMixin {
    @Inject(method = "createWaterHangParticle", at = @At("TAIL"))
    private static void createWaterHangParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, CallbackInfoReturnable<TextureSheetParticle> cir) {
        if (ConfigManager.config.compat.waterTint) TextureUtil.applyWaterTint(cir.getReturnValue(), clientLevel, BlockPos.containing(d, e, f));
    }

    @Inject(method = "createWaterFallParticle", at = @At("TAIL"))
    private static void createWaterFallParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, CallbackInfoReturnable<TextureSheetParticle> cir) {
        if (ConfigManager.config.compat.waterTint) TextureUtil.applyWaterTint(cir.getReturnValue(), clientLevel, BlockPos.containing(d, e, f));
    }
}
//?}
