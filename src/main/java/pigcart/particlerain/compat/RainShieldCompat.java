package pigcart.particlerain.compat;


import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.VersionUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class RainShieldCompat {

    private static MethodHandle cancelRain;

    static {
        if (VersionUtil.isModLoaded("rainshield")) {
            try {
                cancelRain = MethodHandles.lookup().findStatic(
                        Class.forName("com.mrbysco.rainshield.util.RainShieldData"),
                        "cancelRain",
                        MethodType.methodType(boolean.class, Level.class, BlockPos.class)
                );
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
                ParticleRain.LOGGER.error("Could not load Rain Shield compat", e);
            }
        }
    }

    public static boolean cancelRain(Level level, BlockPos pos) {
        if (cancelRain != null) {
            try {
                return (boolean) cancelRain.invoke(level, pos);
            } catch (Throwable throwable) {
                ParticleRain.LOGGER.error("Rain Shield compat failed", throwable);
                cancelRain = null;
            }
        }
        return false;
    }
}
