package pigcart.particlerain.mixin;

import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import pigcart.particlerain.config.gui.AbstractWidgetAccess;

@Mixin(AbstractWidget.class)
public class AbstractWidgetMixin implements AbstractWidgetAccess {

    @Unique public int xOffset = 0;

    @Override
    public int particle_rain$getOffset() {
        return xOffset;
    }

    @Override
    public void particle_rain$setOffset(int value) {
        xOffset = value;
    }
}
