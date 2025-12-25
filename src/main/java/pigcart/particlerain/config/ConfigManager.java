package pigcart.particlerain.config;

import com.google.gson.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
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
import pigcart.particlerain.config.gui.ConfigScreen;
import pigcart.particlerain.mixin.access.ParticleEngineAccessor;

import java.awt.*;
import java.io.*;
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

    public static Screen screenPlease(Screen lastScreen) {
        return new ConfigScreen(lastScreen, config, getDefaultConfig(), Component.translatable("particlerain.title"));
    }

    public static ConfigData getDefaultConfig() {
        return new ConfigData();
    }

    public static void load() {
        File file = new File(CONFIG_PATH);
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                config = GSON.fromJson(reader, ConfigData.class);
            } catch (Exception e) {
                ParticleRain.LOGGER.error("Error loading config: {}", e.getMessage());
                config = getDefaultConfig();
                save();
            }
        } else {
            ParticleRain.LOGGER.info("Creating config file at " + CONFIG_PATH);
            config = getDefaultConfig();
            save();
        }
        if (config.configVersion < getDefaultConfig().configVersion) {
            ParticleRain.LOGGER.info("Overwriting old config file");
            config = getDefaultConfig();
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
        public static Color getColor(String string) {
            return Color.decode(string);
        }
        public static String getString(Color color) {
            return String.join("",
                    "#",
                    String.format("%02X", color.getRed()),
                    String.format("%02X", color.getGreen()),
                    String.format("%02X", color.getBlue()));
        }

        @Override
        public JsonElement serialize(Color color, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(getString(color));
        }
        @Override
        public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return getColor(json.getAsString());
        }
    }

    public static class Percent implements Function<Object, Component> {
        public Component apply(Object value) {
            return Component.literal(NumberFormat.getPercentInstance().format(value));
        }
    }

    public static class PercentOrOff implements Function<Object, Component> {
        public Component apply(Object value) {
            return ((Number)value).floatValue() == 0 ? CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED) : Component.literal(NumberFormat.getPercentInstance().format(value));
        }
    }

    public static class ZeroIsAutomatic implements Function<Object, Component> {
        public Component apply(Object stringValue) {
            final int value = Integer.parseInt((String) stringValue);
            return value == 0 ? Component.translatable("particlerain.auto") : Component.literal((String) stringValue);
        }
    }

    public static class ReloadResources implements Runnable {
        public void run() {
            Minecraft.getInstance().reloadResourcePacks();
        }
    }
    public static class ClearParticles implements Runnable {
        public void run() {
            ((ParticleEngineAccessor)Minecraft.getInstance().particleEngine).callClearParticles();
        }
    }
    public static class RefreshScreen implements Runnable {
        public void run() {
            ((ConfigScreen)Minecraft.getInstance().screen).refresh();
        }
    }

    public static class SupplyParticleTypes implements Supplier<List<String>> {
        public List<String> get() {
            List<String> list = new ArrayList<>();
            for (Map.Entry<ResourceKey<ParticleType<?>>, ParticleType<?>> entry : BuiltInRegistries.PARTICLE_TYPE.entrySet()) {
                if (entry.getValue() instanceof SimpleParticleType) {
                    list.add(VersionUtil.getKeyId(entry.getKey()).toString());
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

    public static class ParticleIsCustomAndAlsoUsesCustomTint implements Function<Object, Boolean> {
        public Boolean apply(Object context) {
            ConfigData.ParticleData ctx = (ConfigData.ParticleData) context;
            return ctx.tintType.equals(ConfigData.TintType.CUSTOM) && new ParticleIsCustom().apply(context);
        }
    }
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
            return getDefaultConfig().particles.stream().noneMatch(
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