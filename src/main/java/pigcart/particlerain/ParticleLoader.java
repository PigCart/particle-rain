package pigcart.particlerain;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import pigcart.particlerain.config.ParticleData;

import java.awt.Color;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ParticleLoader {
    private static final TypeToken<Map<String, ParticleData>> WEATHER_PARTICLE_TYPE = new TypeToken<>() {};
    private static final String CUSTOM_PARTICLES_PATH = "config/particlerain/particles.json";

    public static Map<String, ParticleData> particles;
    public static Map<String, ParticleData> packParticles;

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(Color.class, new ColorTypeAdapter())
            .registerTypeAdapter(ParticleData.class, (InstanceCreator<?>) type -> {
                ParticleData newNullParticle = new ParticleData();
                for (Field field : ParticleData.class.getDeclaredFields()) {
                    try {
                        field.set(newNullParticle, null);
                    } catch (IllegalAccessException e) {
                        ParticleRain.LOGGER.error("Couldn't access field '{}' while instantiating null particle", field.getName(), e);
                    }
                }
                return newNullParticle;
            })
            .create();

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

    public static void onResourceManagerReload(ResourceManager resourceManager) {
        // particles needs to be a unique copy of the pack particles so customizations can be merged in
        particles = loadPackParticles(resourceManager);
        packParticles = loadPackParticles(resourceManager);
        loadCustomParticles();
        particles.forEach((id, data) -> data.updateTransientVariables());
        ParticleRain.LOGGER.info("Loaded {} particles", particles.size());
    }

    public static Map<String, ParticleData> loadPackParticles(ResourceManager resourceManager) {
        Map<String, ParticleData> map = new LinkedHashMap<>();
        for (Resource resource : resourceManager.getResourceStack(VersionUtil.getId("particles.json"))) {
            try (Reader reader = resource.openAsReader()) {
                deserializeParticles(reader, map);
            } catch (Exception e) {
                ParticleRain.LOGGER.warn("Invalid 'particles.json' in resourcepack: '{}'", resource.sourcePackId(), e);
            }
        }
        return map;
    }

    public static void loadCustomParticles() {
        File file = new File(CUSTOM_PARTICLES_PATH);
        if (!file.exists()) {
            ParticleRain.LOGGER.info("Skipping loading custom particles because no file exists at: " + CUSTOM_PARTICLES_PATH);
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            deserializeParticles(reader, particles);
        } catch (Exception e) {
            ParticleRain.LOGGER.error("Unable to load custom particles:", e);
        }
    }

    public static void deserializeParticles(Reader reader, Map<String, ParticleData> destination) {
        GSON.fromJson(reader, WEATHER_PARTICLE_TYPE).forEach((id, data) -> {
            data.id = id;
            if (destination.containsKey(id)) {
                destination.merge(id, data, ParticleLoader::mergeParticles);
            } else {
                destination.put(id, mergeParticles(new ParticleData(), data));
            }
        });
    }

    public static ParticleData mergeParticles(ParticleData mergeTo, ParticleData mergeFrom) {
        for (Field field : ParticleData.class.getDeclaredFields()) {
            try {
                final Object customizedValue = field.get(mergeFrom);
                if (customizedValue != null) {
                    field.set(mergeTo, customizedValue);
                }
            } catch (IllegalAccessException e) {
                ParticleRain.LOGGER.error("Couldn't access field '{}' while merging particle data", field.getName(), e);
            }
        }
        return mergeTo;
    }

    public static void saveCustomParticles() {
        Map<String, ParticleData> particlesToSave = new HashMap<>();
        particles.forEach((id, data) -> {
            particlesToSave.put(id, isolateCustomizedValues(data, packParticles.get(id)));
            data.updateTransientVariables();
        });
        try (FileWriter writer = new FileWriter(CUSTOM_PARTICLES_PATH)) {
            GSON.toJson(particlesToSave, writer);
        } catch (Exception e) {
            ParticleRain.LOGGER.error(e.getMessage());
        }
    }

    public static ParticleData isolateCustomizedValues(ParticleData data, ParticleData defaultData) {
        if (defaultData == null) return data; //no matching id in pack particles; this particle is entirely custom
        ParticleData dataToSave = new ParticleData();
        for (Field field : ParticleData.class.getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers())) continue;
            try {
                field.setAccessible(true);
                final Object valueToSave = field.get(data);
                final Object defaultValue = field.get(defaultData);
                if (defaultValue == null) System.out.println(field.getName());
                field.set(dataToSave, valueToSave.toString().equals(defaultValue.toString()) ? null : valueToSave);
            } catch (IllegalAccessException e) {
                ParticleRain.LOGGER.error("Couldn't access field '{}' while isolating save data", field.getName());
            }
        }
        return dataToSave;
    }
}
