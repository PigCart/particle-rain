package pigcart.particlerain.mixin.access;

import com.mojang.math.Transformation;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.class)
public interface DisplayAccessor {

    @Invoker
    void callSetTransformation(Transformation transformation);

    //? 1.20.1 {
    @Invoker
    void callSetInterpolationDuration(int interpolationDuration);

    @Invoker
    void callSetInterpolationDelay(int interpolationDelay);
    //?} else {
    /*@Invoker
    void callSetTransformationInterpolationDuration(int i);
    @Invoker
    void callSetTransformationInterpolationDelay(int i);
    *///?}

}
