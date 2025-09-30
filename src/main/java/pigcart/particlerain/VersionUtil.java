package pigcart.particlerain;

import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.awt.*;
import java.util.stream.Stream;

public class VersionUtil {
    @SuppressWarnings("removal")
    public static ResourceLocation getId(String namespace, String path) {
        //? if <=1.20.1 {
        return new ResourceLocation(namespace, path);
        //?} else {
        /*return ResourceLocation.fromNamespaceAndPath(namespace, path);
        *///?}
    }
    @SuppressWarnings("removal")
    public static ResourceLocation getId(String path) {
        //? if <=1.20.1 {
        return new ResourceLocation(ResourceLocation.DEFAULT_NAMESPACE, path);
        //?} else {
        /*return ResourceLocation.withDefaultNamespace(path);
        *///?}
    }

    public static ResourceLocation parseId(String string) {
        try {
            //? if <=1.20.1 {
            return ResourceLocation.tryParse(string);
             //?} else {
            /*return ResourceLocation.parse(string);
            *///?}
        } catch (ResourceLocationException e) {
            return null;
        }
    }
    public static ClipContext getClipContext(Vec3 clipStart, Vec3 clipEnd) {
        //? if <=1.20.1 {
        return new ClipContext(clipStart, clipEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, Minecraft.getInstance().player);
        //?} else {
        /*return new ClipContext(clipStart, clipEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, CollisionContext.empty());
        *///?}
    }
    //? if <=1.20.1 {
    public static AnimationMetadataSection getSpriteMetadata() {
        return AnimationMetadataSection.EMPTY;
    }
    //?} else {
    /*public static ResourceMetadata getSpriteMetadata() {
        return new ResourceMetadata.Builder().build();
    }
    *///?}

    public static Biome.Precipitation getPrecipitationAt(Level level, Holder<Biome> biome, BlockPos blockPos) {
        //? if >=1.21.4 {
        /*return biome.value().getPrecipitationAt(blockPos, level.getSeaLevel());
         *///?} else {
        return biome.value().getPrecipitationAt(blockPos);
        //?}
    }

    public static Color getMapColor(ClientLevel level, BlockPos blockPos) {
        //? if >=1.21.4 {
        /*return new Color(level.getBlockState(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).below()).getBlock().defaultMapColor().calculateARGBColor(MapColor.Brightness.NORMAL));
        *///?} else {
        final Color color = new Color(level.getBlockState(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).below()).getBlock().defaultMapColor().calculateRGBColor(MapColor.Brightness.NORMAL));
        // red and blue are swapped
        return new Color(color.getBlue(), color.getGreen(), color.getRed());
        //?}
    }

    public static void schedule(Runnable task) {
        //? if >=1.21.4 {
        /*Minecraft.getInstance().schedule(task);
         *///?} else {
        Minecraft.getInstance().tell(task);
        //?}
    }

    public static <T> Stream<TagKey<T>> getTagIds(Registry<T> registry) {
        //? if >=1.21.4 {
        /*return registry.listTagIds();
        *///?} else {
        return registry.getTagNames();
        //?}
    }

    public static <T> Registry<T> getRegistry(ResourceKey<Registry<T>> key) {
        //? if >=1.21.4 {
        /*return Minecraft.getInstance().level.registryAccess().lookupOrThrow(key);
        *///?} else {
        return Minecraft.getInstance().level.registryAccess().registryOrThrow(key);
        //?}
    }

    public static int getCloudHeight(ClientLevel level) {
        //? if >=1.21.6 {
        /*return level.dimensionType().cloudHeight().isPresent() ? level.dimensionType().cloudHeight().get() : 0;
        *///?} else {
        return (int) level.effects().getCloudHeight();
        //?}
    }
}
