//? >=1.21.9 {
/*package pigcart.particlerain.mixin;

import net.minecraft.core.particles.ParticleLimit;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ParticleLimit.class)
public class ParticleLimitMixin {

    // mods adding limits with values that already exist causes conflicts due to Record's value based behaviour
    // vanilla only adds one limit for spore particles so this usually doesnt matter
    // idk if a normal override is the correct way to go about this with mixin
    @Override
    public boolean equals(Object other) {
        return this == other;
    }
}
*///?}
