package pigcart.particlerain.config.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import pigcart.particlerain.ParticleLoader;
import pigcart.particlerain.config.ParticleData;

import java.util.ArrayList;

import static pigcart.particlerain.ParticleRain.MOD_ID;
import static pigcart.particlerain.config.gui.WidgetUtil.*;

public class MainConfigScreen extends ConfigScreen {
    public MainConfigScreen(Screen lastScreen, Object config, Object configDefault, Component title) {
        super(lastScreen, config, configDefault, title);
    }

    @Override
    public ConfigScreen getFreshScreen() {
        return new MainConfigScreen(this.lastScreen, this.config, this.configDefault, this.title);
    }

    @Override
    void resetConfig() {
        super.resetConfig();
        ParticleLoader.particles = ParticleLoader.loadPackParticles(Minecraft.getInstance().getResourceManager());
    }

    @Override
    protected void addContents() {
        super.addContents();
        // add particle toggle and edit buttons
        ParticleLoader.particles.forEach((id, data) -> {

            AbstractWidget toggleButton = WidgetUtil.getBool(MOD_ID + "." + id,
                    data.enabled,
                    (value) -> data.enabled = value
            );
            toggleButton.setWidth(BUTTON_WIDTH);

            AbstractWidget editButton = getButton(Component.translatable("selectWorld.edit").append("..."), (bttn)-> {
                // if this is a user-added particle remove it from the map in case the user wants to change its id
                if (!ParticleLoader.packParticles.containsKey(id)) {
                    ParticleLoader.particles.remove(id);
                }
                // get the object for the reset button to use
                Object defaultData;
                if (ParticleLoader.packParticles.containsKey(id)) {
                    defaultData = ParticleLoader.loadPackParticles(Minecraft.getInstance().getResourceManager()).get(id);
                } else {
                    defaultData = new ParticleData();
                }
                Minecraft.getInstance().setScreen(new ParticleEditScreen(
                        this.getFreshScreen(),
                        data,
                        defaultData,
                        Component.translatable("particlerain." + id)
                ));
            });
            ((AbstractWidgetAccess)editButton).pigcart$setOffset(toggleButton.getWidth() + 10);

            // if this is a user-added particle add remove button
            if (!ParticleLoader.packParticles.containsKey(id)) {
                editButton.setWidth(BUTTON_WIDTH - BUTTON_HEIGHT);
                AbstractWidget removeButton = getRemoveButton( (bttn) -> {
                    ParticleLoader.particles.remove(id);
                    this.refresh();
                });
                this.addRow(toggleButton, editButton, removeButton);
            } else {
                editButton.setWidth(BUTTON_WIDTH);
                this.addRow(toggleButton, editButton);
            }
        });

        final MutableComponent addButtonText = Component.translatable("particlerain.addNew");
        final AbstractWidget addButton = getButton(addButtonText, (bttn) -> {
            Object particleToAdd = new ParticleData();
            Object defaultParticle = new ParticleData();
            Minecraft.getInstance().setScreen(new ParticleEditScreen(this.getFreshScreen(), particleToAdd, defaultParticle, addButtonText));
            // user particles get added when the edit screen is removed
        });
        this.addRow(addButton);
    }

}
