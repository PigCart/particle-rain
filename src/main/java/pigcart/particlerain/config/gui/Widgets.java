package pigcart.particlerain.config.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.VersionUtil;
import pigcart.particlerain.config.ConfigData;
import pigcart.particlerain.config.ConfigManager;
import pigcart.particlerain.config.gui.Annotations.*;
import pigcart.particlerain.config.gui.Annotations.Label;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static pigcart.particlerain.ParticleRain.MOD_ID;

public class Widgets {
    public static final int BUTTON_WIDTH = 150;
    public static final int BIG_BUTTON_WIDTH = 310;
    public static final int BUTTON_HEIGHT = 20;

    public static LabelWidget getLabel(Component message) {
        return new LabelWidget(BIG_BUTTON_WIDTH, BUTTON_HEIGHT, message);
    }

    public static LabelWidget getOptionLabel(Component message) {
        return new LabelWidget(BUTTON_WIDTH, BUTTON_HEIGHT, message).alignRight();
    }

    public static AbstractWidget getBool(String name, boolean initialValue, Consumer<Boolean> onValueChange, Function<Object, Component> valueFormatter) {
        return new CycleButton.Builder<>(
                valueFormatter
                //? if >=1.21.11 {
                /*,()-> initialValue)
                 *///?} else {
        ).withInitialValue(initialValue)
                //?}
                .withValues(initialValue, !initialValue)
                .create(0, 0, BIG_BUTTON_WIDTH, BUTTON_HEIGHT,
                        Component.translatable(name),
                        (widget, value) -> onValueChange.accept((Boolean) value));
    }

