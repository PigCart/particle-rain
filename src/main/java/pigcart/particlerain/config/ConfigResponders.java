package pigcart.particlerain.config;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import pigcart.particlerain.VersionUtil;
import pigcart.particlerain.ParticleLoader;
import pigcart.particlerain.config.gui.ConfigScreen;
import pigcart.particlerain.mixin.access.ParticleEngineAccessor;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConfigResponders {
    public static List<String> getRegistryEntries(Registry<?> registry) {
        List<String> list = new ArrayList<>();
        registry.keySet().forEach((id)-> list.add(id.toString()));
        VersionUtil.getTagIds(registry).forEach((tag)-> list.add("#" + tag.location()));
        return list;
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

    public static class DistanceInBlocks implements Function<Object, Component> {
        public Component apply(Object stringValue) {
            return Component.translatable("particlerain.distanceInblocks", stringValue);
        }
    }

    public static class TimeInTicks implements Function<Object, Component> {
        public Component apply(Object stringValue) {
            try {
                int i = Integer.parseInt((String) stringValue);
                return Component.translatable("particlerain.timeInTicks", stringValue, i / 20F);
            } catch (NumberFormatException e) {
                return Component.literal((String) stringValue);
            }
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

    public static class ParticleIsCustomAndAlsoUsesCustomTint implements Function<Object, Boolean> {
        public Boolean apply(Object context) {
            ParticleData ctx = (ParticleData) context;
            return ctx.tintType.equals(ParticleData.TintType.CUSTOM) && new ParticleIsCustom().apply(context);
        }
    }

    /// returns true when this particle is instantiated via the CustomParticle class,
    /// or false if a preset from minecraft's registry is used
    public static class ParticleIsCustom implements Function<Object, Boolean> {
        public Boolean apply(Object context) {
            ParticleData ctx = (ParticleData) context;
            return !ctx.usePresetParticle;
        }
    }

    public static class ParticleNotCustom implements Function<Object, Boolean> {
        public Boolean apply(Object context) {
            ParticleData ctx = (ParticleData) context;
            return ctx.usePresetParticle;
        }
    }

    /// returns true if this particle was not loaded by a resource pack
    public static class ParticleIsNotDefault implements Function<Object, Boolean> {
        public Boolean apply(Object context) {
            ParticleData ctx = (ParticleData) context;
            return !ParticleLoader.packParticles.containsKey(ctx.id);
        }
    }

    public static class UsingCustomTint implements Function<Object, Boolean> {
        public Boolean apply(Object context) {
            ParticleData ctx = (ParticleData) context;
            return ctx.tintType == ParticleData.TintType.CUSTOM;
        }
    }
}
