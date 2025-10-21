package pigcart.particlerain.config;

import com.google.gson.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.VersionUtil;
import pigcart.particlerain.WeatherParticleManager;
import pigcart.particlerain.mixin.access.ParticleEngineAccessor;

import java.awt.*;
import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConfigManager {
    static final Gson GSON = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(Color.class, new ColorTypeAdapter())
            .create();
    static final String CONFIG_PATH = "config/" + ParticleRain.MOD_ID + ".json";
    public static ConfigData config;
    public static ConfigData defaultConfig = new ConfigData();

    public static void load() {
        File file = new File(CONFIG_PATH);
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                config = GSON.fromJson(reader, ConfigData.class);
            } catch (Exception e) {
                ParticleRain.LOGGER.error("Error loading config: {}", e.getMessage());
                config = new ConfigData();
                save();
            }
        } else {
            ParticleRain.LOGGER.info("Creating config file at " + CONFIG_PATH);
            config = new ConfigData();
            save();
        }
        if (config.configVersion < defaultConfig.configVersion) {
            ParticleRain.LOGGER.info("Overwriting old config file");
            config = new ConfigData();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_PATH)) {
            GSON.toJson(config, writer);
        } catch (Exception e) {
            ParticleRain.LOGGER.error(e.getMessage());
        }
        updateTransientVariables();
    }

    public static void updateTransientVariables() {
        for (ConfigData.ParticleData opts: config.particles) {
            opts.biomeList.populateInternalLists();
            opts.blockList.populateInternalLists();
            opts.setPresetParticle();
        }
    }

    public static class ColorTypeAdapter implements JsonSerializer<Color>, JsonDeserializer<Color> {
        @Override
        public JsonElement serialize(Color color, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(String.join("",
                    "#",
                    String.format("%02X", color.getRed()),
                    String.format("%02X", color.getGreen()),
                    String.format("%02X", color.getBlue())));
        }
        @Override
        public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Color.decode(json.getAsString());
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface Slider {
        float min() default 0F;
        float max() default 1F;
        float step() default 0.01F;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface Format {Class<? extends Function<Object, Component>> value();}

    public static class Percent implements Function<Object, Component> {
        public Component apply(Object value) {
            return Component.literal(NumberFormat.getPercentInstance().format(value));
        }
    }

    public static class PercentOrOff implements Function<Object, Component> {
        public Component apply(Object value) {
            return ((Number)value).doubleValue() == 0 ? CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED) : Component.literal(NumberFormat.getPercentInstance().format(value));
        }
    }

    public static class ZeroIsAutomatic implements Function<Object, Component> {
        public Component apply(Object value) {
            return ((Number)value).doubleValue() == 0 ? Component.translatable("particlerain.auto") : Component.literal(value.toString());
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface OverrideName { String value(); }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface Label {String key();}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface BooleanFormat {String t(); String f();}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface Group {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface NoGUI {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface RegenScreen {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface OnChange {Class<? extends Runnable> value();}

    public static class ReloadResources implements Runnable {
        public void run() {
            Minecraft.getInstance().reloadResourcePacks();
        }
    }
    public static class ClearParticles implements Runnable {
        public void run() {
            ((ParticleEngineAccessor)Minecraft.getInstance().particleEngine).callClearParticles();
            WeatherParticleManager.particleGroup = new /*? if >=1.21.9 {*//*ParticleLimit*//*?} else {*/ParticleGroup/*?}*/(config.perf.maxParticleAmount);
            //MistParticle.group = new ParticleGroup(config.mist.amount);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface Dropdown {Class<? extends Supplier<List<String>>> value();}

    public static class SupplyParticleTypes implements Supplier<List<String>> {
        public List<String> get() {
            List<String> list = new ArrayList<>();
            for (Map.Entry<ResourceKey<ParticleType<?>>, ParticleType<?>> entry : BuiltInRegistries.PARTICLE_TYPE.entrySet()) {
                if (entry.getValue() instanceof SimpleParticleType) {
                    list.add(entry.getKey().location().toString());
                }
            }
            return list;
        }
    }
    public static class SupplyBlocks implements Supplier<List<String>> {
        public List<String> get() {
            if (Minecraft.getInstance().level == null) return List.of("[!] §e§l" + Component.translatable("particlerain.suggest").getString());
            return getRegistryEntries(BuiltInRegistries.BLOCK);
        }
    }
    public static class SupplyBiomes implements Supplier<List<String>> {
        public List<String> get() {
            if (Minecraft.getInstance().level == null) return List.of("[!] §e§l" + Component.translatable("particlerain.suggest").getString());
            return getRegistryEntries(VersionUtil.getRegistry(Registries.BIOME));
        }
    }
    public static List<String> getRegistryEntries(Registry<?> registry) {
        List<String> list = new ArrayList<>();
        registry.keySet().forEach((id)-> list.add(id.toString()));
        VersionUtil.getTagIds(registry).forEach((tag)-> list.add("#" + tag.location()));
        return list;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface OnlyVisibleIf {Class<? extends Function<Object, Boolean>> value();}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface OnlyEditableIf {Class<? extends Function<Object, Boolean>> value();}

    public static class ParticleIsCustom implements Function<Object, Boolean> {
        public Boolean apply(Object context) {
            ConfigData.ParticleData ctx = (ConfigData.ParticleData) context;
            return !ctx.usePresetParticle;
        }
    }
    public static class ParticleNotCustom implements Function<Object, Boolean> {
        public Boolean apply(Object context) {
            ConfigData.ParticleData ctx = (ConfigData.ParticleData) context;
            return ctx.usePresetParticle;
        }
    }
    public static class ParticleIsNotDefault implements Function<Object, Boolean> {
        public Boolean apply(Object context) {
            ConfigData.ParticleData ctx = (ConfigData.ParticleData) context;
            return ConfigManager.defaultConfig.particles.stream().noneMatch(
                        defaultData -> ctx.id.equals(defaultData.id)
                );
        }
    }
    public static class UsingCustomTint implements Function<Object, Boolean> {
        public Boolean apply(Object context) {
            ConfigData.ParticleData ctx = (ConfigData.ParticleData) context;
            return ctx.tintType == ConfigData.TintType.CUSTOM;
        }
    }
}