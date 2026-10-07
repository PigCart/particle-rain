package pigcart.particlerain.mixin.access;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleAccessor {

    @Accessor("x")
    double getX();

    @Accessor("y")
    double getY();

    @Accessor("z")
    double getZ();

    @Accessor
    double getXd();

    @Accessor
    void setXd(double xd);

    @Accessor
    double getZd();

    @Accessor
    void setZd(double zd);
}
