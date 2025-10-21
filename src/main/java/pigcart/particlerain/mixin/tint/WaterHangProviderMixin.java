//? if >=1.21.9 {
/*package pigcart.particlerain.mixin.tint;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pigcart.particlerain.TextureUtil;
import pigcart.particlerain.config.ConfigManager;

@Mixin(DripParticle.WaterHangProvider.class)
public abstract class WaterHangProviderMixin {
    @Inject(method = "createParticle(Lnet/minecraft/core/particles/SimpleParticleType;Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/util/RandomSource;)Lnet/minecraft/client/particle/Particle;", at = @At("TAIL"))
    public void createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random, CallbackInfoReturnable<Particle> cir) {
        if (ConfigManager.config.compat.waterTint) TextureUtil.applyWaterTint((SingleQuadParticle) cir.getReturnValue(), level, BlockPos.containing(x, y, z));
    }
}
*///?}
