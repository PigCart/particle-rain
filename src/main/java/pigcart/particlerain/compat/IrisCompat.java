package pigcart.particlerain.compat;

import net.irisshaders.iris.api.v0.IrisApi;
import pigcart.particlerain.VersionUtil;

public class IrisCompat {
    public static boolean shadersEnabled() {
        return irisLoaded() && IrisApi.getInstance().isShaderPackInUse();

    }
    public static boolean irisLoaded() {
        //~ if forge 'iris' -> 'oculus'
        return VersionUtil.isModLoaded("iris");
    }
    //? if >=1.21.9 {
    /*public static void registerRenderPipelines() {
        if (irisLoaded()) {
            IrisApi.getInstance().assignPipeline(
                    pigcart.particlerain.particle.render.BlendedParticleRenderType.BLENDED_PARTICLE,
                    net.irisshaders.iris.api.v0.IrisProgram.PARTICLES_TRANSLUCENT
            );
        }
    }
    *///?}
}
