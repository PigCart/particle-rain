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
import pigcart.particlerain.TextureUtil;
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
            } catch (IllegalStateException e) {
                // "Tried to lookup sprite, but atlas is not initialized" no idea what causes this. seems random. can't reproduce.
                // happens in the getSprite call when `this.texturesByName.getOrDefault(name, this.missingSprite)` returns null
                System.out.println(e.getMessage());
            }
            TextureUtil.applyWaterTint((TextureSheetParticle) (Object) this, this.level, BlockPos.containing(x, y, z));
        }
    }
}
