package pigcart.particlerain.mixin.render;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.particlerain.WeatherParticleSpawner;
import pigcart.particlerain.config.ModConfig;

@Mixin(WeatherEffectRenderer.class)
public class WeatherEffectRendererMixin {
    @Unique
    private int rainSoundTime;

    @Inject(method = "tickRainParticles", at = @At("HEAD"), cancellable = true)
    public void tickRainParticles(ClientLevel level, Camera camera, int ticks, ParticleStatus particleStatus, CallbackInfo ci) {
        if (!ModConfig.CONFIG.compat.tickVanillaWeather) {
            float f = level.getRainLevel(1.0F);
            if (f > 0.0F) {
                RandomSource random = RandomSource.create((long) ticks * 312987231L);
                BlockPos blockPos = BlockPos.containing(camera.getPosition());
                BlockPos blockPos2 = null;

                for (int j = 0; j < 100.0F * f * f; ++j) {
                    int k = random.nextInt(21) - 10;
                    int l = random.nextInt(21) - 10;
                    BlockPos blockPos3 = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos.offset(k, 0, l));
                    if (blockPos3.getY() > level.getMinY() && blockPos3.getY() <= blockPos.getY() + 10 && blockPos3.getY() >= blockPos.getY() - 10) {
                        blockPos2 = blockPos3.below();
                    }
                }

                if (blockPos2 != null && random.nextInt(3) < this.rainSoundTime++) {
                    this.rainSoundTime = 0;
                    if (blockPos2.getY() > blockPos.getY() + 1 && level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).getY() > Mth.floor((float) blockPos.getY())) {
                        SoundEvent sound = WeatherParticleSpawner.getBiomeSound(blockPos2, true);
                        if (sound != null)
                            level.playLocalSound(blockPos2, sound, SoundSource.WEATHER, 0.1F, 0.5F, false);
                    } else {
                        SoundEvent sound = WeatherParticleSpawner.getBiomeSound(blockPos2, false);
                        if (sound != null)
                            level.playLocalSound(blockPos2, sound, SoundSource.WEATHER, 0.2F, 1.0F, false);
                    }
                }
            }
            ci.cancel();
        }
    }

    @Inject(method = "render(Lnet/minecraft/world/level/Level;Lnet/minecraft/client/renderer/MultiBufferSource;IFLnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"), cancellable = true)
    public void render(Level level, MultiBufferSource bufferSource, int ticks, float partialTick, Vec3 cameraPosition, CallbackInfo ci) {
        if (!ModConfig.CONFIG.compat.renderVanillaWeather) {
            ci.cancel();
        }
    }
}