    public static AbstractWidget getBool(String name, boolean initialValue, Consumer<Boolean> onValueChange) {
        return getBool(name, initialValue, onValueChange,
                (value)-> (boolean) value ? CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN) : CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED));
    }

    public static AbstractWidget getEnum(String name, boolean displayOnlyValue, Object initialValue, Consumer<Object> onValueChange) {
        Object[] values = initialValue.getClass().getEnumConstants();
        if (values == null) values = initialValue.getClass().getEnclosingClass().getEnumConstants();
        final CycleButton.Builder<Object> builder = new CycleButton.Builder<>(
                (value) -> {
                    String className = value.getClass().getSimpleName();
                    if (className.isEmpty()) className = value.getClass().getEnclosingClass().getSimpleName();
                    return Component.translatable(MOD_ID + "." + className + "." + value);
                }
                //? if >=1.21.11 {
                /*,()-> initialValue)
                 *///?} else {
        ).withInitialValue(initialValue)
                //?}
                .withValues(values);
        if (displayOnlyValue) {
            builder.displayOnlyValue(/*? >=1.21.9 <1.21.11 {*//*true*//*?}*/);
        }
        return builder.create(0, 0, BIG_BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.translatable(name),
                (widget, value) -> onValueChange.accept(value));
    }

    public static AbstractWidget getFloat(int width, int x, String name, float initialValue, Consumer<Float> onValueChange, Function<Object, Component> valueFormatter) {
        final DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(6);
        final InputWidget inputWidget = new InputWidget(width, x,
                df.format(initialValue),
                (string) -> onValueChange.accept(Float.valueOf(string)),
                valueFormatter
        );
        inputWidget.setFilter(InputWidget.NON_FLOAT);
        inputWidget.setMessage(Component.translatable(name));
        return inputWidget;
    }

    public static AbstractWidget getInt(int width, int x, String name, int initialValue, Consumer<Integer> onValueChange, Function<Object, Component> valueFormatter) {
        final InputWidget inputWidget = new InputWidget(width, x,
                String.valueOf(initialValue),
                (string) -> onValueChange.accept(Integer.valueOf(string)),
                valueFormatter
        );
        inputWidget.setFilter(InputWidget.NON_INTEGER);
        inputWidget.setMessage(Component.translatable(name));
        return inputWidget;
    }

    public static AbstractWidget getString(int width, int x, String name, String initialValue, Consumer<String> onValueChange, Function<Object, Component> valueFormatter) {
        final InputWidget inputWidget = new InputWidget(width, x,
                String.valueOf(initialValue),
                onValueChange,
                valueFormatter
        );
        inputWidget.setFilter(InputWidget.NON_PATH);
        inputWidget.setMessage(Component.translatable(name));
        return inputWidget;
    }

    public static AbstractWidget getButton(Component name, Button.OnPress onPress) {
        return Button.builder(name, onPress).bounds(0, 0, BIG_BUTTON_WIDTH, BUTTON_HEIGHT).build();
    }

    public static AbstractWidget getSlider(String name, float initialValue, Consumer<Float> onValueChange, float min, float max, float step, Function<Object, Component> valueFormatter) {
        return new AbstractSliderButton(0, 0, BIG_BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.translatable(name).append(": ").append(valueFormatter.apply(initialValue)),
                Mth.lerp(initialValue, min, max)
        ) {
            protected void updateMessage() {
                this.setMessage(Component.translatable(name).append(": ").append(valueFormatter.apply(this.value)));
            }
            protected void applyValue() {
                onValueChange.accept((float) Math.round(Mth.lerp(value, min, max) / step) * step);
            }
        };
    }

    public static AbstractWidget[] getHexColor(int width, int x, String name, Object initialValue, Consumer<Object> onValueChange, Function<Object, Component> valueFormatter) {
        String value = ConfigManager.ColorTypeAdapter.getString((Color) initialValue);
        Consumer<String> onChange = (string) -> {
            Color color = ConfigManager.ColorTypeAdapter.getColor(string);
            onValueChange.accept(color);
        };
        InputWidget input = (InputWidget) getString(width, x, name, value, onChange, valueFormatter);
        input.setFilter(InputWidget.NON_HEX);
        return new AbstractWidget[]{
                Widgets.getOptionLabel(Component.translatable(name).append(":")),
                input
        };
    }

    @SuppressWarnings("unchecked")
    public static void addOptionWidgets(ConfigScreen screen) {
        if (screen.config instanceof List<?>) {
            addListOptions(screen, (List<?>)screen.config, (List<?>)screen.configDefault, screen.configGenericType);
            return;
        }
        Field[] fields = screen.config.getClass().getFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(NoGUI.class)) continue;
            if (field.isAnnotationPresent(OnlyVisibleIf.class)) {
                final OnlyVisibleIf annotation = field.getAnnotation(OnlyVisibleIf.class);
                try {
                    final Function<Object, Boolean> function = (Function<Object, Boolean>) annotation.value().getConstructors()[0].newInstance();
                    boolean optionIsVisible = function.apply(screen.config);
                    if (!optionIsVisible) continue;
                } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {throw new RuntimeException(e);}
            }
            if (field.isAnnotationPresent(Label.class)) {
                AbstractWidget[] widgets = new AbstractWidget[]{Widgets.getLabel(Component.translatable(field.getAnnotation(Label.class).key()))};
                screen.add(widgets);
            }
            String name = MOD_ID + "." + field.getName();
            field.setAccessible(true);
            final Class<?> type = field.getType();
            final Object currentValue;
            final Object defaultValue;
            try {
                currentValue = field.get(screen.config);
                defaultValue = field.get(screen.configDefault);
            } catch (IllegalAccessException e) {throw new RuntimeException(e);}
            Consumer onValueChange = (value) -> {
                try {
                    field.set(screen.config, value);
                    if (field.isAnnotationPresent(OnChange.class)) {
                        final OnChange onChange = field.getAnnotation(OnChange.class);
                        ((Runnable) onChange.value().getConstructors()[0].newInstance()).run();
                    }
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {throw new RuntimeException(e);}
            };
            Function<Object, Component> valueFormatter = (value)-> Component.literal(value.toString());
            if (field.isAnnotationPresent(Format.class)) {
                final Format format = field.getAnnotation(Format.class);
                try {
                    valueFormatter = (Function<Object, Component>) format.value().getConstructors()[0].newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
            if (type.equals(ArrayList.class)) {
                final Class<?> listType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];

                if (field.getName().equals("particles")) {
                    List<ConfigData.ParticleData> particles = (List<ConfigData.ParticleData>) currentValue;
                    for (int i = 0; i < particles.size(); i+= 2) {
                        ConfigData.ParticleData leftParticle = particles.get(i);
                        AbstractWidget left = Widgets.getBool(MOD_ID + "." + leftParticle.id,
                                leftParticle.enabled,
                                (value) -> leftParticle.enabled = value);
                        left.setWidth(BUTTON_WIDTH);
                        if (i < particles.size() - 1) {
                            ConfigData.ParticleData rightParticle = particles.get(i + 1);
                            AbstractWidget right = Widgets.getBool(MOD_ID + "." + rightParticle.id,
                                            rightParticle.enabled,
                                            (value) -> rightParticle.enabled = value);
                            ((AbstractWidgetAccess)right).particle_rain$setOffset(left.getWidth() + 8);
                            right.setWidth(BUTTON_WIDTH);
                            screen.add(left, right);
                        } else {
                            screen.add(left);
                        }
                    }
                } else if (field.isAnnotationPresent(NoSubMenu.class)) {
                    addListOptions(screen, (List<?>)currentValue, (List<?>)defaultValue, listType);
                    continue; // skip adding sub-menu
                }
                AbstractWidget[] widgets = new AbstractWidget[]{Widgets.getButton(Component.translatable(name).append("..."), (bttn) ->
                        Minecraft.getInstance().setScreen(new ConfigScreen(
                                screen.getFreshScreen(),
                                currentValue,
                                defaultValue,
                                listType,
                                Component.translatable(name)
                        ))
                )};
                screen.add(widgets);
            } else {
                AbstractWidget[] widgets = getOptionWidget(screen, field, name, currentValue, defaultValue, onValueChange, valueFormatter, type);
                screen.add(widgets);
            }
        }
        appendBespokeParticleOptions(screen);
    }
    @SuppressWarnings("unchecked")
    private static AbstractWidget[] getOptionWidget(ConfigScreen screen, Field field, String name, Object currentValue, Object defaultValue, Consumer onValueChange, Function<Object, Component> valueFormatter, Class<?> type) {
        if (field.isAnnotationPresent(Slider.class)) {
            final Slider slider = field.getAnnotation(Slider.class);
            return new AbstractWidget[]{
                    Widgets.getSlider(name, (Float) currentValue, onValueChange, slider.min(), slider.max(), slider.step(), valueFormatter)};
        } else if (type.equals(boolean.class)) {
            if (field.isAnnotationPresent(BooleanFormat.class)) {
                final BooleanFormat format = field.getAnnotation(BooleanFormat.class);
                final Component t = Component.translatable(MOD_ID + "." + format.t());
                final Component f = Component.translatable(MOD_ID + "." + format.f());
                valueFormatter = (value) -> (boolean)value ? t : f;
                return new AbstractWidget[]{Widgets.getBool(name, (Boolean) currentValue, onValueChange, valueFormatter)};
            } else {
                return new AbstractWidget[]{Widgets.getBool(name, (Boolean) currentValue, onValueChange)};
            }
        } else if (type.equals(float.class)) {
            return new AbstractWidget[]{
                    Widgets.getOptionLabel(Component.translatable(name).append(":")),
                    Widgets.getFloat(BUTTON_WIDTH, BUTTON_WIDTH + 8, "", (Float) currentValue, onValueChange, valueFormatter)};
        } else if (type.equals(int.class)) {
            return new AbstractWidget[]{
                    Widgets.getOptionLabel(Component.translatable(name).append(":")),
                    Widgets.getInt(BUTTON_WIDTH, BUTTON_WIDTH + 8, name, (Integer) currentValue, onValueChange, valueFormatter)};
        } else if (type.equals(String.class)) {
            return new AbstractWidget[]{
                    Widgets.getOptionLabel(Component.translatable(name).append(":")),
                    Widgets.getString(BUTTON_WIDTH, BUTTON_WIDTH + 8, name, (String) currentValue, onValueChange, valueFormatter)};
        } else if (type.isEnum()) {
            return new AbstractWidget[]{
                    Widgets.getEnum(name, false, currentValue, onValueChange)};
        } else if (type.equals(URI.class)) {
            return new AbstractWidget[]{
                    Widgets.getButton(Component.translatable(name), (bttn)-> Minecraft.getInstance().setScreen(new ConfirmLinkScreen(
                            (result) -> {
                                if (result) VersionUtil.openUri((URI) currentValue);
                                Minecraft.getInstance().setScreen(screen);
                                },
                            currentValue.toString(),
                            true
            )))};
        } else if (type.equals(Color.class)) {
            return getHexColor(BIG_BUTTON_WIDTH, 0, name, currentValue, onValueChange, valueFormatter);
        } else if (type.getFields().length > 0) {
            return new AbstractWidget[]{
                    Widgets.getButton(Component.translatable(name).append("..."), (bttn)->
                            Minecraft.getInstance().setScreen(new ConfigScreen(
                                    screen,
                                    currentValue,
                                    defaultValue,
                                    Component.translatable(name)
                            ))
                    )};
        } else {
            ParticleRain.LOGGER.error("Unable to create option for field {}", field.getName());
            return new AbstractWidget[]{Widgets.getLabel(Component.literal("Unable to create option for field " + field.getName()))};
        }
    }

    @SuppressWarnings("unchecked")
    private static <E> void addListOptions(ConfigScreen screen, List<E> list, List<?> defaultList, Class<?> listEntryType) {
        if (list.isEmpty()) {
            screen.add(getLabel(Component.translatable("mco.configure.world.slot.empty")));
        }
        //final Class<?> listEntryType = list.get(0).getClass();
        for (int i = 0; i < list.size(); i++) {
            Object entry = list.get(i);
            Object defaultEntry = i < defaultList.size() ? defaultList.get(i) : getNewValue(listEntryType);
            final int index = i;
            final Consumer<Object> onValueChange = (value) -> list.set(index, (E) value);
            final AbstractWidget listEntryWidget = getListEntryWidget(screen, listEntryType, String.valueOf(index + 1), entry, defaultEntry, onValueChange);
            final AbstractWidget removeButton = getButton(Component.literal("❌").withStyle(ChatFormatting.RED), (bttn) -> {
                list.remove(index);
                screen.refresh();
            });
            listEntryWidget.setWidth(BIG_BUTTON_WIDTH - BUTTON_HEIGHT - 2);
            removeButton.setWidth(BUTTON_HEIGHT);
            ((AbstractWidgetAccess)removeButton).particle_rain$setOffset(BIG_BUTTON_WIDTH - BUTTON_HEIGHT);
            screen.add(listEntryWidget, removeButton);
        }
        final MutableComponent addButtonText = Component.translatable("particlerain.addNew");
        final AbstractWidget addButton = Widgets.getButton(addButtonText, (bttn) -> {
            Object newListEntry = getNewValue(listEntryType);
            Object defaultEntry = getNewValue(listEntryType);
            list.add((E) newListEntry);
            if (listEntryType.equals(ConfigData.ParticleData.class)) {
                Minecraft.getInstance().setScreen(new ConfigScreen(screen.getFreshScreen(), newListEntry, defaultEntry, addButtonText));
            } else {
                screen.refresh();
            }
        });
        if (!(list instanceof ArrayList<?>)) addButton.active = false;
        screen.add(addButton);
    }

    @SuppressWarnings("unchecked")
    private static AbstractWidget getListEntryWidget(ConfigScreen screen, Class<?> type, String name, Object entry, Object defaultEntry, Consumer onValueChange) {
        if (type.equals(String.class)) {
            return Widgets.getString(BIG_BUTTON_WIDTH, 0,
                    name,
                    (String) entry,
                    onValueChange,
                    (value)-> Component.literal(value.toString())
            );
        } else if (type.equals(ConfigData.ParticleData.class)) {
            String particleKey = MOD_ID + "." + ((ConfigData.ParticleData) entry).id;
            return Widgets.getButton(Component.translatable(particleKey), (bttn) ->
                    Minecraft.getInstance().setScreen(new ConfigScreen(
                            screen.getFreshScreen(), entry, defaultEntry, Component.translatable(particleKey)
                    ))
            );
        } else if (type.isEnum()) {
            return Widgets.getEnum(name, true, entry, onValueChange);
        } else {
            return Widgets.getLabel(Component.literal(type.getSimpleName() + " unimplemented"));
        }
    }

    private static Object getNewValue(Class<?> type) {
        if (type.isEnum()) {
            return type.getEnumConstants()[0];
        } else {
            try {
                return type.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                ParticleRain.LOGGER.error("Couldn't get new value for: {}", type.getSimpleName());
                throw new RuntimeException(e);
            }
        }
    }

    private static void appendBespokeParticleOptions(ConfigScreen screen) {
        try {
            if (screen.config instanceof ConfigData.ParticleData particleData) {
                if (particleData.usePresetParticle) {
                    String configId = VersionUtil.parseId(particleData.presetParticleId).getPath();
                    if (ParticleRain.particleConfigIds.contains(configId)) {
                        final Field f = ConfigData.class.getField(configId);
                        f.setAccessible(true);
                        final Object bespokeParticleConfig = f.get(ConfigManager.config);
                        final Object defaultConfig = f.get(ConfigManager.getDefaultConfig());
                        AbstractWidget button = getButton(Component.translatable("particlerain.appearance"),
                                (bttn) -> Minecraft.getInstance().setScreen(new ConfigScreen(
                                        screen,
                                        bespokeParticleConfig,
                                        defaultConfig,
                                        Component.translatable("particlerain.appearance")
                                )));
                        screen.list.add(button);
                    }
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
