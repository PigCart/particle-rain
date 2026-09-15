package pigcart.particlerain;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import org.joml.Vector3f;
import pigcart.particlerain.config.ParticleData;
import pigcart.particlerain.mixin.access.ParticleAccessor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class WindManager {

    private static final int MAX_TICKS = 6000;

    private static final List<Tracked> TRACKED = new ArrayList<>();

    private static class Tracked {
        final Particle particle;
        final ParticleData data;
        int ticks;

        Tracked(Particle particle, ParticleData data) {
            this.particle = particle;
            this.data = data;
        }
    }

    private WindManager() {}

    public static void track(Particle particle, ParticleData data) {
        if (WindLinkCompat.alreadyDrives(particle)) return;
        applySpawnWind(particle, data, Minecraft.getInstance().level);
        TRACKED.add(new Tracked(particle, data));
    }

    public static void tick(ClientLevel level) {
        if (TRACKED.isEmpty()) return;
        Iterator<Tracked> iterator = TRACKED.iterator();
        while (iterator.hasNext()) {
            Tracked tracked = iterator.next();
            if (!tracked.particle.isAlive() || ++tracked.ticks > MAX_TICKS) {
                iterator.remove();
                continue;
            }
            applyWind(tracked.particle, tracked.data, level);
        }
    }

    public static void clear() {
        TRACKED.clear();
    }

    public static float windMultiplier(ClientLevel level, ParticleData data) {
        return level != null && level.isThundering() ? data.stormWindStrength : data.windStrength;
    }

    public static void applyWind(Particle particle, ParticleData data, ClientLevel level) {
        ParticleAccessor p = (ParticleAccessor) particle;
        Vector3f target = windLinkTarget(level, data, p.getX(), p.getY(), p.getZ());
        if (target != null) {
            float couple = WindLinkCompat.couple(data.id);
            if (couple > 0) {
                p.setXd(p.getXd() + (target.x - p.getXd()) * couple);
                p.setZd(p.getZd() + (target.z - p.getZd()) * couple);
                return;
            }
        }
        float multiplier = windMultiplier(level, data);
        if (multiplier == 0) return;
        Vector3f wind = ParticleRain.getWind(p.getX(), p.getY(), p.getZ()).mul(multiplier);
        p.setXd(p.getXd() + wind.x);
        p.setZd(p.getZd() + wind.z);
    }

    public static void applySpawnWind(Particle particle, ParticleData data, ClientLevel level) {
        ParticleAccessor p = (ParticleAccessor) particle;
        Vector3f target = windLinkTarget(level, data, p.getX(), p.getY(), p.getZ());
        if (target != null) {
            p.setXd(p.getXd() + target.x);
            p.setZd(p.getZd() + target.z);
            return;
        }
        float multiplier = windMultiplier(level, data);
        if (multiplier == 0) return;
        Vector3f wind = ParticleRain.getWind(p.getX(), p.getY(), p.getZ()).mul(multiplier * 10);
        p.setXd(p.getXd() + wind.x);
        p.setZd(p.getZd() + wind.z);
    }

    private static Vector3f windLinkTarget(ClientLevel level, ParticleData data, double x, double y, double z) {
        if (!WindLinkCompat.isDriving()) return null;
        if (windMultiplier(level, data) == 0) return null;
        return WindLinkCompat.target(level, x, y, z, data.id);
    }
}
