package pigcart.particlerain.config.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import pigcart.particlerain.ParticleLoader;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.config.ConfigData;
import pigcart.particlerain.config.ConfigManager;
import pigcart.particlerain.config.ParticleData;

import java.lang.reflect.Field;

import static pigcart.particlerain.config.gui.WidgetUtil.getButton;

public class ParticleEditScreen extends ConfigScreen {

    public ParticleEditScreen(Screen lastScreen, Object config, Object configDefault, Component title) {
        super(lastScreen, config, configDefault, title);
    }

    @Override
    public ConfigScreen getFreshScreen() {
        return new ParticleEditScreen(this.lastScreen, this.config, this.configDefault, this.title);
    }

    @Override
    public void removed() {
        // if we just edited a user-added particle, add it back to the particle map
        ParticleData data = (ParticleData) this.config;
        if (!ParticleLoader.packParticles.containsKey(data.id)) {
            ParticleLoader.particles.put(data.id, data);
        }
    }

    @Override
    protected void addContents() {
        super.addContents();
        // add button to access legacy options if editing a non CustomParticle
        ParticleData data = (ParticleData) this.config;
        if (data.usePresetParticle && ParticleRain.legacyParticleIds.contains(data.id)) {
            ParticleData particleData = (ParticleData) this.config;
            try {
                final Field field = ConfigData.class.getField(particleData.id);
                field.setAccessible(true);
                final Object bespokeParticleConfig = field.get(ConfigManager.config);
                final Object defaultConfig = field.get(ConfigManager.getDefaultConfig());
                AbstractWidget button = getButton(Component.translatable("particlerain.appearance"),
                        (bttn) -> Minecraft.getInstance().setScreen(new ConfigScreen(
                                this,
                                bespokeParticleConfig,
                                defaultConfig,
                                Component.translatable("particlerain.appearance")
                        )));
                this.list.addRow(button);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
