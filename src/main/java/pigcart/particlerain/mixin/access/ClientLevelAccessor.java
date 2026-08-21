package pigcart.particlerain.mixin.access;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientLevel.class)
public interface ClientLevelAccessor {

    //? 1.20.1 {
    @Invoker
    void callAddEntity(int entityId, Entity entityToSpawn);
    //?}
}
