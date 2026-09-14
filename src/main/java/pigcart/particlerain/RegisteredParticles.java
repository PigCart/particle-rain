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

public final class RegisteredParticles {

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

    private RegisteredParticles() {}

    public static void track(Particle particle, ParticleData data) {
        if (WindLinkCompat.alreadyDrives(particle)) return;
        ClientLevel level = Minecraft.getInstance().level;
        if (!steer(particle, data, level)) {
            float multiplier = windMultiplier(level, data);
            if (multiplier != 0) {
                push(particle, multiplier * 10);
            }
        }
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
            if (steer(tracked.particle, tracked.data, level)) continue;
            float multiplier = windMultiplier(level, tracked.data);
            if (multiplier != 0) push(tracked.particle, multiplier);
        }
    }

    public static void clear() {
        TRACKED.clear();
    }

    private static float windMultiplier(ClientLevel level, ParticleData data) {
        return level != null && level.isThundering() ? data.stormWindStrength : data.windStrength;
    }

    private static void push(Particle particle, float multiplier) {
        ParticleAccessor p = (ParticleAccessor) particle;
        Vector3f wind = ParticleRain.getWind(p.getX(), p.getY(), p.getZ()).mul(multiplier);
        p.setXd(p.getXd() + wind.x);
        p.setZd(p.getZd() + wind.z);
    }

    private static boolean steer(Particle particle, ParticleData data, ClientLevel level) {
        if (!WindLinkCompat.isDriving()) return false;
        if (windMultiplier(level, data) == 0) return false;
        ParticleAccessor p = (ParticleAccessor) particle;
        Vector3f target = WindLinkCompat.target(level, p.getX(), p.getY(), p.getZ(), data.id);
        if (target == null) return false;
        float couple = WindLinkCompat.couple(data.id);
        if (couple <= 0) return false;
        p.setXd(p.getXd() + (target.x - p.getXd()) * couple);
        p.setZd(p.getZd() + (target.z - p.getZd()) * couple);
        return true;
    }
}
