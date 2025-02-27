package pigcart.particlerain;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

import java.awt.*;
import java.io.IOException;

public class WeatherBlockSpawner {
    //static final BlockState PUDDLE_STATE = ParticleRainClient.PUDDLE.defaultBlockState();//Blocks.WATER.defaultBlockState().setValue(BlockStateProperties.LEVEL, 7);

    static NativeImage puddleMap;
    static {
        try {
            puddleMap = Util.loadTexture(ResourceLocation.fromNamespaceAndPath(ParticleRainClient.MOD_ID, "textures/puddles.png"));
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
        return (level.isRainingAt(puddlePos) &&
                getPuddleLevel(puddlePos.getX(), puddlePos.getZ()) > 100 &&
                //level.getBlockState(puddlePos).getFluidState().isEmpty() &&
                level.getBlockState(puddlePos.below()).isFaceSturdy(level, puddlePos, Direction.DOWN) &&
                level.getBlockState(puddlePos.offset(1, -1, 0)).isSolid() &&
                level.getBlockState(puddlePos.offset(-1, -1, 0)).isSolid() &&
                level.getBlockState(puddlePos.offset(0, -1, 1)).isSolid() &&
                level.getBlockState(puddlePos.offset(0, -1, -1)).isSolid()
                );
    }

    public static boolean wasRaining = false;
    public static void tick(ClientLevel level) {
        //TODO: variable weather intensity
        if (level.isRaining()) {
            if (!wasRaining) {
                setLevelDirty(level);
                wasRaining = true;
            }
        } else if (wasRaining) {
            setLevelDirty(level);
            wasRaining = false;
        }
    }

    public static void setLevelDirty(ClientLevel level) {
        int renderDistance = Minecraft.getInstance().options.renderDistance().get();
        ChunkPos playerPos = Minecraft.getInstance().player.chunkPosition();
        System.out.println("marking chunks dirty to refresh puddles");
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
