package pigcart.particlerain.mixin.tint;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.VersionUtil;
import pigcart.particlerain.TextureUtil;
import pigcart.particlerain.config.ConfigManager;
import pigcart.particlerain.mixin.access.ParticleEngineAccessor;

@Mixin(WaterDropParticle.class)
public abstract class WaterDropParticleMixin extends TextureSheetParticleMixin {

    protected WaterDropParticleMixin(ClientLevel clientLevel, double d, double e, double f) {
        super(clientLevel, d, e, f);
    }

    @Override
    public void pickSprite(SpriteSet spriteSet, CallbackInfo ci) {
        if (ConfigManager.config.compat.waterTint) {
            try {
                ParticleEngineAccessor particleEngine = (ParticleEngineAccessor) Minecraft.getInstance().particleEngine;
                this.setSprite(particleEngine.getTextureAtlas().getSprite(VersionUtil.getId(ParticleRain.MOD_ID, "splash_" + random.nextInt(4))));
            } catch (IllegalStateException e) {
                // "Tried to lookup sprite, but atlas is not initialized" no idea what causes this. seems random. can't reproduce.
                // happens in the getSprite call when `this.texturesByName.getOrDefault(name, this.missingSprite)` returns null
                ParticleRain.LOGGER.error(e.getMessage());
                this.remove();
            }
            TextureUtil.applyWaterTint((TextureSheetParticle) (Object) this, this.level, BlockPos.containing(x, y, z));
        }
    }
}
