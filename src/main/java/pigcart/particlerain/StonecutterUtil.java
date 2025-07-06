package pigcart.particlerain;

import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.awt.*;

public class StonecutterUtil {
    public static Biome.Precipitation getPrecipitationAt(Level level, Biome biome, BlockPos blockPos) {
        //? if >=1.21.4 {
        /*return biome.getPrecipitationAt(blockPos, level.getSeaLevel());
        *///?} else {
        return biome.getPrecipitationAt(blockPos);
        //?}
    }
    @SuppressWarnings("removal")
    public static ResourceLocation getResourceLocation(String namespace, String path) {
        //? if <=1.20.1 {
        return new ResourceLocation(namespace, path);
        //?} else {
        /*return ResourceLocation.fromNamespaceAndPath(namespace, path);
        *///?}
    }
    @SuppressWarnings("removal")
    public static ResourceLocation getResourceLocation(String path) {
        //? if <=1.20.1 {
        return new ResourceLocation(ResourceLocation.DEFAULT_NAMESPACE, path);
        //?} else {
        /*return ResourceLocation.withDefaultNamespace(path);
        *///?}
    }

    public static ResourceLocation parseResourceLocation(String string) {
        try {
            //? if <=1.20.1 {
            return ResourceLocation.tryParse(string);
             //?} else {
            /*return ResourceLocation.parse(string);
            *///?}
        } catch (ResourceLocationException e) {
            ParticleRain.LOGGER.error(e.getMessage());
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

    public static Color getMapColor(ClientLevel level, BlockPos blockPos) {
        //? if >=1.21.4 {
        /*return new Color(level.getBlockState(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).below()).getBlock().defaultMapColor().calculateARGBColor(MapColor.Brightness.NORMAL));
        *///?} else {
        final Color color = new Color(level.getBlockState(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).below()).getBlock().defaultMapColor().calculateRGBColor(MapColor.Brightness.NORMAL));
        // red and blue are swapped
        return new Color(color.getBlue(), color.getGreen(), color.getRed());
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
