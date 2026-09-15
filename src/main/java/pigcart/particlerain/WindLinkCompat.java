package pigcart.particlerain;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

@SuppressWarnings("unchecked")
public final class WindLinkCompat {

    private static final String WIND_DATA = "com.plunderpixels.wind.api.PlunderWindData";
    private static final String WIND_SAMPLE = "com.plunderpixels.windsource.WindSample";

    private static final MethodHandle TARGET_FOR;
    private static final MethodHandle KNOWN;
    private static final MethodHandle VX;
    private static final MethodHandle VZ;
    private static final MethodHandle COUPLE_FOR;
    private static final MethodHandle WANTS_TO_DRIVE;
    private static final MethodHandle ALREADY_DRIVES;
    private static final MethodHandle SPRITES_FOR;
    private static final MethodHandle SPRITE_SIZE_FOR;
    private static final MethodHandle DENSITY_FOR;
    private static final MethodHandle SIZE_SCALE_FOR;
    private static boolean available;

    static {
        MethodHandle targetFor = null, known = null, vx = null, vz = null, coupleFor = null;
        MethodHandle wantsToDrive = null, alreadyDrives = null, spritesFor = null, spriteSizeFor = null;
        MethodHandle densityFor = null, sizeScaleFor = null;
        boolean found = false;
        if (VersionUtil.windLinkLoaded()) {
            try {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                Class<?> windData = Class.forName(WIND_DATA);
                Class<?> windSample = Class.forName(WIND_SAMPLE);
                targetFor = lookup.findStatic(windData, "targetFor", MethodType.methodType(windSample, String.class, String.class, Level.class, double.class, double.class, double.class))
                        .asType(MethodType.methodType(Object.class, String.class, String.class, Level.class, double.class, double.class, double.class));
                coupleFor = lookup.findStatic(windData, "coupleFor", MethodType.methodType(float.class, String.class, String.class));
                known = lookup.findVirtual(windSample, "known", MethodType.methodType(boolean.class))
                        .asType(MethodType.methodType(boolean.class, Object.class));
                vx = lookup.findVirtual(windSample, "vx", MethodType.methodType(double.class))
                        .asType(MethodType.methodType(double.class, Object.class));
                vz = lookup.findVirtual(windSample, "vz", MethodType.methodType(double.class))
                        .asType(MethodType.methodType(double.class, Object.class));
                found = true;
                try {
                    wantsToDrive = lookup.findStatic(windData, "wantsToDrive", MethodType.methodType(boolean.class, String.class));
                    alreadyDrives = lookup.findStatic(windData, "alreadyDrives", MethodType.methodType(boolean.class, Particle.class));
                } catch (ReflectiveOperationException e) {
                    ParticleRain.LOGGER.info("WindLink is older than the particle calls, leaving those to it");
                }
                try {
                    spritesFor = lookup.findStatic(windData, "spritesFor", MethodType.methodType(List.class, String.class, String.class));
                    spriteSizeFor = lookup.findStatic(windData, "spriteSizeFor", MethodType.methodType(float.class, String.class, String.class));
                } catch (ReflectiveOperationException e) {
                    ParticleRain.LOGGER.info("WindLink has no sprites to lend, using ours");
                }
                try {
                    densityFor = lookup.findStatic(windData, "densityFor", MethodType.methodType(float.class, String.class, String.class));
                    sizeScaleFor = lookup.findStatic(windData, "sizeScaleFor", MethodType.methodType(float.class, String.class, String.class));
                } catch (ReflectiveOperationException e) {
                    ParticleRain.LOGGER.info("WindLink has no density or size to ask for, using ours");
                }
                ParticleRain.LOGGER.info("WindLink found, weather particles will follow its wind");
            } catch (Throwable t) {
                ParticleRain.LOGGER.warn("WindLink is installed but its wind could not be read, using the built-in wind", t);
            }
        }
        TARGET_FOR = targetFor;
        COUPLE_FOR = coupleFor;
        KNOWN = known;
        VX = vx;
        VZ = vz;
        WANTS_TO_DRIVE = wantsToDrive;
        ALREADY_DRIVES = alreadyDrives;
        SPRITES_FOR = spritesFor;
        SPRITE_SIZE_FOR = spriteSizeFor;
        DENSITY_FOR = densityFor;
        SIZE_SCALE_FOR = sizeScaleFor;
        available = found;
    }

    private WindLinkCompat() {}

    public static boolean isAvailable() {
        return available;
    }

    public static boolean isDriving() {
        if (!available) return false;
        if (WANTS_TO_DRIVE == null) return true;
        try {
            return (boolean) WANTS_TO_DRIVE.invokeExact(ParticleRain.MOD_ID);
        } catch (Throwable t) {
            disable("Asking WindLink whether it drives Particle Rain failed", t);
            return false;
        }
    }

    public static boolean alreadyDrives(Particle particle) {
        if (!available) return false;
        if (ALREADY_DRIVES == null) return true;
        try {
            return (boolean) ALREADY_DRIVES.invokeExact(particle);
        } catch (Throwable t) {
            disable("Asking WindLink about a particle failed", t);
            return false;
        }
    }

    public static Vector3f target(Level level, double x, double y, double z, String particleId) {
        if (!available || TARGET_FOR == null) return null;
        try {
            Object reading = (Object) TARGET_FOR.invokeExact(ParticleRain.MOD_ID, particleId, level, x, y, z);
            if (!(boolean) KNOWN.invokeExact(reading)) return null;
            return new Vector3f((float) (double) VX.invokeExact(reading), 0, (float) (double) VZ.invokeExact(reading));
        } catch (Throwable t) {
            disable("Reading WindLink's wind failed", t);
            return null;
        }
    }

    public static float couple(String particleId) {
        if (!available || COUPLE_FOR == null) return 0;
        try {
            return (float) COUPLE_FOR.invokeExact(ParticleRain.MOD_ID, particleId);
        } catch (Throwable t) {
            disable("Asking WindLink how fast to follow its wind failed", t);
            return 0;
        }
    }

    public static List<String> spritesFor(String particleId) {
        if (!available || SPRITES_FOR == null || particleId == null) return null;
        try {
            List<String> sprites = (List<String>) SPRITES_FOR.invokeExact(ParticleRain.MOD_ID, particleId);
            return sprites == null || sprites.isEmpty() ? null : sprites;
        } catch (Throwable t) {
            disable("Asking WindLink for sprites failed", t);
            return null;
        }
    }

    public static float spriteSizeFor(String particleId) {
        if (!available || SPRITE_SIZE_FOR == null || particleId == null) return 0;
        try {
            float size = (float) SPRITE_SIZE_FOR.invokeExact(ParticleRain.MOD_ID, particleId);
            return size > 0 ? size : 0;
        } catch (Throwable t) {
            disable("Asking WindLink for a sprite size failed", t);
            return 0;
        }
    }

    public static float density(String particleId) {
        return scale(DENSITY_FOR, particleId, "Asking WindLink how much weather to spawn failed");
    }

    public static float sizeScale(String particleId) {
        return scale(SIZE_SCALE_FOR, particleId, "Asking WindLink for a particle size failed");
    }

    private static float scale(MethodHandle handle, String particleId, String whatFailed) {
        if (!available || handle == null || particleId == null) return 1;
        try {
            float v = (float) handle.invokeExact(ParticleRain.MOD_ID, particleId);
            return v > 0 ? v : 1;
        } catch (Throwable t) {
            disable(whatFailed, t);
            return 1;
        }
    }

    private static void disable(String what, Throwable t) {
        available = false;
        ParticleRain.LOGGER.warn(what + ", using the built-in wind", t);
    }
}
