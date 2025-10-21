package pigcart.particlerain.particle.render;

//? if >=1.21.9 {
/*import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;

import static net.minecraft.client.renderer.RenderPipelines.PARTICLE_SNIPPET;

public class BlendedParticleRenderType {
    public static final BlendFunction FOG_BLEND = new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
    public static final RenderPipeline FOG_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(PARTICLE_SNIPPET)
                    .withLocation("pipeline/particlerain_fog")
                    .withBlend(FOG_BLEND)
                    .withDepthWrite(false).build()
    );
    public static final SingleQuadParticle.Layer INSTANCE = new SingleQuadParticle.Layer(true, TextureAtlas.LOCATION_PARTICLES, FOG_PIPELINE);
}
*///?} else if >=1.21.5 {
/*import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.TriState;

import static net.minecraft.client.renderer.RenderPipelines.PARTICLE_SNIPPET;
import static net.minecraft.client.renderer.RenderStateShard.*;

import net.minecraft.client.particle.ParticleRenderType;

// there doesnt seem to be an iris-compatible way to do this in >=1.21.5 ?
public class BlendedParticleRenderType {
    public static final BlendFunction FOG_BLEND = new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
    public static final RenderPipeline FOG_PIPELINE =
            //? if neoforge {
            /^// cant be bothered to figure out neo impl lmao. might just need an access transformer?
            RenderPipelines.TRANSLUCENT_PARTICLE;
            ^///?} else {
            RenderPipelines.register(
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
                    .setTextureState(new TextureStateShard(TextureAtlas.LOCATION_PARTICLES,
                            //? if <1.21.6 {
                                /^TriState.FALSE,
                            ^///?}
                            false))
                    .setOutputState(PARTICLES_TARGET)
                    .setLightmapState(LIGHTMAP)
                    .createCompositeState(false)
    );
    public static final ParticleRenderType INSTANCE = new ParticleRenderType("particlerain:fog", FOG);
}

*///?} else if 1.21.4 {
/*import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.TriState;

public class BlendedParticleRenderType {
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
*///?} else if 1.21.1 {
/*import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import org.jetbrains.annotations.Nullable;
import pigcart.particlerain.VersionUtil;

public class BlendedParticleRenderType {
    public static final ParticleRenderType INSTANCE = new ParticleRenderType() {
        @Override
        public @Nullable BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.setShaderTexture(2, VersionUtil.getMcId("dynamic/light_map_1"));
            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }
    };
}
*///?} else if 1.20.1 {
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import pigcart.particlerain.VersionUtil;

public class BlendedParticleRenderType {
    public static final ParticleRenderType INSTANCE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.setShaderTexture(2, VersionUtil.getMcId("dynamic/light_map_1"));
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }
        @Override
        public void end(Tesselator tessellator) {
            tessellator.end();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
        }
    };
}
//?}