package pigcart.particlerain.mixin.tint;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.VersionUtil;
import pigcart.particlerain.TextureUtil;
import pigcart.particlerain.config.ConfigManager;
//? if >=1.21.9 {
/*import net.minecraft.client.particle.SingleQuadParticle;
*///?} else {
import net.minecraft.client.particle.TextureSheetParticle;
//?}

@Mixin(WaterDropParticle.class)
//? if >=1.21.9 {
/*public abstract class WaterDropParticleMixin extends SingleQuadParticle {

    protected WaterDropParticleMixin(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void pickSprite(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite, CallbackInfo ci) {
*///?} else {
public abstract class WaterDropParticleMixin extends TextureSheetParticleMixin {

    protected WaterDropParticleMixin(ClientLevel clientLevel, double d, double e, double f) {
        super(clientLevel, d, e, f);
    }

    @Override
    public void pickSprite(SpriteSet spriteSet, CallbackInfo ci) {
//?}

        if (ConfigManager.config.compat.waterTint) {
            try {
                this.setSprite(VersionUtil.getSprite(VersionUtil.getId("splash_" + random.nextInt(4))));
            } catch (IllegalStateException e) {
                // "Tried to lookup sprite, but atlas is not initialized" no idea what causes this. seems random. can't reproduce.
                // happens in the getSprite call when `this.texturesByName.getOrDefault(name, this.missingSprite)` returns null
                ParticleRain.LOGGER.error(e.getMessage());
                this.remove();
            }
            TextureUtil.applyWaterTint(this, this.level, BlockPos.containing(x, y, z));
        }
    }
}
