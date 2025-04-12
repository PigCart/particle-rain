package pigcart.particlerain;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public class StonecutterUtil {
    public static Biome.Precipitation getPrecipitationAt(Level level, Biome biome, BlockPos blockPos) {
        //? if >=1.21.4 {
        return biome.getPrecipitationAt(blockPos, level.getSeaLevel());
        //?} else {
        /*return biome.getPrecipitationAt(blockPos);
        *///?}
    }
}
