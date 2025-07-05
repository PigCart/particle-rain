package pigcart.particlerain.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import pigcart.particlerain.ParticleRain;


import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class ConfigScreens {

    private static Screen screenToOpen;
    private static String lastScreenInitialized = "";

    public static void regenerateScreen(YACLScreen thisScreen, Supplier<Screen> generator) {
        final String key = ((TranslatableContents) thisScreen.getTitle().getContents()).getKey();
        if (!lastScreenInitialized.equals(key)) {
            lastScreenInitialized = key;
            if (generator != null) Minecraft.getInstance().setScreen(generator.get());
        }
    }

    public static Screen generateScreen(String titleKey, Collection<OptionGroup> groups, Collection<Option<?>> options, Supplier<Screen> generator, Screen parent) {
        final ConfigCategory.Builder categoryBuilder = ConfigCategory.createBuilder();
        if (groups != null) categoryBuilder.groups(groups);
        if (options != null) categoryBuilder.options(options);
        return YetAnotherConfigLib.createBuilder()
                .title(getComponent(titleKey))
                .category(categoryBuilder.name(getComponent(titleKey)).build())
                .save(ModConfig::saveConfig)
                .screenInit((thisScreen) -> regenerateScreen(thisScreen, generator))
                .build()
                .generateScreen(parent);
    }

    public static Screen generateMainConfigScreen(Screen prevScreen) {
        List<OptionGroup> groups = collectGroups(ModConfig.DEFAULT, ModConfig.CONFIG);
        groups.add(1, getParticleToggles());
        return generateScreen("title", groups, collectOptions(ModConfig.DEFAULT, ModConfig.CONFIG), ()-> generateMainConfigScreen(prevScreen), prevScreen);
    }

    public static Screen generateParticleListScreen(Screen prevScreen) {
        Collection<Option<?>> options = new ArrayList<>();
        options.add(getScreenButtonOption(getComponent("button.add"),"", () -> {
            ModConfig.ParticleOptions particle = new ModConfig.ParticleOptions();
            ModConfig.CONFIG.customParticles.add(particle);
            ModConfig.saveConfig();
            screenToOpen = generateEditScreen(generateParticleListScreen(prevScreen), particle, particle);
        }));
        for (ModConfig.ParticleOptions opts : ModConfig.CONFIG.customParticles) {
            ModConfig.ParticleOptions defaultParticle = new ModConfig.ParticleOptions();
            for (ModConfig.ParticleOptions defaultParticle1 : ModConfig.DEFAULT.customParticles) {
                if (opts.id.equals(defaultParticle1.id)) defaultParticle = defaultParticle1;
            }
            ModConfig.ParticleOptions finalDefaultParticle = defaultParticle;
            options.add(LabelOption.create(Component.literal("")));
            final ButtonOption editButton = getScreenButtonOption(Component.literal(opts.id), "", () ->
                    screenToOpen = generateEditScreen(Minecraft.getInstance().screen, opts, finalDefaultParticle));
            if (opts.id.equals("rain_splashing") || opts.id.equals("rain_ripples") || opts.id.equals("rain_smoke") || opts.id.equals("shrubs")) editButton.setAvailable(false);
            options.add(editButton);
            options.add(getScreenButtonOption(Component.translatable("selectWorld.delete").withStyle(ChatFormatting.RED), "", () -> {
                ModConfig.CONFIG.customParticles.remove(opts);
                ModConfig.saveConfig();
                screenToOpen = generateParticleListScreen(prevScreen);
            }));
        }
        return generateScreen("editParticles", null, options, ()-> generateParticleListScreen(prevScreen), prevScreen);
    }

    public static Screen generateEditScreen(Screen prevScreen, Object configObject, Object defaultObject) {
        return generateScreen("category.edit", null, collectOptions(defaultObject, configObject), ()-> generateEditScreen(prevScreen, configObject, defaultObject), prevScreen);
    }

    private static ButtonOption getScreenButtonOption(Component name, String text, Runnable runnable) {
        return ButtonOption.createBuilder()
                .name(name)
                .text(Component.literal(text))
                .action((yaclScreen, buttonOption) -> {
                    runnable.run();
                    Minecraft.getInstance().setScreen(screenToOpen);
                }).build();
    }

    static <T> ButtonOption getListButtonOption(Object instance, Field field, ListOption<T> listOption) {
        String listText = "";
        try {
            listText = field.get(instance).toString();
        } catch (IllegalAccessException e) {
            ParticleRain.LOGGER.error(e.getMessage());
        }
        if (listText.length() > 42) listText = listText.substring(0, 42) + "...";
        return getScreenButtonOption(
                getComponent( instance.getClass().getSimpleName() + "." + field.getName()),
                listText,
                () -> screenToOpen = generateScreen("editList", List.of(listOption), null, null, Minecraft.getInstance().screen));
    }

    static OptionGroup getParticleToggles() {
        List<Option<?>> options = new ArrayList<>();
        options.add(getScreenButtonOption(getComponent("editParticles"), "", () ->
            screenToOpen = generateParticleListScreen(Minecraft.getInstance().screen)));
        for (ModConfig.ParticleOptions particle : ModConfig.CONFIG.customParticles) {
            try {
                options.add(getBoolOption(new ModConfig.ParticleOptions(), particle, particle.getClass().getField("enabled"))
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

    static List<OptionGroup> collectGroups(Object defaultInstance, Object instance) {
        List<OptionGroup> groups = new ArrayList<>();
        Field[] fields = instance.getClass().getFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ModConfig.Group.class)) {
                field.setAccessible(true);
                try {
                    groups.add(OptionGroup.createBuilder()
                            .name(getComponent(field.getType().getSimpleName()))
                            .description(OptionDescription.of(getComponentWithFallback(field.getName() + ".description")))
                            .options(collectOptions(field.get(defaultInstance), field.get(instance)))
                            .build());
                } catch (IllegalAccessException e) {
                    ParticleRain.LOGGER.error(e.toString());
                }
            }
        }
        return groups;
    }

    static List<Option<?>> collectOptions(Object defaultInstance, Object instance) {
        List<Option<?>> options = new ArrayList<>();
        Field[] fields = instance.getClass().getFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ModConfig.NoGUI.class) || field.isAnnotationPresent(ModConfig.Group.class)) continue;
            if (field.isAnnotationPresent(ModConfig.Label.class)) {
                options.add(LabelOption.create(getComponent(instance.getClass().getSimpleName() + "." + field.getDeclaredAnnotation(ModConfig.Label.class).key())));
            }
            field.setAccessible(true);
            final Class<?> type = field.getType();
            if (type.equals(boolean.class)) {
                options.add(getBoolOption(defaultInstance, instance, field).build());
            } else if (type.equals(float.class)) {
                if (field.getDeclaredAnnotation(ModConfig.Percentage.class) != null) {
                    options.add(getPercentOption(defaultInstance, instance, field));
                } else {
                    options.add(getFloatOption(defaultInstance, instance, field));
                }
            } else if (type.equals(int.class)) {
                options.add(getIntOption(defaultInstance, instance, field));
            } else if (type.equals(String.class)) {
                options.add(getStringOption(defaultInstance, instance, field));
            } else if (type.isEnum()) {
                options.add(getEnumOption(defaultInstance, instance, field, type));
            } else if (type.equals(URI.class)) {
                options.add(getLinkButtonOption(instance, field));
            } else if (type.equals(Color.class)) {
                options.add(getColorOption(defaultInstance, instance, field));
            } else if (type.equals(List.class)) {
                ListOption<?> listOption;
                final Class<?> listType = (Class<?>) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
                if (listType.equals(String.class)) {
                    listOption = getStringListOption(defaultInstance, instance, field);
                } else if (listType.isEnum()) {
                    listOption = getEnumListOption(defaultInstance, instance, field, listType);
                } else {
                    listOption = null;
                    ParticleRain.LOGGER.error("Unable to create list for field {}", field.getName());
                }
                if (listOption != null) options.add(getListButtonOption(instance, field, listOption));
            } else {
                ParticleRain.LOGGER.error("Unable to create option for field {}", field.getName());
            }
        }
        options.add(LabelOption.create(CommonComponents.EMPTY));
        return options;
    }

    private static ButtonOption getLinkButtonOption(Object instance, Field field) {
        String groupName = instance.getClass().getSimpleName();
        if (instance.getClass().isAnnotationPresent(ModConfig.OverrideName.class)) {
            groupName = instance.getClass().getAnnotation(ModConfig.OverrideName.class).newName();
        }
        final String fieldName = field.getName();
        try {
            return ButtonOption.createBuilder()
                    .name(getComponent(groupName + "." + fieldName))
                    .description(OptionDescription.of(Component.literal(field.get(instance).toString())))
                    .text(Component.literal(""))
                    .action(((yaclScreen, buttonOption) -> {
                        Minecraft minecraft = Minecraft.getInstance();
                        try {
                            minecraft.setScreen(new ConfirmLinkScreen((result) -> {
                                    try {
                                        if (result) Util.getPlatform().openUri((URI) field.get(instance));
                                    } catch (IllegalAccessException ignored) {}
                                minecraft.setScreen(yaclScreen);
                            }, field.get(instance).toString(), true
                            ));
                        } catch (IllegalAccessException ignored) {}
                    })).build();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    private static Option<Color> getColorOption(Object defaultInstance, Object instance, Field field) {
        return ConfigScreens.<Color>getOptionBuilder(defaultInstance, instance, field)
                .controller(ColorControllerBuilder::create)
                .build();
    }
    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> Option<T> getEnumOption(Object defaultInstance, Object instance, Field field, Class<?> eClass) {
        return ConfigScreens.<T>getOptionBuilder(defaultInstance, instance, field)
                .controller(opt -> EnumControllerBuilder.create(opt).enumClass((Class<T>) eClass))
                .build();
    }
    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> ListOption<T> getEnumListOption(Object defaultInstance, Object instance, Field field, Class<?> eClass) {
        return ConfigScreens.<T>getListOptionBuilder(defaultInstance, instance, field)
                .controller(opt -> EnumControllerBuilder.create(opt).enumClass((Class<T>) eClass))
                .build();
    }
    private static ListOption<String> getStringListOption(Object defaultInstance, Object instance, Field field) {
        return ConfigScreens.<String>getListOptionBuilder(defaultInstance, instance, field)
                .controller(StringControllerBuilder::create)
                .initial("")
                .build();
    }
    private static Option.Builder<Boolean> getBoolOption(Object defaultInstance, Object instance, Field field) {
        return ConfigScreens.<Boolean>getOptionBuilder(defaultInstance, instance, field)
                .controller(opt -> {
                    if (field.isAnnotationPresent(ModConfig.BooleanFormat.class)) {
                        return BooleanControllerBuilder.create(opt).formatValue(val -> getComponent(
                                val ? field.getAnnotation(ModConfig.BooleanFormat.class).t()
                                : field.getAnnotation(ModConfig.BooleanFormat.class).f()
                        ));
                    }
                    return BooleanControllerBuilder.create(opt).coloured(true);
                });
    }
    private static Option<Float> getFloatOption(Object defaultInstance, Object instance, Field field) {
        return ConfigScreens.<Float>getOptionBuilder(defaultInstance, instance, field)
                .controller(opt -> FloatFieldControllerBuilder.create(opt)
                        .formatValue(val -> Component.literal(val.toString())))
                .build();
    }
    private static Option<Float> getPercentOption(Object defaultInstance, Object instance, Field field) {
        return ConfigScreens.<Float>getOptionBuilder(defaultInstance, instance, field)
                .controller(opt -> FloatSliderControllerBuilder.create(opt)
                        .range(0f, 1f)
                        .step(0.01f)
                        .formatValue(val -> Component.literal(NumberFormat.getPercentInstance().format(val))))
                .build();
    }
    private static Option<Integer> getIntOption(Object defaultInstance, Object instance, Field field) {
        return ConfigScreens.<Integer>getOptionBuilder(defaultInstance, instance, field)
                .controller(IntegerFieldControllerBuilder::create)
                .build();
    }
    private static Option<String> getStringOption(Object defaultInstance, Object instance, Field field) {
        return ConfigScreens.<String>getOptionBuilder(defaultInstance, instance, field)
                .controller(StringControllerBuilder::create)
                .build();
    }

    private static <T> Option.Builder<T> getOptionBuilder(Object defaultInstance, Object instance, Field field) {
        String groupName = instance.getClass().getSimpleName();
        if (instance.getClass().isAnnotationPresent(ModConfig.OverrideName.class)) {
            groupName = instance.getClass().getAnnotation(ModConfig.OverrideName.class).newName();
        }
        final String fieldName = field.getName();
        final Binding<T> binding = getBinding(defaultInstance, instance, field);
        Option.Builder<T> optionBuilder = Option.<T>createBuilder()
                .name(getComponent(groupName + "." + fieldName))
                .description(OptionDescription.of(getComponentWithFallback(groupName + "." + fieldName + ".description")));
        if (field.getDeclaredAnnotation(ModConfig.ReloadsResources.class) != null) {
            optionBuilder.flag(OptionFlag.ASSET_RELOAD).binding(binding);
            // 'Always synced state managers do not support option flags' :/
        } else {
            // Prevents changes from being discarded when browsing between screens.
            optionBuilder.stateManager(StateManager.createInstant(binding));
        }
        return optionBuilder;
    }
    @SuppressWarnings("unchecked")
    private static <T> ListOption.Builder<T> getListOptionBuilder(Object defaultGroup, Object group, Field field) {
        final String groupName = group.getClass().getSimpleName();
        final String fieldName = field.getName();
        return ListOption.<T>createBuilder()
                .name(getComponent(groupName + "." + fieldName))
                .description(OptionDescription.of(getComponentWithFallback(groupName + "." + fieldName + ".description")))
                //.minimumNumberOfEntries(1)
                .binding(getBinding(defaultGroup, group, field))
                .initial(() -> {
                    try {
                        Field defaultField = defaultGroup.getClass().getField(field.getName());
                        defaultField.setAccessible(true);
                        return (T) ((List<?>)defaultField.get(defaultGroup)).get(0);
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
    @SuppressWarnings("unchecked")
    private static <T> Binding<T> getBinding(Object defaultGroup, Object group, Field field) {
        T defaultValue;
        try {
            Field defaultField = defaultGroup.getClass().getField(field.getName());
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
        return Component.translatable(ParticleRain.MOD_ID + "." + translationKey);
    }
    private static Component getComponentWithFallback(String translationKey) {
        return Component.translatableWithFallback("particlerain." + translationKey, "");
    }
}