package pigcart.particlerain.mixin.yacl;

import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(YACLScreen.class)
public interface YACLScreenAccessor {
    @Accessor(remap = false)
    Screen getParent();
}
