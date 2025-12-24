package pigcart.particlerain;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
//? >=1.21.11 {
/*import net.minecraft.world.attribute.EnvironmentAttributes;
*///?}
//? >=1.21.9 {
/*import net.minecraft.data.AtlasIds;
*///?} else {
import pigcart.particlerain.mixin.access.ParticleEngineAccessor;
//?}

import java.awt.Color;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;

public class VersionUtil {
    @SuppressWarnings("removal")
    public static ResourceLocation getId(String path) {
        //? if <=1.20.1 {
        return new ResourceLocation(ParticleRain.MOD_ID, path);
        //?} else {
        /*return ResourceLocation.fromNamespaceAndPath(ParticleRain.MOD_ID, path);
        *///?}
    }
    @SuppressWarnings("removal")
    public static ResourceLocation getMcId(String path) {
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
    public static AnimationMetadataSection getEmptySpriteMetadata() {
        return AnimationMetadataSection.EMPTY;
    }
    //?} else if <1.21.9 {
    /*public static ResourceMetadata getEmptySpriteMetadata() {
        return new ResourceMetadata.Builder().build();
    }
    *///?}

    //? if <=1.20.1 {
    public static SpriteContents loadSplashSprite(int i) throws IOException {
        final ResourceLocation location = getMcId("textures/particle/splash_" + i + ".png");
        Resource resource = Minecraft.getInstance().getResourceManager().getResourceOrThrow(location);
        AnimationMetadataSection metadata = resource.metadata().getSection(AnimationMetadataSection.SERIALIZER).orElse(AnimationMetadataSection.EMPTY);
        NativeImage splashImage = TextureUtil.loadTexture(resource);
        TextureUtil.desaturate(splashImage);
        return new SpriteContents(
                getId("splash_" + i),
                metadata.calculateFrameSize(splashImage.getWidth(), splashImage.getHeight()),
                splashImage, metadata);
    }
    //?} else {
    /*public static SpriteContents loadSplashSprite(int i) throws IOException {
        final ResourceLocation location = getMcId("textures/particle/splash_" + i + ".png");
        Resource resource = Minecraft.getInstance().getResourceManager().getResourceOrThrow(location);
        ResourceMetadata resourceMetadata = resource.metadata();
        NativeImage splashImage = TextureUtil.loadTexture(resource);
        TextureUtil.desaturate(splashImage);
        Optional<AnimationMetadataSection> animationMetadata = resourceMetadata.getSection(
                //? if >=1.21.4 {
                /^AnimationMetadataSection.TYPE
                 ^///?} else {
                AnimationMetadataSection.SERIALIZER
                //?}
        );
        FrameSize frameSize;
        if (animationMetadata.isPresent()) {
            frameSize = animationMetadata.get().calculateFrameSize(splashImage.getWidth(), splashImage.getHeight());
        } else {
            frameSize = new FrameSize(splashImage.getWidth(), splashImage.getHeight());
        }
        return new SpriteContents(
                getId("splash_" + i),
                frameSize,
                splashImage
                //? <1.21.11 {
                ,
                /^? if >=1.21.9 {^//^animationMetadata, List.of()^//^?} else {^/resourceMetadata/^?}^/
                //?}
        );
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

    public static int getCloudHeight(ClientLevel level, BlockPos pos) {
        //? if >=1.21.11 {
        /*return level.environmentAttributes().getValue(EnvironmentAttributes.CLOUD_HEIGHT, pos).intValue();
        *///?} else if >=1.21.6 {
        /*return level.dimensionType().cloudHeight().isPresent() ? level.dimensionType().cloudHeight().get() : 0;
        *///?} else {
        return (int) level.effects().getCloudHeight();
        //?}
    }

    public static TextureAtlasSprite getSprite(ResourceLocation id) {
        //? if >= 1.21.9 {
        /*return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.PARTICLES).getSprite(id);
         *///?} else {
        return ((ParticleEngineAccessor) Minecraft.getInstance().particleEngine).getTextureAtlas().getSprite(id);
        //?}
    }

    public static SpriteContents newNonAnimatedSpriteContents(String id, FrameSize frameSize, NativeImage sprite) {
        //? if >=1.21.9 {
        /*return new SpriteContents(getId(id), frameSize, sprite);
        *///?} else {
        return(new SpriteContents(VersionUtil.getId(id), frameSize, sprite, getEmptySpriteMetadata()));
        //?}
    }

    public static void openUri(URI uri) {
        //? >=1.21.11 {
        /*net.minecraft.util.Util.getPlatform().openUri(uri);
        *///?} else {
        net.minecraft.Util.getPlatform().openUri(uri);
        //?}
    }

    public static Vec3 camPos(Camera cam) {
        //? >=1.21.11 {
        /*return cam.position();
        *///?} else {
        return cam.getPosition();
        //?}
    }

    public static ResourceLocation getKeyId(ResourceKey key) {
        //? >=1.21.11 {
        /*return key.identifier();
        *///?} else {
        return key.location();
         //?}
    }

    public static Color getFogColor(Level level, BlockPos pos) {
        //? >=1.21.11 {
        /*return new Color(12112639);
        // environment values change dramatically at night. looks weird on particles. idk how to target only the day value.
        //return new Color(level.environmentAttributes().getValue(EnvironmentAttributes.FOG_COLOR, pos));
        *///?} else {
        return new Color(level.getBiome(pos).value().getFogColor());
         //?}
    }
}
