package pigcart.particlerain.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.particlerain.ParticleRainClient;
import pigcart.particlerain.WeatherParticleSpawner;

import java.util.Random;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private int ticks;

    @Shadow
    private int rainSoundTime;

    @Inject(method = "tickRain", at = @At("HEAD"), cancellable = true)
    public void tickRain(Camera camera, CallbackInfo ci) {
        //TODO: play sound where particles are actually falling. presence footsteps but for rain drops might not be an awful idea?
        if (!ParticleRainClient.config.tickVanillaWeather) {
            float f = this.minecraft.level.getRainLevel(1.0F);
            if (f > 0.0F) {
                Random random = new Random((long) this.ticks * 312987231L);
                LevelReader level = this.minecraft.level;
                BlockPos blockPos = BlockPos.containing(camera.getPosition());
                BlockPos blockPos2 = null;

                for (int j = 0; j < 100.0F * f * f; ++j) {
                    int k = random.nextInt(21) - 10;
                    int l = random.nextInt(21) - 10;
                    BlockPos blockPos3 = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos.offset(k, 0, l));
                    if (blockPos3.getY() > level.getMinBuildHeight() && blockPos3.getY() <= blockPos.getY() + 10 && blockPos3.getY() >= blockPos.getY() - 10) {
                        blockPos2 = blockPos3.below();
                    }
                }

                if (blockPos2 != null && random.nextInt(3) < this.rainSoundTime++) {
                    this.rainSoundTime = 0;
                    if (blockPos2.getY() > blockPos.getY() + 1 && level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).getY() > Mth.floor((float) blockPos.getY())) {
                        SoundEvent sound = WeatherParticleSpawner.getBiomeSound(blockPos2, true);
                        if (sound != null)
                            this.minecraft.level.playLocalSound(blockPos2, sound, SoundSource.WEATHER, 0.1F, 0.5F, false);
                    } else {
                        SoundEvent sound = WeatherParticleSpawner.getBiomeSound(blockPos2, false);
                        if (sound != null)
                            this.minecraft.level.playLocalSound(blockPos2, sound, SoundSource.WEATHER, 0.2F, 1.0F, false);
                    }
                }
            }
            ci.cancel();
        }
    }

    @Inject(method = "renderSnowAndRain", at = @At("HEAD"), cancellable = true)
    public void renderWeather(LightTexture lightTexture, float partialTicks, double x, double y, double z, CallbackInfo ci) {
        if (!ParticleRainClient.config.renderVanillaWeather) {
            ci.cancel();
        }
    }
}
