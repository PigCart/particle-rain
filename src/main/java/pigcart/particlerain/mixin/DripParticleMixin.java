package pigcart.particlerain.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pigcart.particlerain.ParticleRainClient;

import java.awt.*;

@Mixin(DripParticle.class)
public abstract class DripParticleMixin {

    @Inject(method = "createWaterHangParticle", at = @At("TAIL"))
    private static void createWaterHangParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, CallbackInfoReturnable<TextureSheetParticle> cir) {
        TextureSheetParticle particle = cir.getReturnValue();
        BlockPos blockPos = BlockPos.containing(d, e, f);
        final Color waterColor = new Color(BiomeColors.getAverageWaterColor(clientLevel, blockPos));
        final Color fogColor = new Color(clientLevel.getBiome(blockPos).value().getFogColor());
        float rCol = (Mth.lerp(ParticleRainClient.config.rain.mix / 100F, waterColor.getRed(), fogColor.getRed()) / 255F);
        float gCol = (Mth.lerp(ParticleRainClient.config.rain.mix / 100F, waterColor.getGreen(), fogColor.getGreen()) / 255F);
        float bCol = (Mth.lerp(ParticleRainClient.config.rain.mix / 100F, waterColor.getBlue(), fogColor.getBlue()) / 255F);
        particle.setColor(rCol, gCol, bCol);
    }

    @Inject(method = "createWaterFallParticle", at = @At("TAIL"))
    private static void createWaterFallParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, CallbackInfoReturnable<TextureSheetParticle> cir) {
        TextureSheetParticle particle = cir.getReturnValue();
        BlockPos blockPos = BlockPos.containing(d, e, f);
        final Color waterColor = new Color(BiomeColors.getAverageWaterColor(clientLevel, blockPos));
        final Color fogColor = new Color(clientLevel.getBiome(blockPos).value().getFogColor());
        float rCol = (Mth.lerp(ParticleRainClient.config.rain.mix / 100F, waterColor.getRed(), fogColor.getRed()) / 255F);
        float gCol = (Mth.lerp(ParticleRainClient.config.rain.mix / 100F, waterColor.getGreen(), fogColor.getGreen()) / 255F);
        float bCol = (Mth.lerp(ParticleRainClient.config.rain.mix / 100F, waterColor.getBlue(), fogColor.getBlue()) / 255F);
        particle.setColor(rCol, gCol, bCol);
    }
}
