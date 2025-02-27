package pigcart.particlerain.mixin.tint;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.particlerain.ParticleRainClient;
import pigcart.particlerain.Util;
import pigcart.particlerain.config.ModConfig;

@Mixin(WaterDropParticle.class)
public abstract class WaterDropParticleMixin extends TextureSheetParticleMixin {

    protected WaterDropParticleMixin(ClientLevel clientLevel, double d, double e, double f) {
        super(clientLevel, d, e, f);
    }

    //TODO: whoa hey i should probably not be using override for this!
    @Override
    public void pickSprite(SpriteSet spriteSet, CallbackInfo ci) {
        if (ModConfig.CONFIG.compat.biomeTint) {
            try {
                this.setSprite(Minecraft.getInstance().particleEngine.textureAtlas.getSprite(ResourceLocation.fromNamespaceAndPath(ParticleRainClient.MOD_ID, "splash" + random.nextInt(4))));
            } catch (Exception e) {
                // guessing that this is caused by servers sending particle events while the client is reloading?
                // causing the atlas to be accessed before its initialized??
                throw new RuntimeException(e);
            }
            Util.applyWaterTint((TextureSheetParticle) (Object) this, this.level, BlockPos.containing(x, y, z));
        }
    }
}
