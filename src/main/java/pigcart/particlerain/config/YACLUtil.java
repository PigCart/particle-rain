package pigcart.particlerain.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.config.ConfigManager.Format;
import pigcart.particlerain.config.ConfigManager.OnlyEditableIf;
import pigcart.particlerain.config.ConfigManager.OverrideName;
import pigcart.particlerain.mixin.yacl.YACLScreenAccessor;


import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class YACLUtil {

    private static String lastScreenInitialized = "";

    public static void regenerateScreen(YACLScreen thisScreen, Supplier<Screen> generator) {
        final String key = ((TranslatableContents) thisScreen.getTitle().getContents()).getKey();
        if (!lastScreenInitialized.equals(key)) {
            lastScreenInitialized = key;
            if (generator != null) Minecraft.getInstance().setScreen(generator.get());
        }
    }

    public static Screen generateScreen(Component title, Collection<OptionGroup> groups, Collection<Option<?>> options, Supplier<Screen> generator, Screen parent) {
        final ConfigCategory.Builder categoryBuilder = ConfigCategory.createBuilder();
        if (groups != null && !groups.isEmpty()) categoryBuilder.groups(groups);
        if (options != null && !options.isEmpty()) categoryBuilder.options(options);
        return YetAnotherConfigLib.createBuilder()
                .title(title)
                .category(categoryBuilder.name(title).build())
                .save(ConfigManager::save)
                .screenInit((thisScreen) -> regenerateScreen(thisScreen, generator))
                .build()
                .generateScreen(parent);
    }



    static ButtonOption getScreenButtonOption(Component name, String text, Supplier<Screen> screenSupplier) {
        return getScreenButtonOption(name, text, true, screenSupplier);
    }
    private static ButtonOption getScreenButtonOption(Component name, String text, boolean available, Supplier<Screen> screenSupplier) {
        return ButtonOption.createBuilder()
                .name(name)
                .text(Component.literal(text))
                .available(available)
                .action((yaclScreen, buttonOption) -> {
                    Minecraft.getInstance().setScreen(screenSupplier.get());
                }).build();
    }

    @SuppressWarnings("unchecked")
    static <T> ButtonOption getListButtonOption(Object instance, Field field, ListOption<T> listOption) {
        String listText = "";
        try {
            listText = cropText(field.get(instance).toString());
        } catch (IllegalAccessException e) {
            ParticleRain.LOGGER.error(e.getMessage());
        }
        boolean available = true;
        if (field.isAnnotationPresent(OnlyEditableIf.class)) {
            final OnlyEditableIf annotation = field.getAnnotation(OnlyEditableIf.class);
            try {
                final Function<Object, Boolean> function = (Function<Object, Boolean>) annotation.value().getConstructors()[0].newInstance();
                available = function.apply(instance);
            } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
        return getScreenButtonOption(
                getComponent( instance.getClass().getSimpleName() + "." + field.getName()),
                listText,
                available,
                () -> generateScreen(getComponent("editList"), List.of(listOption), null, null, Minecraft.getInstance().screen)
        );
    }

    @SuppressWarnings("unchecked")
    static List<OptionGroup> collectGroups(Object defaultInstance, Object instance) {
        List<OptionGroup> groups = new ArrayList<>();
        Field[] fields = instance.getClass().getFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ConfigManager.Group.class)) {
                if (field.isAnnotationPresent(ConfigManager.Dropdown.class)) {
                    final ConfigManager.Dropdown annotation = field.getAnnotation(ConfigManager.Dropdown.class);
                    try {
                        List<String> strings = ((Supplier<List<String>>) annotation.value().getConstructors()[0].newInstance()).get();
                        groups.add(getStringDropdownListOption(defaultInstance, instance, field, strings));

                    } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
                        throw new RuntimeException(e);
                    }
                } else if (field.getType().equals(List.class)) {
                    groups.add(getListOption(defaultInstance, instance, field));
                } else {
                    field.setAccessible(true);
                    try {
                        groups.add(getObjectAsGroup(defaultInstance, instance, field));
                    } catch (IllegalAccessException e) {
                        ParticleRain.LOGGER.error(e.toString());
                    }
                }
            }
        }
        return groups;
    }

    static OptionGroup getObjectAsGroup(Object defaultInstance, Object instance, Field field) throws IllegalAccessException {
        if (field == null) return null;
        return OptionGroup.createBuilder()
                .name(getComponent(field.getType().getSimpleName()))
                .description(OptionDescription.of(getComponentWithFallback(field.getName() + ".description")))
                .options(collectOptions(field.get(defaultInstance), field.get(instance)))
                .build();
    }

    @SuppressWarnings("unchecked")
    static List<Option<?>> collectOptions(Object defaultInstance, Object instance) {
        List<Option<?>> options = new ArrayList<>();
        Field[] fields = instance.getClass().getFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ConfigManager.NoGUI.class) || field.isAnnotationPresent(ConfigManager.Group.class)) continue;
            if (field.isAnnotationPresent(ConfigManager.OnlyVisibleIf.class)) {
                final ConfigManager.OnlyVisibleIf annotation = field.getAnnotation(ConfigManager.OnlyVisibleIf.class);
                try {
                    final Function<Object, Boolean> function = (Function<Object, Boolean>) annotation.value().getConstructors()[0].newInstance();
                    boolean optionIsVisible = function.apply(instance);
                    if (!optionIsVisible) continue;
                } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
            }
            if (field.isAnnotationPresent(ConfigManager.Label.class)) {
                options.add(LabelOption.create(getComponent(instance.getClass().getSimpleName() + "." + field.getDeclaredAnnotation(ConfigManager.Label.class).key())));
            }
            field.setAccessible(true);
            final Class<?> type = field.getType();
            if (type.equals(boolean.class)) {
                options.add(getBoolOption(defaultInstance, instance, field).build());
            } else if (type.equals(float.class)) {
                options.add(getFloatOption(defaultInstance, instance, field));
            } else if (type.equals(int.class)) {
                options.add(getIntOption(defaultInstance, instance, field));
            } else if (field.isAnnotationPresent(ConfigManager.Dropdown.class)) {
                final ConfigManager.Dropdown annotation = field.getAnnotation(ConfigManager.Dropdown.class);
                try {
                    List<String> strings = ((Supplier<List<String>>) annotation.value().getConstructors()[0].newInstance()).get();
                    if (type.equals(List.class)) {
                        options.add(getStringDropdownListOption(defaultInstance, instance, field, strings));
                    } else {
                        options.add(getStringDropdownOption(defaultInstance, instance, field, strings));
                    }
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else if (type.equals(String.class)) {
                options.add(getStringOption(defaultInstance, instance, field));
            } else if (type.isEnum()) {
                options.add(getEnumOption(defaultInstance, instance, field, type));
            } else if (type.equals(URI.class)) {
                options.add(getLinkButtonOption(instance, field));
            } else if (type.equals(Color.class)) {
                options.add(getColorOption(defaultInstance, instance, field));
            } else if (type.equals(List.class)) {
                ListOption<?> listOption = getListOption(defaultInstance, instance, field);
                if (listOption != null) options.add(getListButtonOption(instance, field, listOption));
            } else if (type.getFields().length != 0) {
                options.add(getObjectAsOption(defaultInstance, instance, field));
            } else {
                ParticleRain.LOGGER.error("Unable to create option for field {}", field.getName());
            }
        }
        options.add(LabelOption.create(CommonComponents.EMPTY));
        return options;
    }
    private static ListOption<?> getListOption(Object defaultInstance, Object instance, Field field) {
        final Class<?> listType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        if (listType.equals(String.class)) {
            return getStringListOption(defaultInstance, instance, field);
        } else if (listType.isEnum()) {
            return getEnumListOption(defaultInstance, instance, field, listType);
        } else {
            ParticleRain.LOGGER.error("Unable to create list for field {}", field.getName());
            return null;
        }
    }
    private static ButtonOption getObjectAsOption(Object defaultInstance, Object instance, Field field) {
        try {
            final Object newDefaultInstance = field.get(defaultInstance);
            final Object newInstance = field.get(instance);
            final Component name = getComponent(field.getName());
            String text = "";
            // exclude classes that haven't overriden toString
            if (!newInstance.toString().startsWith(newInstance.getClass().getName())) {
                text = cropText(newInstance.toString());
            }
            return getScreenButtonOption(name, text, ()-> generateScreen(
                    getComponent(field.getName()),
                    collectGroups(newDefaultInstance, newInstance),
                    collectOptions(newDefaultInstance, newInstance),
                    null,
                    Minecraft.getInstance().screen));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static ButtonOption getLinkButtonOption(Object instance, Field field) {
        String groupName = instance.getClass().getSimpleName();
        if (instance.getClass().isAnnotationPresent(OverrideName.class)) {
            groupName = instance.getClass().getAnnotation(OverrideName.class).value();
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
        return YACLUtil.<Color>getOptionBuilder(defaultInstance, instance, field)
                .controller(ColorControllerBuilder::create)
                .build();
    }
    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> Option<T> getEnumOption(Object defaultInstance, Object instance, Field field, Class<?> eClass) {
        return YACLUtil.<T>getOptionBuilder(defaultInstance, instance, field)
                .controller(opt -> EnumControllerBuilder.create(opt).enumClass((Class<T>) eClass))
                .build();
    }
    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> ListOption<T> getEnumListOption(Object defaultInstance, Object instance, Field field, Class<?> eClass) {
        return YACLUtil.<T>getListOptionBuilder(defaultInstance, instance, field)
                .controller(opt -> EnumControllerBuilder.create(opt).enumClass((Class<T>) eClass))
                .build();
    }
    private static ListOption<String> getStringListOption(Object defaultInstance, Object instance, Field field) {
        return YACLUtil.<String>getListOptionBuilder(defaultInstance, instance, field)
                .controller(StringControllerBuilder::create)
                .initial("")
                .build();
    }
    static Option.Builder<Boolean> getBoolOption(Object defaultInstance, Object instance, Field field) {
        return YACLUtil.<Boolean>getOptionBuilder(defaultInstance, instance, field)
                .controller(opt -> {
                    if (field.isAnnotationPresent(ConfigManager.BooleanFormat.class)) {
                        return BooleanControllerBuilder.create(opt).formatValue(val -> getComponent(
                                val ? field.getAnnotation(ConfigManager.BooleanFormat.class).t()
                                : field.getAnnotation(ConfigManager.BooleanFormat.class).f()
                        ));
                    }
                    return BooleanControllerBuilder.create(opt).coloured(true);
                });
    }

    @SuppressWarnings("unchecked")
    private static <T> ValueFormatter<T> getFormatter(Field field) {
        if (field.isAnnotationPresent(Format.class)) {
            final Format annotation = field.getAnnotation(Format.class);
            try {
                final Function<Object, Component> function = (Function<Object, Component>) annotation.value().getConstructors()[0].newInstance();
                return function::apply;
            } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
        return val -> Component.literal(val.toString());
    }

    private static Option<Float> getFloatOption(Object defaultInstance, Object instance, Field field) {
        return YACLUtil.<Float>getOptionBuilder(defaultInstance, instance, field)
                .controller(opt -> {
                    if (field.isAnnotationPresent(ConfigManager.Slider.class)) {
                        ConfigManager.Slider annotation = field.getAnnotation(ConfigManager.Slider.class);
                        return FloatSliderControllerBuilder.create(opt).formatValue(getFormatter(field))
                                .range(annotation.min(), annotation.max())
                                .step(annotation.step());
                    }
                    return FloatFieldControllerBuilder.create(opt).formatValue(getFormatter(field));
                })
                .build();
    }
    private static Option<Integer> getIntOption(Object defaultInstance, Object instance, Field field) {
        return YACLUtil.<Integer>getOptionBuilder(defaultInstance, instance, field)
                .controller(opt ->
                        IntegerFieldControllerBuilder.create(opt).formatValue(getFormatter(field)))
                .build();
    }
    private static Option<String> getStringOption(Object defaultInstance, Object instance, Field field) {
        return YACLUtil.<String>getOptionBuilder(defaultInstance, instance, field)
                .controller(StringControllerBuilder::create)
                .build();
    }
    private static Option<String> getStringDropdownOption(Object defaultInstance, Object instance, Field field, List<String> strings) {
        BuiltInRegistries.PARTICLE_TYPE.keySet();
        return YACLUtil.<String>getOptionBuilder(defaultInstance, instance, field)
                .controller(opt -> DropdownStringControllerBuilder.create(opt)
                        .allowAnyValue(true)
                        .allowEmptyValue(true)
                        .values(strings)
                )
                .build();
    }
    private static ListOption<String> getStringDropdownListOption(Object defaultInstance, Object instance, Field field, List<String> strings) {
        BuiltInRegistries.PARTICLE_TYPE.keySet();
        return YACLUtil.<String>getListOptionBuilder(defaultInstance, instance, field)
                .controller(opt -> DropdownStringControllerBuilder.create(opt)
                        .allowAnyValue(true)
                        .allowEmptyValue(true)
                        .values(strings)
                )
                .initial("")
                .build();
    }

    @SuppressWarnings("unchecked")
    private static <T> Option.Builder<T> getOptionBuilder(Object defaultInstance, Object instance, Field field) {
        String groupName = instance.getClass().getSimpleName();
        if (instance.getClass().isAnnotationPresent(OverrideName.class)) {
            groupName = instance.getClass().getAnnotation(OverrideName.class).value();
        }
        final String fieldName = field.getName();
        final Binding<T> binding = getBinding(defaultInstance, instance, field);
        Option.Builder<T> optionBuilder = Option.<T>createBuilder()
                .name(getComponent(groupName + "." + fieldName))
                .description(OptionDescription.of(getComponentWithFallback(groupName + "." + fieldName + ".description")));
        // Prevents changes from being discarded when browsing between screens.
        optionBuilder.stateManager(StateManager.createInstant(binding));
        if (field.isAnnotationPresent(OnlyEditableIf.class)) {
            final OnlyEditableIf annotation = field.getAnnotation(OnlyEditableIf.class);
            try {
                final Function<Object, Boolean> function = (Function<Object, Boolean>) annotation.value().getConstructors()[0].newInstance();
                optionBuilder.available(function.apply(instance));
            } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
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
    private static <T> Binding<T> getBinding(Object defaultInstance, Object instance, Field field) {
        T defaultValue;
        try {
            Field defaultField = defaultInstance.getClass().getField(field.getName());
            defaultField.setAccessible(true);
            defaultValue = (T) defaultField.get(defaultInstance);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return Binding.generic(defaultValue, () -> {
            // gets the value from the field and displays it in the controller
            try {
                return (T) field.get(instance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }, newVal -> {
            // sets the value of the field from the contents of the controller
            try {
                Object oldVal = field.get(instance);
                String oldStr = oldVal == null ? "" : oldVal.toString();
                if (!newVal.toString().equals(oldStr)) {
                    field.set(instance, newVal);
                    final ConfigManager.OnChange onChange = field.getDeclaredAnnotation(ConfigManager.OnChange.class);
                    if (onChange != null) {
                        ((Runnable) onChange.value().getConstructors()[0].newInstance()).run();
                    }
                    final ConfigManager.RegenScreen regenScreen = field.getDeclaredAnnotation(ConfigManager.RegenScreen.class);
                    if (regenScreen != null) {
                        final Screen parent = ((YACLScreenAccessor)Minecraft.getInstance().screen).getParent();
                        final Screen screen = ConfigScreens.generateParticleEditScreen(parent, instance, defaultInstance);
                        Minecraft.getInstance().setScreen(screen);
                        // oh past me hardcoded this? future me pls fix
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                //throw new RuntimeException(e);
                ParticleRain.LOGGER.error(e.getMessage());
            }
        });
    }

    static Component getComponent(String translationKey) {
        return Component.translatable(ParticleRain.MOD_ID + "." + translationKey);
    }
    static Component getComponentWithFallback(String translationKey) {
        return Component.translatableWithFallback("particlerain." + translationKey, "");
    }
    private static String cropText(String text) {
        if (text.length() > 42) text = text.substring(0, 42) + "...";
        return text;
    }
}