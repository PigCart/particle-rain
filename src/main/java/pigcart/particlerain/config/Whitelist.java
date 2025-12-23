package pigcart.particlerain.config;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import pigcart.particlerain.VersionUtil;

import java.net.URI;
import java.util.ArrayList;

import static pigcart.particlerain.config.gui.Annotations.*;
import static pigcart.particlerain.config.ConfigManager.*;

public abstract class Whitelist<T> {

    @NoGUI private transient final ResourceKey<Registry<T>> registry;
    @NoGUI public transient ArrayList<TagKey<T>> tags = new ArrayList<>();
    @NoGUI public transient ArrayList<ResourceLocation> ids = new ArrayList<>();
    @BooleanFormat(t="whitelist", f="blacklist")
    public boolean isWhitelist;
    Whitelist(ResourceKey<Registry<T>> registry, boolean isWhitelist) {
        this.isWhitelist = isWhitelist;
        this.registry = registry;
    }
    Whitelist(ResourceKey<Registry<T>> registry) {
        this(registry, true);
    }

    // just to use different suppliers in the dropdown annotation
    public abstract ArrayList<String> getEntries();

    @Override
    public String toString() {
        return getEntries().toString();
    }

    public void populateInternalLists() {
        ids.clear();
        tags.clear();
        for (String string : getEntries()) {
            if (string.startsWith("#")) {
                ResourceLocation id = VersionUtil.parseId(string.substring(1));
                if (id != null) tags.add(TagKey.create(registry, id));
            } else {
                ResourceLocation id = VersionUtil.parseId(string);
                if (id != null) ids.add(id);
            }
        }
    }
    public boolean contains(Holder<T> holder) {
        if (!getEntries().isEmpty()) {
            for (TagKey<T> tag : tags) {
                boolean hasMatch = (holder.is(tag));
                if (isWhitelist && hasMatch) {
                    return true;
                } else if (hasMatch) {
                    return false;
                }
            }
            for (ResourceLocation id : ids) {
                boolean hasMatch = (holder.is(id));
                if (isWhitelist && hasMatch) {
                    return true;
                } else if (hasMatch) {
                    return false;
                }
            }
            return !isWhitelist;
        }
        return true;
    }

    @OverrideName("Whitelist")
    public static class BlockList extends Whitelist<Block> {
        public transient final URI wikiLink = URI.create("https://wiki.fabricmc.net/community:common_tags#block_tags");
        @NoSubMenu
        @Dropdown(SupplyBlocks.class)
        public ArrayList<String> entries;
        BlockList(boolean isWhitelist, ArrayList<String> list) {
            super(Registries.BLOCK, isWhitelist);
            this.entries = list;
        }
        public BlockList() {
            super(Registries.BLOCK);
            this.entries = new ArrayList<>();
        }
        @Override
        public ArrayList<String> getEntries() {
            return entries;
        }
    }
    @OverrideName("Whitelist")
    public static class BiomeList extends Whitelist<Biome> {
        public transient final URI wikiLink = URI.create("https://wiki.fabricmc.net/community:common_tags#biome_tags");
        @NoSubMenu
        @Dropdown(SupplyBiomes.class)
        public ArrayList<String> entries;
        BiomeList(boolean isWhitelist, ArrayList<String> list) {
            super(Registries.BIOME, isWhitelist);
            this.entries = list;
        }
        public BiomeList() {
            super(Registries.BIOME);
            this.entries = new ArrayList<>();
        }
        @Override
        public ArrayList<String> getEntries() {
            return entries;
        }
    }
}
