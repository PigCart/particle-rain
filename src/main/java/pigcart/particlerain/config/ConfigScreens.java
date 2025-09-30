package pigcart.particlerain.config;

import dev.isxander.yacl3.api.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.VersionUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static pigcart.particlerain.config.ConfigManager.config;
import static pigcart.particlerain.config.ConfigManager.defaultConfig;
import static pigcart.particlerain.config.YACLUtil.*;

public class ConfigScreens {

    public static Screen generateMainConfigScreen(Screen parent) {
        List<OptionGroup> groups = collectGroups(defaultConfig, config);
        groups.add(1, getParticleToggles());
        return generateScreen(getComponent("title"), groups, collectOptions(defaultConfig, config), ()-> generateMainConfigScreen(parent), parent);
    }

    static OptionGroup getParticleToggles() {
        List<Option<?>> options = new ArrayList<>();
        options.add(getScreenButtonOption(getComponent("editParticles"), "", () ->
                generateParticleListScreen(Minecraft.getInstance().screen)
        ));
        for (ConfigData.ParticleData particle : config.particles) {
            try {
                options.add(getBoolOption(new ConfigData.ParticleData(), particle, particle.getClass().getField("enabled"))
                        .name(getComponent(particle.id)).build());
            } catch (NoSuchFieldException e) {
                ParticleRain.LOGGER.error(e.getMessage());
            }
        }
        return OptionGroup.createBuilder()
                .name(getComponent("particleToggles"))
                .description(OptionDescription.of(getComponentWithFallback("particles.description")))
                .options(options)
                .build();
    }

    public static Screen generateParticleListScreen(Screen parent) {
        Collection<Option<?>> options = new ArrayList<>();
        options.add(getScreenButtonOption(getComponent("button.add"),"", () -> {
            ConfigData.ParticleData particle = new ConfigData.ParticleData();
            config.particles.add(particle);
            ConfigManager.save();
            return generateParticleEditScreen(generateParticleListScreen(parent), particle, particle);
        }));
        for (ConfigData.ParticleData opts : config.particles) {
            final ConfigData.ParticleData finalDefaultParticle = getDefaultParticle(opts);
            options.add(LabelOption.create(Component.literal("")));
            final ButtonOption editButton = getScreenButtonOption(Component.literal(opts.id), "", () -> {
                return generateParticleEditScreen(Minecraft.getInstance().screen, opts, finalDefaultParticle);
            });
            options.add(editButton);
            options.add(getScreenButtonOption(Component.translatable("selectWorld.delete").withStyle(ChatFormatting.RED), "", () -> {
                config.particles.remove(opts);
                ConfigManager.save();
                return generateParticleListScreen(parent);
            }));
        }
        return generateScreen(getComponent("editParticles"), null, options, ()-> generateParticleListScreen(parent), parent);
    }

    private static ConfigData.ParticleData getDefaultParticle(ConfigData.ParticleData particleData) {
        for (ConfigData.ParticleData defaultData : defaultConfig.particles) {
            if (particleData.id.equals(defaultData.id)) return defaultData;
        }
        return new ConfigData.ParticleData();
    }

    public static Screen generateParticleEditScreen(Screen parent, Object configObject, Object defaultObject) {
        List<OptionGroup> groups = null;
        try {
            // appends the mist/ripple/shrub/streak config options if needed
            final Field usePresetParticle = ConfigData.ParticleData.class.getField("usePresetParticle");
            usePresetParticle.setAccessible(true);
            if ((Boolean)usePresetParticle.get(configObject)) {
                final Field presetParticleField = ConfigData.ParticleData.class.getField("presetParticleId");
                presetParticleField.setAccessible(true);
                String configId = VersionUtil.parseId((String)presetParticleField.get(configObject)).getPath();
                if (ParticleRain.particleConfigIds.contains(configId)) groups = List.of(getObjectAsGroup(defaultConfig, config, ConfigData.class.getField(configId)));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return generateScreen(getComponent("category.edit"), groups, collectOptions(defaultObject, configObject), ()-> generateParticleEditScreen(parent, configObject, defaultObject), parent);
    }
}
