package pigcart.particlerain.mixin.yacl;

import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.config.ConfigScreens;
import pigcart.particlerain.config.ModConfig;

@Mixin(YACLScreen.class)
public abstract class YACLScreenMixin {

    @Shadow(remap = false) @Final public YetAnotherConfigLib config;

    @Shadow @Final private Screen parent;

    // workaround for screen that technically doesn't have any options to reset
    @Inject(method = "cancelOrReset", at = @At("TAIL"), remap = false)
    public void resetParticlesList(CallbackInfo ci) {
        if (this.config.title().getContents().getClass().equals(TranslatableContents.class)) {
            final String key = ((TranslatableContents) this.config.title().getContents()).getKey();
            if (key.equals("particlerain.editParticles") || key.equals("particlerain.title")) {
                ModConfig.CONFIG.customParticles = ModConfig.DEFAULT.customParticles;
                if (key.equals("particlerain.editParticles")) {
                    Minecraft.getInstance().setScreen(ConfigScreens.generateParticleListScreen(this.parent));
                } else {
                    Minecraft.getInstance().setScreen(ConfigScreens.generateMainConfigScreen(this.parent));
                }
            }
        }
        ParticleRain.LOGGER.info("Particles reset");
    }

    // workaround for https://github.com/isXander/YetAnotherConfigLib/issues/187
    @Inject(method = "onClose", at = @At("TAIL"), remap = false)
    public void runSaveFunction(CallbackInfo ci) {
        if (this.config.title().getContents().getClass().equals(TranslatableContents.class)) {
            if (((TranslatableContents) this.config.title().getContents()).getKey().startsWith(ParticleRain.MOD_ID)) {
                this.config.saveFunction().run();
            }
        }
    }
}
