package pigcart.particlerain;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

import java.awt.*;
import java.io.IOException;

import static pigcart.particlerain.config.ModConfig.CONFIG;

public class WeatherBlockManager {
    //static final BlockState PUDDLE_STATE = ParticleRainClient.PUDDLE.defaultBlockState();//Blocks.WATER.defaultBlockState().setValue(BlockStateProperties.LEVEL, 7);

    static NativeImage puddleMap;
    static {
        try {
            puddleMap = TextureUtil.loadTexture(ResourceLocation.fromNamespaceAndPath(ParticleRainClient.MOD_ID, "textures/puddles.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getPuddleLevel(int x, int z) {
        x = Mth.abs(x % puddleMap.getWidth());
        z = Mth.abs(z % puddleMap.getHeight());
        final int pixel = puddleMap.getPixel(x, z);
        Color color = new Color(pixel);
        return color.getBlue();
    }
    public static boolean hasPuddle(ClientLevel level, BlockPos puddlePos) {
        return (level.getBiome(puddlePos).value().getPrecipitationAt(puddlePos, level.getSeaLevel()) == Biome.Precipitation.RAIN &&
                isExposed(level, puddlePos) &&
                getPuddleLevel(puddlePos.getX(), puddlePos.getZ()) < puddleThreshold &&
                level.getBlockState(puddlePos.below()).isFaceSturdy(level, puddlePos, Direction.DOWN) && level.getBlockState(puddlePos.below()).isCollisionShapeFullBlock(level, puddlePos.below()) &&
                level.getBlockState(puddlePos.north().below()).isSolid() && level.getBlockState(puddlePos.north()).getFluidState().isEmpty() &&
                level.getBlockState(puddlePos.east().below()).isSolid() && level.getBlockState(puddlePos.east()).getFluidState().isEmpty() &&
                level.getBlockState(puddlePos.south().below()).isSolid() && level.getBlockState(puddlePos.south()).getFluidState().isEmpty() &&
                level.getBlockState(puddlePos.west().below()).isSolid() && level.getBlockState(puddlePos.west()).getFluidState().isEmpty());
    }
    public static boolean isExposed(ClientLevel level, BlockPos blockPos) {
        final BlockPos heightmapPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos);
        if (level.canSeeSky(blockPos)) {
            if (heightmapPos.getY() > blockPos.getY()) {
                return !level.getBlockState(heightmapPos.below()).isCollisionShapeFullBlock(level, heightmapPos);
            }
            return true;
        }
        return false;
    }

    public static int puddleThreshold = 0;
    public static int puddleTargetLevel = 0;
    public static int ticksUntilPuddleUpdate = 0;

    public static void tick(ClientLevel level) {
        if (CONFIG.effect.doPuddles) {
            if (ticksUntilPuddleUpdate-- == 0) {
                ticksUntilPuddleUpdate = CONFIG.puddle.updateDelay;
                if (puddleThreshold != puddleTargetLevel) {
                    if (puddleThreshold < puddleTargetLevel) {
                        puddleThreshold += CONFIG.puddle.updateStep;
                        if (puddleThreshold > puddleTargetLevel) puddleThreshold = puddleTargetLevel;
                    } else {
                        puddleThreshold -= CONFIG.puddle.updateStep;
                        if (puddleThreshold < puddleTargetLevel) puddleThreshold = puddleTargetLevel;
                    }
                    setLevelDirty(level);
                }
                if (level.isRaining()) {
                    puddleTargetLevel = level.isThundering() ? CONFIG.puddle.stormLevel : CONFIG.puddle.rainLevel;
                } else {
                    puddleTargetLevel = 0;
                }
            }
        } else if (puddleThreshold != 0) {
            puddleThreshold = 0;
            setLevelDirty(level);
        }
    }

    public static void setLevelDirty(ClientLevel level) {
        int renderDistance = Minecraft.getInstance().options.renderDistance().get();
        ChunkPos playerPos = Minecraft.getInstance().player.chunkPosition();
        // parchment mappings mixed up x and y
        Minecraft.getInstance().levelRenderer.setSectionRangeDirty(
                playerPos.x - renderDistance,
                level.getMinSectionY(),
                playerPos.z - renderDistance,
                playerPos.x + renderDistance,
                level.getMaxSectionY(),
                playerPos.z + renderDistance);
    }
}
