package pigcart.particlerain.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.particlerain.ParticleRainClient;

import java.awt.*;

@Mixin(WaterDropParticle.class)
public abstract class WaterDropParticleMixin extends TextureSheetParticleMixin {

    protected WaterDropParticleMixin(ClientLevel clientLevel, double d, double e, double f) {
        super(clientLevel, d, e, f);
    }

    @Override
    public void pickSprite(SpriteSet spriteSet, CallbackInfo ci) {
        if (ParticleRainClient.config.biomeTint) {
            this.setSprite(Minecraft.getInstance().particleEngine.textureAtlas.getSprite(ResourceLocation.fromNamespaceAndPath(ParticleRainClient.MOD_ID, "splash" + random.nextInt(4))));
            BlockPos blockPos = BlockPos.containing(x, y, z);
            final Color waterColor = new Color(BiomeColors.getAverageWaterColor(level, blockPos));
            final Color fogColor = new Color(this.level.getBiome(blockPos).value().getFogColor());
            this.rCol = (Mth.lerp(ParticleRainClient.config.rain.mix / 100F, waterColor.getRed(), fogColor.getRed()) / 255F);
            this.gCol = (Mth.lerp(ParticleRainClient.config.rain.mix / 100F, waterColor.getGreen(), fogColor.getGreen()) / 255F);
            this.bCol = (Mth.lerp(ParticleRainClient.config.rain.mix / 100F, waterColor.getBlue(), fogColor.getBlue()) / 255F);
        }
    }
}
