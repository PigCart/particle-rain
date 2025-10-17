package pigcart.particlerain.mixin.tint;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import pigcart.particlerain.TextureUtil;
import pigcart.particlerain.config.ConfigManager;
//? if >=1.21.9 {
/*import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?} else {
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//?}

@Mixin(DripParticle.class)
public abstract class DripParticleMixin
    //? if >=1.21.9 {
        /*extends SingleQuadParticle {

    protected DripParticleMixin(ClientLevel clientLevel, double d, double e, double f, TextureAtlasSprite textureAtlasSprite) {
        super(clientLevel, d, e, f, textureAtlasSprite);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    void DripParticle(ClientLevel clientLevel, double d, double e, double f, Fluid fluid, TextureAtlasSprite textureAtlasSprite, CallbackInfo ci) {
        if (fluid == Fluids.WATER && ConfigManager.config.compat.waterTint) {
            TextureUtil.applyWaterTint(this, clientLevel, BlockPos.containing(d, e, f));
        }
    }
    *///?} else {
    {
    @Inject(method = "createWaterHangParticle", at = @At("TAIL"))
    private static void createWaterHangParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, CallbackInfoReturnable<TextureSheetParticle> cir) {
        if (ConfigManager.config.compat.waterTint) TextureUtil.applyWaterTint(cir.getReturnValue(), clientLevel, BlockPos.containing(d, e, f));
    }

    @Inject(method = "createWaterFallParticle", at = @At("TAIL"))
    private static void createWaterFallParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, CallbackInfoReturnable<TextureSheetParticle> cir) {
        if (ConfigManager.config.compat.waterTint) TextureUtil.applyWaterTint(cir.getReturnValue(), clientLevel, BlockPos.containing(d, e, f));
    }
    //?}
}
