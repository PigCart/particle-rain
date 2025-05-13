package pigcart.particlerain;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class StonecutterUtil {
    public static Biome.Precipitation getPrecipitationAt(Level level, Biome biome, BlockPos blockPos) {
        //? if >=1.21.4 {
        return biome.getPrecipitationAt(blockPos, level.getSeaLevel());
        //?} else {
        /*return biome.getPrecipitationAt(blockPos);
        *///?}
    }
    public static ResourceLocation getResourceLocation(String namespace, String path) {
        //? if <=1.20.1 {
        /*return new ResourceLocation(namespace, path);
        *///?} else {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
        //?}
    }
    public static ResourceLocation getResourceLocation(String path) {
        //? if <=1.20.1 {
        /*return new ResourceLocation(ResourceLocation.DEFAULT_NAMESPACE, path);
        *///?} else {
        return ResourceLocation.withDefaultNamespace(path);
        //?}
    }
    public static ResourceLocation parseResourceLocation(String string) {
        //? if <=1.20.1 {
        /*return ResourceLocation.tryParse(string);
        *///?} else {
        return ResourceLocation.parse(string);
        //?}
    }
    public static ClipContext getClipContext(Vec3 clipStart, Vec3 clipEnd) {
        //? if <=1.20.1 {
        /*return new ClipContext(clipStart, clipEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, Minecraft.getInstance().player);
        *///?} else {
        return new ClipContext(clipStart, clipEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, CollisionContext.empty());
        //?}
    }
    //? if <=1.20.1 {
    /*public static AnimationMetadataSection getSpriteMetadata() {
        return AnimationMetadataSection.EMPTY;
    }
    *///?} else {
    public static ResourceMetadata getSpriteMetadata() {
        return new ResourceMetadata.Builder().build();
    }
    //?}
}
