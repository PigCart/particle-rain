package pigcart.particlerain.config;

import com.google.gson.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.config.gui.ConfigScreen;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static final String CONFIG_PATH = "config/particlerain/config.json";
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
        try {
            Files.createDirectories(Path.of("config", "particlerain"));
        } catch (IOException e) {
            ParticleRain.LOGGER.error("Couldn't create directory 'config/particlerain/'", e);
        }
        try (FileWriter writer = new FileWriter(CONFIG_PATH)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            ParticleRain.LOGGER.error("Couldn't save config", e);
        }
    }

}