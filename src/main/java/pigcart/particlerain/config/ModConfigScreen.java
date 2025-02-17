package pigcart.particlerain.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.net.URI;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class ModConfigScreen {
    public static Screen generateConfigScreen(Screen screen) {
        return YetAnotherConfigLib.createBuilder()
                .title(getComponent("title"))
                .category(ConfigCategory.createBuilder()
                        .name(getComponent("category.general"))
                        .group(getGroup(ModConfig.DEFAULT.perf, ModConfig.INSTANCE.perf))
                        .group(getGroup(ModConfig.DEFAULT.effect, ModConfig.INSTANCE.effect))
                        .group(getGroup(ModConfig.DEFAULT.sound, ModConfig.INSTANCE.sound))
                        .group(getGroup(ModConfig.DEFAULT.compat, ModConfig.INSTANCE.compat))
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(getComponent("category.effects"))
                        .group(getGroup(ModConfig.DEFAULT.rain, ModConfig.INSTANCE.rain))
                        .group(getGroup(ModConfig.DEFAULT.snow, ModConfig.INSTANCE.snow))
                        .group(getGroup(ModConfig.DEFAULT.dust, ModConfig.INSTANCE.dust))
                        .group(getGroup(ModConfig.DEFAULT.shrub, ModConfig.INSTANCE.shrub))
                        .group(getGroup(ModConfig.DEFAULT.ripple, ModConfig.INSTANCE.ripple))
                        .group(getGroup(ModConfig.DEFAULT.fog, ModConfig.INSTANCE.fog))
                        .group(getGroup(ModConfig.DEFAULT.groundFog, ModConfig.INSTANCE.groundFog))
                        .build())
                .category(getCategory("category.SpawnOptions", ModConfig.DEFAULT.spawn, ModConfig.INSTANCE.spawn))
                .save(ModConfig::saveConfig)
                .build()
                .generateScreen(screen);
    }
    static List<OptionGroup> optionGroups = new ArrayList<>();
    private static ConfigCategory getCategory(String translationKey, Object defaultGroup, Object group) {
        optionGroups.clear();
        return ConfigCategory.createBuilder()
                .name(getComponent(translationKey))
                .options(getOptions(defaultGroup, group))
                .groups(optionGroups)
                .build();
    }

    private static <T> OptionGroup getGroup(Object defaultGroup, Object group) {
        String groupName = group.getClass().getSimpleName();
        return OptionGroup.createBuilder()
                .name(getComponent(groupName))
                .description(OptionDescription.of(getComponentWithFallback(groupName + ".description")))
                .options(getOptions(defaultGroup, group))
                .build();
    }

    private static List<Option<?>> getOptions(Object defaultGroup, Object group) {
        List<Option<?>> options = new ArrayList<>();
        Field[] fields = group.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            final Class<?> type = field.getType();
            if (type.equals(boolean.class)) {
                options.add(getBoolOption(defaultGroup, group, field));
            } else if (type.equals(float.class)) {
                if (field.getDeclaredAnnotation(ModConfig.Percentage.class) != null) {
                    options.add(getPercentOption(defaultGroup, group, field));
                } else {
                    options.add(getFloatOption(defaultGroup, group, field));
                }
            } else if (type.equals(int.class)) {
                options.add(getIntOption(defaultGroup, group, field));
            } else if (type.equals(ModConfig.Precipitation.class)) {
                options.add(getPrecipitationOption(defaultGroup, group, field));
            } else if (type.equals(List.class)) {
                optionGroups.add(getStringListOption(defaultGroup, group, field));
            } else if (type.equals(URI.class)) {
                options.add(getLinkButtonOption(group, field));
            } else {
                System.out.println("Unable to create option for field " + field.getName());
            }
        }
        return options;
    }
    private static ButtonOption getLinkButtonOption(Object group, Field field) {
        String groupName = group.getClass().getSimpleName();
        if (group.getClass().getAnnotation(ModConfig.OverrideName.class) != null) {
            groupName = group.getClass().getAnnotation(ModConfig.OverrideName.class).newName();
        }
        final String fieldName = field.getName();
        try {
            return ButtonOption.createBuilder()
                    .name(getComponent(groupName + "." + fieldName))
                    .description(OptionDescription.of(Component.literal(field.get(group).toString())))
                    .text(CommonComponents.GUI_OPEN_IN_BROWSER)
                    .action(((yaclScreen, buttonOption) -> {
                        Minecraft minecraft = Minecraft.getInstance();
                        try {
                            minecraft.setScreen(new ConfirmLinkScreen((result) -> {
                                    try {
                                        if (result) Util.getPlatform().openUri((URI) field.get(group));
                                    } catch (IllegalAccessException e) {
                                        throw new RuntimeException(e);
                                    }
                                minecraft.setScreen(yaclScreen);
                            }, field.get(group).toString(), true
                            ));
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    })).build();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    private static Option<ModConfig.Precipitation> getPrecipitationOption(Object defaultGroup, Object group, Field field) {
        return ModConfigScreen.<ModConfig.Precipitation>getOptionBuilder(defaultGroup, group, field)
                .controller(opt -> EnumControllerBuilder.create(opt).enumClass(ModConfig.Precipitation.class))
                .build();
    }
    private static ListOption<String> getStringListOption(Object defaultGroup, Object group, Field field) {
        return ModConfigScreen.<String>getListOptionBuilder(defaultGroup, group, field)
                .controller(StringControllerBuilder::create)
                .initial("")
                .build();
    }
    private static Option<Boolean> getBoolOption(Object defaultGroup, Object group, Field field) {
        return ModConfigScreen.<Boolean>getOptionBuilder(defaultGroup, group, field)
                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                .build();
    }
    private static Option<Float> getFloatOption(Object defaultGroup, Object group, Field field) {
        return ModConfigScreen.<Float>getOptionBuilder(defaultGroup, group, field)
                .controller(opt -> FloatFieldControllerBuilder.create(opt)
                        .formatValue(val -> Component.literal(val.toString())))
                .build();
    }
    private static Option<Float> getPercentOption(Object defaultGroup, Object group, Field field) {
        return ModConfigScreen.<Float>getOptionBuilder(defaultGroup, group, field)
                .controller(opt -> FloatSliderControllerBuilder.create(opt)
                        .range(0f, 1f)
                        .step(0.01f)
                        .formatValue(val -> Component.literal(NumberFormat.getPercentInstance().format(val))))
                .build();
    }
    private static Option<Integer> getIntOption(Object defaultGroup, Object group, Field field) {
        return ModConfigScreen.<Integer>getOptionBuilder(defaultGroup, group, field)
                .controller(IntegerFieldControllerBuilder::create)
                .build();
    }

    private static <T> Option.Builder<T> getOptionBuilder(Object defaultGroup, Object group, Field field) {
        String groupName = group.getClass().getSimpleName();
        if (group.getClass().getAnnotation(ModConfig.OverrideName.class) != null) {
            groupName = group.getClass().getAnnotation(ModConfig.OverrideName.class).newName();
        }
        final String fieldName = field.getName();
        Option.Builder<T> optionBuilder = Option.<T>createBuilder()
                .name(getComponent(groupName + "." + fieldName))
                .description(OptionDescription.of(getComponentWithFallback(groupName + "." + fieldName + ".description")))
                .binding(getBinding(defaultGroup, group, field));
        if (field.getDeclaredAnnotation(ModConfig.ReloadsResources.class) != null) {
            optionBuilder.flag(OptionFlag.ASSET_RELOAD);
        }
        return optionBuilder;
    }
    private static <T> ListOption.Builder<T> getListOptionBuilder(Object defaultGroup, Object group, Field field) {
        final String groupName = group.getClass().getSimpleName();
        final String fieldName = field.getName();
        return ListOption.<T>createBuilder()
                .name(getComponent(groupName + "." + fieldName))
                .description(OptionDescription.of(getComponentWithFallback(groupName + "." + fieldName + ".description")))
                .minimumNumberOfEntries(1)
                .binding(getBinding(defaultGroup, group, field));
    }
    @SuppressWarnings("unchecked")
    private static <T> Binding<T> getBinding(Object defaultGroup, Object group, Field field) {
        T defaultValue;
        try {
            Field defaultField = defaultGroup.getClass().getDeclaredField(field.getName());
            defaultField.setAccessible(true);
            defaultValue = (T) defaultField.get(defaultGroup);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return Binding.generic(defaultValue, () -> {
            try {
                return (T) field.get(group);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }, newVal -> {
            try {
                field.set(group, newVal);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    protected static Component getComponent(String translationKey) {
        return Component.translatable("particlerain." + translationKey);
    }
    private static Component getComponentWithFallback(String translationKey) {
        return Component.translatableWithFallback("particlerain." + translationKey, "");
    }
}