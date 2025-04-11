package pigcart.particlerain.particle.render;

//? if >=1.21.5 {
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.TriState;

import static net.minecraft.client.renderer.RenderPipelines.PARTICLE_SNIPPET;
import static net.minecraft.client.renderer.RenderStateShard.*;
//?} else {
/*import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.TriState;
*///?}

import net.minecraft.client.particle.ParticleRenderType;

// there doesnt seem to be an iris-compatible way to do this in >=1.21.5 ?
public class FogRenderType {
    //? if >=1.21.5 {
    public static final BlendFunction FOG_BLEND = new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
    //? if neoforge {
    /*public static final RenderPipeline FOG_PIPELINE = null;
    *///?} else {
    public static final RenderPipeline FOG_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(PARTICLE_SNIPPET)
                    .withLocation("pipeline/particlerain_fog")
                    .withBlend(FOG_BLEND)
                    .withDepthWrite(false).build()
    );
    //?}
    private static final RenderType FOG = RenderType.create(
            "particlerain_fog",
            1536,
            false,
            false,
            FOG_PIPELINE,
            RenderType.CompositeState.builder()
                    .setTextureState(new TextureStateShard(TextureAtlas.LOCATION_PARTICLES, TriState.FALSE, false))
                    .setOutputState(PARTICLES_TARGET)
                    .setLightmapState(LIGHTMAP)
                    .createCompositeState(false)
    );
    //?} else {
    /*private static final RenderType FOG = RenderType.create(
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
    *///?}
    public static final ParticleRenderType INSTANCE = new ParticleRenderType("particlerain:fog", FOG);
}