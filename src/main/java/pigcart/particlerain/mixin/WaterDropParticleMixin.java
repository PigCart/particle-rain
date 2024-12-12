package pigcart.particlerain.mixin;

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

@Mixin(WaterDropParticle.class)
public abstract class WaterDropParticleMixin extends TextureSheetParticleMixin {

    protected WaterDropParticleMixin(ClientLevel clientLevel, double d, double e, double f) {
        super(clientLevel, d, e, f);
    }

    @Override
    public void pickSprite(SpriteSet spriteSet, CallbackInfo ci) {
        if (ParticleRainClient.config.biomeTint) {
            this.setSprite(Minecraft.getInstance().particleEngine.textureAtlas.getSprite(ResourceLocation.fromNamespaceAndPath(ParticleRainClient.MOD_ID, "splash" + random.nextInt(4))));
            ParticleRainClient.applyWaterTint((TextureSheetParticle) (Object) this, this.level, BlockPos.containing(x, y, z));
        }
    }
}
