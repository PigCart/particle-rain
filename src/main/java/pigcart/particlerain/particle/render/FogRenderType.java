package pigcart.particlerain.particle.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.TriState;

public class FogRenderType {
    private static final RenderType FOG = RenderType.create(
            "particlerain:fog",
            DefaultVertexFormat.PARTICLE,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderType.PARTICLE_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_PARTICLES, TriState.FALSE, false))
                    .setTransparencyState(new RenderStateShard.TransparencyStateShard(
                            "custom_blend",
                            () -> {
                                RenderSystem.depthMask(false);
                                RenderSystem.enableBlend();
                                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                            },
                            () -> {
                                RenderSystem.depthMask(true);
                                RenderSystem.disableBlend();
                            }
                    ))
                    .setLightmapState(RenderType.LIGHTMAP)
                    .setOverlayState(RenderType.OVERLAY)
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
                    .setOutputState(RenderType.PARTICLES_TARGET)
                    .createCompositeState(false)
    );

    public static final ParticleRenderType INSTANCE = new ParticleRenderType(
            "particlerain:fog",
            FOG
    );
}