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
import pigcart.particlerain.config.ConfigManager;
import pigcart.particlerain.config.ConfigScreens;

@Mixin(YACLScreen.class)
public abstract class YACLScreenMixin {

    @Shadow(remap = false) @Final public YetAnotherConfigLib config;

    @Shadow(remap = false) @Final private Screen parent;

    @Inject(method = "cancelOrReset", at = @At("TAIL"), remap = false)
    public void cancelOrReset(CallbackInfo ci) {
        if (this.config.title().getContents().getClass().equals(TranslatableContents.class)) {
            final String key = ((TranslatableContents) this.config.title().getContents()).getKey();
            if (key.equals("particlerain.editParticles") || key.equals("particlerain.title")) {
                ConfigManager.config.mist = ConfigManager.defaultConfig.mist;
                ConfigManager.config.streak = ConfigManager.defaultConfig.streak;
                ConfigManager.config.shrub = ConfigManager.defaultConfig.shrub;
                ConfigManager.config.ripple = ConfigManager.defaultConfig.ripple;
                ConfigManager.config.particles = ConfigManager.defaultConfig.particles;
                ParticleRain.LOGGER.info("Particles reset");
                if (key.equals("particlerain.editParticles")) {
                    Minecraft.getInstance().setScreen(ConfigScreens.generateParticleListScreen(this.parent));
                } else {
                    Minecraft.getInstance().setScreen(ConfigScreens.generateMainConfigScreen(this.parent));
                }
            }
        }
    }

    // workaround for https://github.com/isXander/YetAnotherConfigLib/issues/187
    @Inject( //yacl's mappings are borked
            //? if forge {
            /*method = "m_7379_", at = @At("HEAD"), remap = false)
            *///?} else {
            method = "onClose", at = @At("HEAD"))
            //?}
    public void runSaveFunction(CallbackInfo ci) {
        if (this.config.title().getContents().getClass().equals(TranslatableContents.class)) {
            if (((TranslatableContents) this.config.title().getContents()).getKey().startsWith(ParticleRain.MOD_ID)) {
                this.config.saveFunction().run();
            }
        }
    }
}
